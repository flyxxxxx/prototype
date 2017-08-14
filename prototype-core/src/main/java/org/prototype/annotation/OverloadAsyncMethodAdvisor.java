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
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * OverloadAsync注解实现. <br>
 * 
 * @author lj
 *
 */
@Slf4j
@Component
public class OverloadAsyncMethodAdvisor implements MethodAdvisor {

	@Resource
	private ApplicationContext context;

	@Resource
	private InjectHelper helper;
	@Resource
	private ExecutorManager manager;

	@Override
	public MethodFilter<?> matches(MethodBuilder builder, Errors errors) {
		OverloadAsync async = builder.getAnnotation(OverloadAsync.class);
		if (async == null) {
			return null;
		}
		boolean rs = true;
		if (!existExecutor(async.executor())) {// bean是否存在
			errors.add("overloadasync.executor.notfound", builder.toString(), async.executor());
			rs = false;
		}
		for (String name : async.value()) {
			MethodBuilder[] methods = builder.getClassBuilder().findMethods(name);
			for (MethodBuilder method : methods) {
				rs = checkAsycMethod(method, errors) && rs;
			}
		}
		return rs ? new OverloadAsyncMethodFilter(async) : null;
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
	 * 检查异步方法
	 * 
	 * @param builder
	 *            异步方法的构建器
	 * @param errors
	 *            错误
	 * @return 方法不正确返回false
	 */
	private boolean checkAsycMethod(MethodBuilder builder, Errors errors) {
		if (builder.enableInject(errors)) {
			if (!"void".equals(builder.getReturnType())) {// 方法不允许有返回值
				errors.add("async.method.return", builder.toString());
				return false;
			}
			return true;
		} else {// 不允许注入
			errors.add("method.inject.some", builder.toString());
			return false;
		}
	}

	/**
	 * 查询Executor
	 * 
	 * @param target
	 *            目标对象
	 * @param executor
	 *            Executor
	 * @return Executor实例
	 */
	private Executor findExecutor(Object target, String executor) {
		if ("".equals(executor)) {
			return manager.getAsyncExecutor(target);
		}
		return (Executor) context.getBean(executor);
	}

	/**
	 * 重载注解处理过滤
	 * 
	 * @author lj
	 *
	 */
	@Order(ChainOrder.VERY_HIGH)
	private class OverloadAsyncMethodFilter implements MethodFilter<OverloadAsync> {

		private OverloadAsync async;

		public OverloadAsyncMethodFilter(OverloadAsync async) {
			this.async = async;
		}

		@Override
		public Object doFilter(Object[] args, MethodChain chain) throws Exception {
			if (async.after()) {
				Object rs = chain.doFilter(args);
				doAsync(chain);
				return rs;
			} else {
				doAsync(chain);
				return chain.doFilter(args);
			}
		}

		/**
		 * 依次调用
		 * 
		 * @param chain
		 */
		private void doAsync(MethodChain chain) throws Exception {
			log.debug("Business {} , OverloadAsync in : {}",PrototypeStatus.getStatus().getId(), chain.getMethod());
			Executor executor = findExecutor(chain.getTarget(), async.executor());
			if (executor == null) {
				log.warn("OverloadAsync is need " + Executor.class.getName());
			}
			for (String name : async.value()) {
				for (Method method : chain.findMethods(name)) {
					if (method.getAnnotation(Async.class) == null) {
						execute(executor, chain, method);
					} else {
						method.invoke(chain.getTarget(), helper.getInjectParameters(method));
					}
				}
			}
		}

		/**
		 * 执行方法
		 * 
		 * @param chain
		 *            方法链
		 * @param method
		 *            方法
		 */
		private void execute(Executor executor, MethodChain chain, Method method) {
			Runnable runnable = new MethodInvokerRunnable(helper, method, chain.getTarget(), null, ChainOrder.LOWER);
			if (executor == null) {
				new Thread(runnable).start();
			} else {
				executor.execute(runnable);
			}
		}

	}

}
