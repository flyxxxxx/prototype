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

import javax.annotation.Resource;

import org.prototype.core.Errors;
import org.prototype.core.MethodAdvisor;
import org.prototype.core.MethodBuilder;
import org.prototype.core.MethodChain;
import org.prototype.core.MethodFilter;
import org.prototype.core.PrototypeStatus;
import org.prototype.inject.InjectHelper;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 方法责任链的适配. <br>
 * 此类可通过日志debug方式将责任链中的每个方法的用时输出到日志以进行性能分析
 * 
 * @see Chain
 * @author flyxxxxx@163.com
 *
 */
@Slf4j
@Component
public class ChainMethodAdvisor implements MethodAdvisor {

	/**
	 * 方法参数注入帮助类
	 */
	@Resource
	private InjectHelper helper;

	/**
	 * 对原方法，责任链的方法进行返回值及参数的检查
	 */
	@Override
	public MethodFilter<?> matches(MethodBuilder builder, Errors errors) {
		Chain chain = builder.getAnnotation(Chain.class);
		if (chain == null) {
			return null;
		}
		boolean rs = true;
		if (chain.value().length == 0) {// 至少要有一个方法
			errors.add("chain.value.required", builder.toString());
			rs = false;
		}
		if (!"void".equals(builder.getReturnType()) && !"boolean".equals(builder.getReturnType())) {// 检查返回值类型
			errors.add("chain.method.return", builder.toString());
			rs = false;
		}
		for (String methodName : chain.value()) {// 循环检查方法
			rs = checkMethod(builder, errors, methodName, chain.dynamic()) && rs;
		}
		boolean debug = log.isDebugEnabled();
		return rs ? new MethodFilterImpl(chain, debug) : null;
	}

	/**
	 * 检查责任链中的方法及其方法参数
	 * 
	 * @param builder
	 *            方法构建接口
	 * @param errors
	 *            错误
	 * @param methodName
	 *            方法名
	 * @return 是否有错误
	 */
	private boolean checkMethod(MethodBuilder builder, Errors errors, String methodName, boolean dynamic) {
		if (methodName.length() == 0) {
			errors.add("chain.method.empty", builder.toString());
			return false;
		}
		MethodBuilder ma = builder.getClassBuilder().findUniqueMethod(methodName, errors, Chain.class);
		if (ma == null && dynamic) {
			return true;
		}
		if (ma == null) {
			errors.add("chain.method.notfound", builder.toString(), methodName);// 方法未找到
			return false;
		}
		boolean rs = true;
		if (!"void".equals(ma.getReturnType()) && !"boolean".equals(ma.getReturnType())) {// 方法返回类型不对
			errors.add("chain.invoke.return", builder.toString());
			rs = false;
		}
		if (!ma.enableInjectFrom(builder, errors)) {// 不允许注入
			errors.add("method.inject.some", builder.toString());
			rs = false;
		}
		return rs;
	}

	/**
	 * 责任链方法过滤实现类
	 * 
	 * @author flyxxxxx@163.com
	 *
	 */
	private class MethodFilterImpl implements MethodFilter<Chain> {

		private Chain chain;

		private boolean debug;

		/**
		 * 构造
		 * 
		 * @param chain
		 *            责任链注解
		 */
		public MethodFilterImpl(Chain chain, boolean debug) {
			this.chain = chain;
			this.debug = debug;
		}

		@Override
		public Object doFilter(Object[] args, MethodChain methodChain) throws Exception {
			log.debug("Business {} doing chain : {} {} {}", PrototypeStatus.getStatus().getId(), chain,
					chain.after() ? "after" : "before", methodChain.getMethod());
			if (debug) {
				long nano = System.nanoTime();
				try {
					return doFilterInner(args, methodChain);
				} finally {
					log.debug("Business {} done chain : {} {} {} use time {} nanoseconds",
							PrototypeStatus.getStatus().getId(), chain, chain.after() ? "after" : "before",
							methodChain.getMethod(), System.nanoTime() - nano);
				}
			} else {
				return doFilterInner(args, methodChain);
			}
		}

		private Object doFilterInner(Object[] args, MethodChain methodChain) throws Exception {
			if (chain.after()) {// 先执行原方法
				Object rs = methodChain.doFilter(args);
				if (!Boolean.FALSE.equals(rs)) {
					rs = doChain(args, methodChain);
				}
				return rs;
			} else {// 后执行原方法
				Object rs = doChain(args, methodChain);
				if (!Boolean.FALSE.equals(rs)) {
					rs = methodChain.doFilter(args);
				}
				return rs;
			}
		}

		/**
		 * 循环执行责任链的方法
		 * 
		 * @param args
		 *            原方法参数
		 * @param methodChain
		 *            方法链
		 * @return 责任链接调用的结果
		 * @throws Exception
		 *             异常
		 */
		private Object doChain(Object[] args, MethodChain methodChain) throws Exception {
			Method sourceMethod = methodChain.getMethod();
			PrototypeStatus status=PrototypeStatus.getStatus();
			for (String methodName : chain.value()) {
				Method method = methodChain.findUniqueMethod(methodName, !chain.dynamic());
				if (method == null) {
					log.debug("Business {} chain method '{}' ignore in '{}'",status.getId(), methodName, sourceMethod);
					continue;
				}
				Object[] parameters = helper.getInjectParameters(method, sourceMethod, args);
				try {
					Object rs = method.invoke(methodChain.getTarget(), parameters);
					if (Boolean.FALSE.equals(rs)) {
						log.debug("Business {} chain method '{}' return false in '{}'",status.getId(), methodName, sourceMethod);
						return false;
					}
					log.debug("Business {} chain method '{}' executed in '{}'",status.getId(), methodName, sourceMethod);
				} finally {
					helper.release(method, sourceMethod, parameters);
				}
			}
			return true;
		}

	}
}
