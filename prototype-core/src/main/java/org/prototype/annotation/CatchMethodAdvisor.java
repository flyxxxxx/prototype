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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.prototype.core.ChainOrder;
import org.prototype.core.Errors;
import org.prototype.core.MethodAdvisor;
import org.prototype.core.MethodBuilder;
import org.prototype.core.MethodChain;
import org.prototype.core.MethodFilter;
import org.prototype.core.PrototypeStatus;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 异常注解处理. <br>
 * 实现对注解{@link Catch}的处理.
 * 
 * @author lj
 *
 */
@Component
@Slf4j
public class CatchMethodAdvisor implements MethodAdvisor {

	/**
	 * 要求异常处理方法以被捕获异常方法名为前缀，方法返回类型一致，参数只允许是一个异常类型。
	 */
	@Override
	public MethodFilter<?> matches(MethodBuilder builder, Errors errors) {
		Catch c = builder.getAnnotation(Catch.class);
		if (c == null) {
			return null;
		}
		String name = builder.getName();
		MethodBuilder[] builders = builder.getClassBuilder().findMethods(name + c.suffix());
		if (builders.length == 0) {// 至少要定义一个异常处理方法
			errors.add("catch.required", builder.toString(), name + c.suffix());
			return null;
		}
		boolean rs = true;
		String returnType = builder.getReturnType();
		for (MethodBuilder mb : builders) {// 依次检查异常处理方法
			rs = checkExceptionCatch(returnType, mb, errors) && rs;
		}
		return rs ? new CatchMethodFilter(c) : null;
	}

	/**
	 * 检查异常处理方法
	 * 
	 * @param returnType
	 *            方法返回类型
	 * @param mb
	 *            方法构建
	 * @param errors
	 *            错误
	 * @return 是否符合规则
	 */
	private boolean checkExceptionCatch(String returnType, MethodBuilder mb, Errors errors) {
		Class<?>[] parameterTypes = mb.getParameterTypes();
		boolean rs = true;
		if (parameterTypes.length != 1 || !Throwable.class.isAssignableFrom(parameterTypes[0])) {
			// 处理方法只能有一个参数并且是Throwable的子类型
			errors.add("catch.exception.params.required", mb.toString());
			rs = false;
		}
		if (!returnType.equals(mb.getReturnType())) {
			// 异常处理方法必须与原方法返回值相同
			errors.add("catch.exception.return", mb.toString(), returnType);
			rs = false;
		}
		return rs;
	}

	@Order(ChainOrder.HIGH)
	class CatchMethodFilter implements MethodFilter<Catch> {
		
		private Catch c;
		public CatchMethodFilter(Catch c){
			this.c=c;
		}

		/**
		 * 在调用原方法时进行异常捕获，如果未匹配到处理异常的方法，则将原异常抛出，否则调用匹配的异常处理方法进行处理
		 */
		@Override
		public Object doFilter(Object[] args, MethodChain chain) throws Exception {
			log.debug("Business {} catch exception for method : {}", PrototypeStatus.getStatus().getId(),chain.getMethod());
			try {
				return chain.doFilter(args);
			} catch (InvocationTargetException e) {
				Throwable t = e.getTargetException();
				if (t instanceof Exception) {
					return onException(chain, args, (Exception) t);
				} else {
					return onException(chain, args, new RuntimeException(t));
				}
			} catch (Exception e) {
				return onException(chain, args, e);
			}
		}

		/**
		 * 处理异常
		 * 
		 * @param chain 方法链
		 * @param args 参数
		 * @param e 异常
		 * @return 异常方法的返回值
		 * @throws Exception 调用异常
		 */
		private Object onException(MethodChain chain, Object[] args, Exception e) throws Exception {
			Method source=chain.getMethod();
			Method method = chain.findOverloadMethod(source.getName() + c.suffix(), e.getClass());
			if (method == null) {// 未匹配到处理异常的方法，则将原异常抛出
				throw e;
			}
			log.debug("Business {} catch exception to method : {}",PrototypeStatus.getStatus().getId(), method);
			Message.getSubject().onNext(new Message(Message.CATCH,
					source.getDeclaringClass().getName(), new Message.ExceptionMessage(chain, e)));
			return method.invoke(chain.getTarget(), new Object[] { e });// 调用匹配的异常处理方法进行处理
		}

	}

}
