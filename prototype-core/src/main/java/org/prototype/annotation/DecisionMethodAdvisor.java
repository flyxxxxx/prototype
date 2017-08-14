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
 * 决策注解的适配. <br>
 * 
 * @author lj
 *
 */
@Component
@Slf4j
public class DecisionMethodAdvisor implements MethodAdvisor {

	@Resource
	private InjectHelper helper;

	@Override
	public MethodFilter<?> matches(MethodBuilder builder, Errors errors) {
		Decision decision = builder.getAnnotation(Decision.class);
		if (decision == null) {
			return null;
		}
		boolean rs = true;
		if (decision.value().length < 2) {// 决策方向不可少于2
			errors.add("decision.value.required", builder.toString());
			rs = false;
		}
		String returnType = builder.getReturnType();
		if (!"java.lang.String".equals(returnType) && !"boolean".equals(returnType) && !"int".equals(returnType)) {// 检查返回值类型
			errors.add("decision.method.return", builder.toString());
			rs = false;
		}
		for (String methodName : decision.value()) {//// 循环检查方法
			rs = checkMethod(builder, errors, methodName) && rs;
		}
		return rs ? new DecisionMethodFilter(decision) : null;
	}

	/**
	 * 检查决策方法及其方法参数
	 * 
	 * @param builder
	 *            方法构建接口
	 * @param errors
	 *            错误
	 * @param methodName
	 *            方法名
	 * @return 是否有错误
	 */
	private boolean checkMethod(MethodBuilder builder, Errors errors, String methodName) {
		if (methodName.length() == 0) {
			errors.add("decision.method.empty", builder.toString());
			return false;
		}
		MethodBuilder ma = builder.getClassBuilder().findUniqueMethod(methodName, errors, Decision.class);
		if (ma == null) {// 方法未找到
			errors.add("decision.method.notfound", builder.toString(), methodName);
			return false;
		}
		boolean rs = true;
		if (!"void".equals(ma.getReturnType())) {// 方法返回类型不对
			errors.add("decision.invoke.return", builder.toString());
			rs = false;
		}
		if (!ma.enableInjectFrom(builder, errors)) {// 不允许注入
			errors.add("method.inject.some", builder.toString());//TODO 消息错误
			rs = false;
		}
		return rs;
	}

	/**
	 * 决策过滤实现类
	 * 
	 * @author flyxxxxx@163.com
	 *
	 */
	private class DecisionMethodFilter implements MethodFilter<Decision> {

		private Decision decision;

		public DecisionMethodFilter(Decision decision) {
			this.decision = decision;
		}

		/**
		 * 根据决策结果调用相应的方法
		 */
		@Override
		public Object doFilter(Object[] args, MethodChain chain) throws Exception {
			Method source = chain.getMethod();
			PrototypeStatus status=PrototypeStatus.getStatus();
			log.debug("Business {} decision beging for method '{}'",status.getId(), source);
			Object rs = chain.doFilter(args);
			String methodName = doDecision(rs);// 获取决策目标方法
			if (methodName != null) {// 决策成功
				log.debug("Business {} decision to method '{}' after method '{}'",status.getId(), methodName, source);
				Method method = chain.findUniqueMethod(methodName, true);
				Object[] parameters=helper.getInjectParameters(method, source, args);
				try{
					method.invoke(chain.getTarget(), parameters);
				}finally {
					helper.release(method, source, parameters);
				}
			} else {// 无决策
				log.debug("Business {} decision is do nothing after method {}",status.getId(), source);
			}
			return rs;
		}

		/**
		 * 根据返回值进行决策
		 * 
		 * @param rs
		 *            原方法返回结果
		 * @return 决策后的方法
		 */
		private String doDecision(Object rs) {
			int index = -1;
			if (rs == null) {
				index = -1;// 返回结果为null视为无决策
			} else if (Integer.class.isInstance(rs)) {
				index = (Integer) rs;// 整数值
			} else if (Boolean.class.isInstance(rs)) {
				index = Boolean.TRUE.equals(rs) ? 0 : 1;// boolean值
			} else {
				index = Arrays.asList(decision.value()).indexOf(rs);// 根据返回值进行索引
			}
			if (index < 0 || index >= decision.value().length) {// 默认决策
				return "".equals(decision.defaultValue()) ? null : decision.defaultValue();
			}
			return decision.value()[index];
		}

	}

}
