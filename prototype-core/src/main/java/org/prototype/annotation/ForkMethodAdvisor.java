/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.prototype.annotation;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import javax.annotation.Resource;

import org.prototype.core.ChainOrder;
import org.prototype.core.Errors;
import org.prototype.core.ExecutorManager;
import org.prototype.core.MethodAdvisor;
import org.prototype.core.MethodBuilder;
import org.prototype.core.MethodChain;
import org.prototype.core.MethodFilter;
import org.prototype.core.PrototypeStatus;
import org.prototype.inject.InjectHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 并发注解适配. <br>
 * 允许并发执行的方法有自己的异常处理（不会使用业务类的异常处理）.
 * 与责任链/异常处理/决策等方法不同的是，并行方法不允许直接引用定义注解Fork的方法的参数，方法参数只能是Spring
 * bean或通过BeanInjecter接口注入的对象。
 * 
 * @author lj
 *
 */
@Slf4j
@Component
public class ForkMethodAdvisor implements MethodAdvisor {

	@Resource
	private ApplicationContext context;

	@Resource
	private InjectHelper helper;
	
	@Resource
	private ExecutorManager manager;

	@Override
	public MethodFilter<?> matches(MethodBuilder accessor, Errors errors) {
		Fork fork = accessor.getAnnotation(Fork.class);
		if (fork == null) {
			return null;
		}
		boolean rs = true;
		if (fork.value().length < 2) {// 最少要有两个并发的方法
			errors.add("fork.value.required", accessor.toString());
			rs = false;
		}
		if (!existExecutor(fork.executor())) {// 检查executor是否存在
			errors.add("fork.executor.notfound", accessor.toString(), fork.executor());
			rs = false;
		}
		for (String methodName : fork.value()) {
			rs = checkMethod(accessor, errors, methodName) && rs;
		}
		return rs ? new ForkMethodFilter(fork) : null;
	}

	/**
	 * 检查指定的Excecutor bean是否存在
	 * 
	 * @param executor
	 *            bean名称
	 * @return 存在返回true
	 */
	private boolean existExecutor(String executor) {
		if (!"".equals(executor)) {
			return context.containsBean(executor) && Executor.class.isInstance(context.getBean(executor));
		}
		return true;
	}

	/**
	 * 对方法进行检查
	 * 
	 * @param builder
	 *            构建器
	 * @param errors
	 *            处理中的错误
	 * @param methodName
	 *            方法名
	 * @return 合格返回true
	 */
	private boolean checkMethod(MethodBuilder builder, Errors errors, String methodName) {
		if (methodName.length() == 0) {
			errors.add("fork.method.empty", builder.toString());
			return false;
		}
		MethodBuilder ma = builder.getClassBuilder().findUniqueMethod(methodName, errors, Fork.class);
		if (ma == null) {// 方法未找到
			errors.add("fork.method.notfound", builder.toString(), methodName);
			return false;
		}
		boolean rs = true;
		if (!"void".equals(ma.getReturnType())) {// 方法返回类型不对
			errors.add("fork.invoke.return", builder.toString());
			rs = false;
		}
		if (!ma.enableInjectFrom(builder, errors)) {// 不允许注入
			errors.add("method.inject.some", ma.toString());
			rs = false;
		}
		return rs;
	}

	/**
	 * 并行过滤实现
	 * 
	 * @author lj
	 *
	 */
	private class ForkMethodFilter implements MethodFilter<Fork> {
		private Fork fork;


		public ForkMethodFilter(Fork fork) {
			this.fork = fork;
		}

		/**
		 * 查找线程池
		 * @param target 目标对象
		 * @param executor
		 *            线程池的bean名称
		 * @return 线程池
		 */
		private Executor findExecutor(Object target,String executor) {
			if ("".equals(executor)) {
				return manager.getExecutor(target);
			}
			return (Executor) context.getBean(executor);
		}

		@Override
		public Object doFilter(Object[] args, MethodChain chain) throws Exception {
			if (fork.after()) {// 在原方法调用之后再并发
				Object rs = chain.doFilter(args);
				doFork(chain);
				return rs;
			} else {// 在原方法调用之前并发
				doFork(chain);
				return chain.doFilter(args);
			}
		}

		/**
		 * 并发执行多个方法
		 * 
		 * @param accessor
		 *            方法访问
		 * @param fork
		 * @throws InterruptedException
		 */
		private void doFork(MethodChain chain) throws InterruptedException {
			Method source = chain.getMethod();
			PrototypeStatus status=PrototypeStatus.getStatus();
			log.debug("Business {} start fork : {} in {}",status.getId(), Arrays.asList(fork.value()), source);
			CountDownLatch latch = new CountDownLatch(fork.value().length);
			Executor executor=findExecutor(chain.getTarget(),fork.executor());
			if(executor==null){
				log.warn("Business "+status.getId()+" , Annotationa 'Fork' is need "+Executor.class.getName());
			}
			for (String methodName : fork.value()) {
				Runnable worker = new MethodInvokerRunnable(helper, chain.findUniqueMethod(methodName, true),
						chain.getTarget(), latch ,ChainOrder.HIGH);
				if (executor == null) {
					new Thread(worker).start();
				} else {
					executor.execute(worker);
				}
			}
			latch.await();
			log.debug("Business {} end fork in {}",status.getId(), executor);
		}

	}

}
