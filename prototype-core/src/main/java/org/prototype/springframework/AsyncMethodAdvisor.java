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
package org.prototype.springframework;

import java.util.concurrent.Executor;

import javax.annotation.Resource;

import org.prototype.annotation.MethodInvokerRunnable;
import org.prototype.core.ChainOrder;
import org.prototype.core.Errors;
import org.prototype.core.ExecutorManager;
import org.prototype.core.MethodAdvisor;
import org.prototype.core.MethodBuilder;
import org.prototype.core.MethodChain;
import org.prototype.core.MethodFilter;
import org.prototype.inject.InjectHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring {@link org.springframework.scheduling.annotation.Async}注解的适配. <br>
 * 
 * <pre>
	异步注解的处理优先级仅次于{@link org.prototype.annotation.OverloadAsync}，高于任何其它方法上的注解，包括{@link org.prototype.annotation.Catch}注解和spring Transactional注解。 
	如果需要处理异步调用的异常（包括Fork注解,OverloadAsync调用的方法），可以下例作为参数：
	&#064;Async void async(...){//do something }
	void asyncException(MyException e){//处理指定类型的异常 }
	void asyncException(Exception e){ //处理其它异常 }
 * </pre>
 * 
 * @author lj
 *
 */
@Slf4j
@Component
public class AsyncMethodAdvisor implements MethodAdvisor {

	@Resource
	private ApplicationContext context;
	@Resource
	private ExecutorManager manager;

	@Override
	public MethodFilter<?> matches(MethodBuilder builder, Errors errors) {
		Async async = builder.getAnnotation(Async.class);
		if (async == null) {
			return null;
		}
		boolean rs = true;
		if (!"void".equals(builder.getReturnType())) {// 方法不允许有返回值
			errors.add("async.method.return", builder.toString());
			rs = false;
		}
		if (!existExecutor(async.value())) {// bean是否存在
			errors.add("async.executor.notfound", builder.toString(), async.value());
			rs = false;
		}
		return rs ? new SpringAsyncMethodFilter(async) : null;
	}

	/**
	 * 查找Executor
	 * 
	 * @param target
	 *            目标对象
	 * @param executor
	 *            Executor
	 * @return Executor实例或null
	 */
	private Executor findExecutor(Object target, String executor) {
		if ("".equals(executor)) {
			return manager.getAsyncExecutor(target);
		}
		return (Executor) context.getBean(executor);
	}

	/**
	 * 异步方法过滤
	 * 
	 * @author lj
	 *
	 */
	@Order(ChainOrder.VERY_HIGH)
	private class SpringAsyncMethodFilter implements MethodFilter<Async> {

		private Async async;

		public SpringAsyncMethodFilter(Async async) {
			this.async = async;
		}

		@Override
		public Object doFilter(Object[] args, MethodChain chain) throws Exception {
			Executor executor = findExecutor(chain.getTarget(), async.value());
			Runnable runnable = new MethodInvokerRunnable(context.getBean(InjectHelper.class), chain, ChainOrder.LOWER);
			if (executor == null) {
				log.warn("Async is need " + Executor.class.getName());
				new Thread(runnable).run();
			} else {
				executor.execute(runnable);
			}
			return null;
		}

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

}
