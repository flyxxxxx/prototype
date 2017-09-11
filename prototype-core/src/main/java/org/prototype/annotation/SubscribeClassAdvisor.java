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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.prototype.core.ChainOrder;
import org.prototype.core.ClassBuilder;
import org.prototype.core.ClassFactory;
import org.prototype.core.Errors;
import org.prototype.core.MethodBuilder;
import org.prototype.inject.InjectHelper;
import org.prototype.reflect.MethodUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 消息订阅类的处理. <br>
 * 支持构造方法注入bean.
 * @author flyxxxxx@163.com
 *
 */
@Slf4j
@Component
@Order(ChainOrder.VERY_LOWER)
public class SubscribeClassAdvisor extends AbstractClassAdvisor {

	@Resource
	private InjectHelper helper;

	/**
	 * 对方法进行检查：方法必须只有一个参数，返回值类型必须是void
	 * 
	 * @param builder
	 *            要检查的方法构建器
	 * @param errors
	 *            处理中的错误
	 * @return 合格返回true
	 */
	private boolean checkMethod(MethodBuilder builder, Errors errors) {
		boolean rs = true;
		int num = builder.getParameterTypes().length;
		if (num == 0 ) {
			errors.add("subscribe.invoke.param", builder.toString());
			rs = false;
		}else{
			Class<?>[] types=builder.getParameterTypes();
			for(int i=1,k=types.length;i<k;i++){
				if(!helper.enableInject(types[i], builder.getParameterAnnotations(i), true, errors)){
					errors.add("method.inject", builder.toString(),Integer.toString(i));
					rs = false;
				}
			}
		}
		if (!"void".equals(builder.getReturnType())) {
			errors.add("subscribe.invoke.return", builder.toString());
			rs = false;
		}
		return rs;
	}

	/**
	 * 消息订阅
	 * 
	 * @author flyxxxxx@163.com
	 *
	 */
	private class MessageSubscribe implements Action1<Message> {

		private Class<?> clazz;
		private Subscribe subscribe;
		private Object target;

		/**
		 * 消息订阅构造
		 * 
		 * @param clazz
		 *            消息处理类
		 * @param subscribe
		 *            注解
		 */
		public MessageSubscribe(Class<?> clazz, Subscribe subscribe) {
			this.clazz = clazz;
			this.subscribe = subscribe;
			try {
				Constructor<?>[] constructors = clazz.getConstructors();
				if (constructors[0].getParameterCount() == 0) {
					target = constructors[0].newInstance();
				} else {
					Object[] args = helper.getInjectParameters(constructors[0]);
					target = constructors[0].newInstance(args);
				}
			} catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
				throw new RuntimeException();
			}
		}

		@Override
		public void call(Message message) {
			Object content = message.getContent();
			Method method = MethodUtils.findOverloadMethod(clazz, subscribe.onMessage(), content.getClass());
			try {
				method.invoke(target, helper.getInjectParameters(method, content));
			} catch (Exception e) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				} else {
					throw new RuntimeException(e);
				}
			}
		}

	}

	/**
	 * 消息过滤
	 * 
	 * @author flyxxxxx@163.com
	 *
	 */
	private class MessageFilter implements Func1<Message, Boolean> {

		private List<String> types;// 消息类别
		private List<String> includes;// 包括的
		private List<String> excludes;// 排除的
		private List<Class<?>> msgTypes = new ArrayList<>();// 消息内容数据类型匹配的

		/**
		 * 消息过滤器构造
		 * 
		 * @param clazz
		 *            消息处理类
		 * @param subscribe
		 *            注解
		 */
		public MessageFilter(Class<?> clazz, Subscribe subscribe) {
			types = Arrays.asList(subscribe.type());
			includes = Arrays.asList(subscribe.incloudSource());
			excludes = Arrays.asList(subscribe.excloudSource());
			List<Method> methods = MethodUtils.findMethods(clazz, subscribe.onMessage());
			for (Method method : methods) {
				Class<?> type = method.getParameterTypes()[0];
				msgTypes.add(MethodUtils.getWrapperClass(type));
			}
		}

		@Override
		public Boolean call(Message message) {
			if (types.indexOf(message.getType()) == -1) {// 类别不匹配
				return Boolean.FALSE;
			}
			String source = message.getSource();
			for (String exclude : excludes) {// 排除
				if (source.startsWith(exclude)) {
					return Boolean.FALSE;
				}
			}
			Boolean rs = Boolean.FALSE;
			for (Class<?> cls : msgTypes) {// 消息内容数据类型匹配的
				if (cls.isInstance(message.getContent())) {
					rs = Boolean.TRUE;
					break;
				}
			}
			if (Boolean.FALSE.equals(rs)) {
				return rs;
			}
			if (includes.isEmpty()) {
				return Boolean.TRUE;
			}
			for (String include : includes) {// 包含
				if (source.startsWith(include)) {
					return Boolean.TRUE;
				}
			}
			return Boolean.FALSE;
		}

	}

	/**
	 * 侦听消息的类必须有无参构造方法，注解的type值必须至少有一个
	 */
	@Override
	protected boolean matches(ClassBuilder builder, Errors errors) {
		Subscribe subscribe = builder.getAnnotation(Subscribe.class);
		if (subscribe != null) {
			boolean result = true;
			if (subscribe.type().length == 0) {
				errors.add("subscribe.type", builder.getName());
				result = false;
			}
			if (!builder.enableConstructInject(errors)) {
				return false;
			}
			MethodBuilder[] methods = builder.findMethods(subscribe.onMessage());
			int rs = 0;
			for (MethodBuilder ma : methods) {
				rs = rs + ((checkMethod(ma, errors)) ? 1 : 0);
			}
			if (rs > 0 && result) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 注册侦听
	 */
	@Override
	protected void register(ClassFactory factory, Class<?> clazz) {
		log.debug("Subscibe for {}", clazz);
		Subscribe subscribe = clazz.getAnnotation(Subscribe.class);
		Observable<Message> observable = Message.getSubject().filter(new MessageFilter(clazz, subscribe));
		if (subscribe.async()) {
			observable.subscribeOn(Schedulers.io());
		}
		observable.subscribe(new MessageSubscribe(clazz, subscribe));
	}

}
