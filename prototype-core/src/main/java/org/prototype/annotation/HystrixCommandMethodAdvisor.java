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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.prototype.PrototypeInitializer;
import org.prototype.core.ChainOrder;
import org.prototype.core.ConditionalHasClass;
import org.prototype.core.Errors;
import org.prototype.core.MethodAdvisor;
import org.prototype.core.MethodBuilder;
import org.prototype.core.MethodChain;
import org.prototype.core.MethodFilter;
import org.prototype.core.PrototypeStatus;
import org.prototype.inject.InjectHelper;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.netflix.hystrix.HystrixCommand.Setter;
import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.hystrix.contrib.javanica.command.GenericSetterBuilder;
import com.netflix.hystrix.contrib.javanica.command.GenericSetterBuilder.Builder;
import com.netflix.hystrix.exception.HystrixBadRequestException;

import lombok.extern.slf4j.Slf4j;

/**
 * HystrixCommand注解的支持。<br>
 * 
 * <pre>
 * 1、必须引入Hystrix相关的依赖。
 * 2、HystrixCommand的fallback方法（默认是原方法名+Fallback后缀）与加HystrixCommand注解的方法返回值类型必须一致.
 * 3、HystrixCommand#fallback中指定的每个方法允许有以下几种类型的参数（并按此优先顺序）:
 * 	  a)、可以是与加HystrixCommand注解的方法的参数，只要相同类型和名称（顺序不做要求）.
	  b)、能通过接口{@link org.prototype.core.BeanInjecter}注入。
	  c)、来源于Spring applicationContext中的bean.
 * 4、可以在启动类中加入注解DefaultProperties作为全局配置
 * </pre>
 * 
 * @author lj
 *
 */
@Slf4j
@Component
@ConditionalHasClass({ HystrixCommand.class, Setter.class })
public class HystrixCommandMethodAdvisor implements MethodAdvisor {

	private static final String FALLBACK = "Fallback";

	@Resource
	private PrototypeInitializer initializer;

	@Resource
	private InjectHelper helper;

	/**
	 * Fallback方法必须存在，返回值类型必须与原方法一致，参数必须可注入。
	 */
	@Override
	public MethodFilter<?> matches(MethodBuilder builder, Errors errors) {
		HystrixCommand command = builder.getAnnotation(HystrixCommand.class);
		if (command == null) {
			return null;
		}
		String fallbackName = getValue(command.fallbackMethod(), builder.getName() + FALLBACK);
		MethodBuilder mb = builder.getClassBuilder().findUniqueMethod(fallbackName, errors, HystrixCommand.class);
		if (mb == null) {
			errors.add("hystrix.command.method.notfound", builder.toString(), fallbackName);// 方法未找到
			return null;
		}
		boolean rs = true;
		if (!mb.getReturnType().equals(builder.getReturnType())) {
			errors.add("hystrix.command.method.return", builder.toString(), fallbackName, builder.getReturnType());// 方法返回值类型不匹配
			rs = false;
		}
		if (!mb.enableInjectFrom(builder, errors)) {// 不允许注入
			errors.add("method.inject.some", mb.toString());
			rs = false;
		}
		return rs ? new HystrixCommandMethodFilter(command) : null;
	}

	/**
	 * 方法过滤实现
	 * 
	 * @author lj
	 *
	 */
	@Order(ChainOrder.HIGH)
	private class HystrixCommandMethodFilter implements MethodFilter<HystrixCommand> {

		private HystrixCommand command;// 注解

		private Setter setter;

		private Method fallbackMethod;// fallback方法

		/**
		 * 构造
		 * 
		 * @param command
		 *            注解
		 */
		public HystrixCommandMethodFilter(HystrixCommand command) {
			this.command = command;
		}

		@Override
		public Object doFilter(Object[] args, MethodChain chain) throws Exception {
			fallbackMethod = getFallbackMethod(chain);
			return new ServiceCommand(getSetter(chain), chain, args, getIgnoreExceptions()).execute();
		}

		/**
		 * 缓存回调方法
		 */
		private Method getFallbackMethod(MethodChain chain) {
			if (fallbackMethod == null) {
				synchronized (this) {
					String fallbackName = getValue(command.fallbackMethod(), chain.getMethod().getName() + FALLBACK);
					fallbackMethod = chain.findUniqueMethod(fallbackName, true);
				}
			}
			return fallbackMethod;
		}

		/**
		 * 缓存Setter
		 */
		private Setter getSetter(MethodChain chain) {
			if (setter == null) {
				synchronized (this) {
					setter = createSetter(chain);
				}
			}
			return setter;
		}

		/**
		 * 合并默认属性和注解上的属性
		 */
		private List<HystrixProperty> merge(HystrixProperty[] defaultProperties, HystrixProperty[] properties) {
			Map<String, HystrixProperty> map = new HashMap<>();
			for (HystrixProperty property : defaultProperties) {
				map.put(property.name(), property);
			}
			for (HystrixProperty property : properties) {
				map.put(property.name(), property);
			}
			return new ArrayList<>(map.values());
		}

		/**
		 * 创建HystrixCommand.Setter
		 * 
		 * @param chain
		 *            方法链
		 * @return HystrixCommand.Setter
		 */
		private Setter createSetter(MethodChain chain) {
			DefaultProperties defaultProperties = initializer.getBootClass().getAnnotation(DefaultProperties.class);
			Class<?> clazz = chain.getTarget().getClass();
			Builder builder = GenericSetterBuilder.builder()
					.commandKey(getValue(command.commandKey(), chain.getMethod().getName()));
			if (defaultProperties == null) {// 没有全局设置
				builder.groupKey(command.groupKey(), clazz.getName());
				builder.commandProperties(Arrays.asList(command.commandProperties()));
				builder.threadPoolKey(command.threadPoolKey());
				builder.threadPoolProperties(Arrays.asList(command.threadPoolProperties()));
			} else {// 有全局设置
				builder.groupKey(getDefault(command.groupKey(), defaultProperties.groupKey(), clazz.getName()));
				builder.commandProperties(merge(defaultProperties.commandProperties(), command.commandProperties()));
				builder.threadPoolKey(getValue(command.threadPoolKey(), defaultProperties.threadPoolKey()));
				builder.threadPoolProperties(
						merge(defaultProperties.threadPoolProperties(), command.threadPoolProperties()));
			}
			return new GenericSetterBuilder(builder).build();
		}

		/**
		 * 获取所有需要忽略的异常(包括默认配置的属性)
		 * 
		 * @return 需要忽略的异常
		 */
		private Set<Class<?>> getIgnoreExceptions() {
			Set<Class<?>> rs = new HashSet<>();
			DefaultProperties defaultProperties = initializer.getBootClass().getAnnotation(DefaultProperties.class);
			if (defaultProperties != null) {
				for (Class<?> clazz : defaultProperties.ignoreExceptions()) {
					rs.add(clazz);
				}
			}
			for (Class<?> clazz : command.ignoreExceptions()) {
				rs.add(clazz);
			}
			return rs;
		}

		/**
		 * HystrixCommand实现类
		 * 
		 * @author lj
		 *
		 * @param <T>
		 */
		private class ServiceCommand extends com.netflix.hystrix.HystrixCommand<Object> {

			private MethodChain chain;
			private Object[] args;
			private Set<Class<?>> ignoreExceptions;// 忽略的异常

			private Exception exception;// 捕获的异常
			
			private String id;

			/**
			 * 构造
			 * 
			 * @param setter
			 *            HystrixCommand.Setter
			 * @param chain
			 *            方法链
			 * @param args
			 *            方法参数
			 */
			public ServiceCommand(Setter setter, MethodChain chain, Object[] args, Set<Class<?>> ignoreExceptions) {
				super(setter);
				this.chain = chain;
				this.args = args;
				this.ignoreExceptions = ignoreExceptions;
				this.id=PrototypeStatus.getStatus().getId();
			}

			@Override
			protected Object run() throws Exception {
				try {
					return chain.doFilter(args);
				} catch (InvocationTargetException e) {
					Throwable throwable = e.getTargetException();
					if (isIgonre(throwable)) {// 抛出忽略的异常消息
						Message.getSubject()
								.onNext(new Message(Message.IGNORE_EXCEPTION, chain.getTarget().getClass().toString(),
										new Message.ExceptionMessage(chain, exception)));
						throw new HystrixBadRequestException(throwable.getMessage(), throwable);
					}
					exception = Exception.class.isInstance(throwable) ? (Exception) throwable
							: new RuntimeException(throwable);// 抛出异常
					throw exception;
				}
			}

			/**
			 * 检查异常是否需要忽略
			 * 
			 * @param throwable
			 *            抛出的异常
			 * @return 忽略则返回true
			 */
			private boolean isIgonre(Throwable throwable) {
				for (Class<?> cls : ignoreExceptions) {
					if (cls.isInstance(throwable)) {
						return true;
					}
				}
				return false;
			}

			/**
			 * 执行fallback方法
			 */
			@Override
			protected Object getFallback() {
				Method method = chain.getMethod();
				if (exception != null) {// 抛出Fallback消息
					Message.getSubject().onNext(new Message(Message.FALLBACK, chain.getTarget().getClass().getName(),
							new Message.ExceptionMessage(chain, exception)));
				}
				log.debug("Business {} , method {} fallback",id, method);
				Object[] params = helper.getInjectParameters(fallbackMethod, method, args);
				try {
					return fallbackMethod.invoke(chain.getTarget(), params);// 调用fallback方法
				} catch (Exception e) {
					throw new RuntimeException(e);
				} finally {
					helper.release(fallbackMethod, method, params);// 回收资源
				}
			}
		}

	}

	/**
	 * 如果value为null或空字符串，则返回默认值
	 * 
	 * @param value
	 *            检查的值
	 * @param defaultValue
	 *            默认值
	 * @return 最终值
	 */
	private static String getValue(String value, String defaultValue) {
		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return value;
	}

	/**
	 * 如果valuevalue为null或空字符串，则检查全局值，如果global为null或空字符串，则返回默认值
	 * 
	 * @param value
	 *            检查的值
	 * @param global
	 *            全局值
	 * @param defaultValue
	 *            默认值
	 * @return 最终值
	 */
	private static String getDefault(String value, String global, String defaultValue) {
		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return value;
	}
}
