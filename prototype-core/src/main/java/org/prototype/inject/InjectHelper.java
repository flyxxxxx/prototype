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
package org.prototype.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.prototype.core.BeanInjecter;
import org.prototype.core.ComponentContainer;
import org.prototype.core.Errors;
import org.prototype.core.ParameterInject;
import org.prototype.core.ParameterInjecter;
import org.prototype.javassist.CtMethodUtils;
import org.prototype.reflect.AnnotationUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

/**
 * 方法参数注入帮助类. <br>
 * 
 * <pre>
 * 支持以下几种方式注入参数：
 * 1、从前一个方法继承（必须有相应注解支持，如Chain、Decision等），所有涉及异步处理的注解均不能支持此方式（避免资源释放问题）.
 * 2、参数使用注解{@link org.prototype.core.ParameterInject}
 * 3、参数类型有对应的{@link org.prototype.core.BeanInjecter}接口实现（支持Spring注解Qualifier）
 * 4、参数来源于Spring ApplicationContext中的bean（支持Spring注解Qualifier）
 * </pre>
 * 
 * @author flyxxxxx@163.com
 *
 */
@Component
@Slf4j
public class InjectHelper {

	// 空数组
	private static final Object[] EMPTY_ARRAY = new Object[0];

	@Resource
	private ApplicationContext applicationContext;

	@Resource
	private ComponentContainer container;

	private Map<Class<?>, ParameterInjecter<?,?>> parameterInjecters = new ConcurrentHashMap<>();

	private Map<Class<?>, BeanInjecter<?>> map = new HashMap<>();

	/**
	 * 初始化
	 */
	@PostConstruct
	void init() {
		ComponentContainer container = applicationContext.getBean(ComponentContainer.class);
		for (BeanInjecter<?> injecter : container.getComponents(BeanInjecter.class)) {
			Class<?> supportedType = ResolvableType.forClass(AopUtils.getTargetClass(injecter)).as(BeanInjecter.class)
					.getGeneric(0).resolve();
			Assert.notNull(supportedType,
					"Bean " + injecter.getClass().getName() + " must has generic for " + BeanInjecter.class);
			map.put(supportedType, injecter);
		}
		log.debug("Inject type includes {}", map.keySet());
		for(ParameterInjecter<?,?> injecter:container.getComponents(ParameterInjecter.class)){
			Class<?> clazz=ResolvableType.forClass(AopUtils.getTargetClass(injecter)).as(ParameterInjecter.class).getGeneric(0).resolve();
			parameterInjecters.put(clazz, injecter);
		}
	}

	/**
	 * 判断指定的参数是否需要事务处理
	 * 
	 * @param clazz
	 *            参数类型
	 * @param annotations
	 *            参数的注解
	 * @return 需要返回true
	 */
	public boolean isNeetTransaction(Class<?> clazz, Object[] annotations) {
		if (AnnotationUtils.getAnnotationByMeta(annotations, ParameterInject.class) != null) {
			return false;
		}
		Class<?> type = clazz.isArray() ? clazz.getComponentType() : clazz;
		BeanInjecter<?> injecter = map.get(type);
		if (injecter != null) {
			return injecter.isNeedTransaction();
		}
		Object parameter = getParameter(clazz, annotations);
		if (parameter == null) {
			return false;
		} else if (parameter.getClass().isArray()) {
			for (Object obj : (Object[]) parameter) {
				if (containsTransactional(AopUtils.getTargetClass(obj))) {
					return true;
				}
			}
			return false;
		} else {
			Class<?> serviceType = AopUtils.getTargetClass(parameter);
			return containsTransactional(serviceType);
		}
	}

	/**
	 * 判断一个类是否包含事务注解
	 * 
	 * @param serviceType
	 *            服务类
	 * @return 包含事务返回true
	 */
	private boolean containsTransactional(Class<?> serviceType) {
		if (serviceType.getAnnotation(Transactional.class) != null) {
			return true;
		}
		for (Method method : serviceType.getMethods()) {
			if (method.getAnnotation(Transactional.class) != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 从applicationContext中获取作为参数的bean实例
	 * 
	 * @param clazz
	 *            参数类型
	 * @param annotations
	 * @return
	 */
	private Object getParameter(Class<?> clazz, Object[] annotations) {
		if (clazz.isArray()) {
			Map<String, ?> beans = applicationContext.getBeansOfType(clazz.getComponentType());
			return beans == null ? null : beans.values().toArray();
		}
		String beanName = getBeanName(annotations);
		if (beanName.length() > 0) {
			return applicationContext.getBean(beanName);
		}
		return applicationContext.getBean(clazz);
	}

	private String getBeanName(Object[] annotations) {
		String beanName = "";
		for (Object object : annotations) {
			if (Qualifier.class.equals(((Annotation) object).annotationType())) {
				Qualifier qualifier = (Qualifier) object;
				beanName = qualifier.value();
			}
		}
		return beanName;
	}

	/**
	 * 判断是否允许注入此参数
	 * 
	 * @param clazz
	 *            参数类型
	 * @param annotations
	 *            参数的注解
	 * @param method
	 *            如果是方法参数则为true，构造方法则为false
	 * @param errors 处理中的错误
	 * @return 允许则返回true
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean enableInject(Class<?> clazz, Object[] annotations, boolean method,Errors errors) {
		Annotation[] anns = AnnotationUtils.getAnnotationByMeta(annotations, ParameterInject.class);
		if (anns.length>1) {
			errors.add("inject.parameter", anns[0].toString(),anns[1].toString());
			return false;
		}else if(anns.length==1){
			ParameterInjecter injecter=parameterInjecters.get(anns[0].annotationType());
			List<String> keys=injecter.validate(anns[0],clazz);
			if(keys==null){
				return true;
			}
			for(String key:keys){
				errors.add(key, anns[0].toString());
			}
			return keys.isEmpty();
		}
		Class<?> type = clazz.isArray() ? clazz.getComponentType() : clazz;
		BeanInjecter<?> injecter = map.get(type);
		String beanName = getBeanName(annotations);
		if (injecter != null) {
			if (!method && injecter.isNeedTransaction()) {
				return false;
			}
			return beanName.length() > 0 ? injecter.containsBean(beanName) : injecter.containsBean();
		} else if (beanName.length() == 0) {
			return container.containsBean(clazz);
		} else if (applicationContext.containsBean(beanName)) {
			return clazz.isInstance(applicationContext.getBean(beanName));
		}
		return false;
	}

	/**
	 * 获取注入的构造方法参数值. <br>
	 * 
	 * @param constructor
	 *            方法
	 * @return 参数值
	 */
	public Object[] getInjectParameters(Constructor<?> constructor) {
		Object[] rs = new Object[constructor.getParameterCount()];
		int k = 0;
		for (Parameter parameter : constructor.getParameters()) {
			rs[k++] = getParameter(parameter);
		}
		return rs;
	}

	/**
	 * 获取注入的方法参数值. <br>
	 * 与{@link #release(Method, Object[])}成对使用
	 * 
	 * @param method
	 *            方法
	 * @return 参数值
	 */
	public Object[] getInjectParameters(Method method) {
		Object[] rs = new Object[method.getParameterCount()];
		int k = 0;
		for (Parameter parameter : method.getParameters()) {
			rs[k++] = getParameter(parameter);
		}
		return rs;
	}

	/**
	 * 释放资源. <br>
	 * 与{@link #getInjectParameters(Method)}成对使用
	 * 
	 * @param method
	 *            调用的方法
	 * @param args
	 *            方法参数
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void release(Method method, Object[] args) {
		int k = 0;
		for (Parameter parameter : method.getParameters()) {
			BeanInjecter injecter = map.get(parameter.getType());
			if (injecter != null) {
				injecter.afterUsed(args[k]);
			}
			k++;
		}
	}

	/**
	 * 获取注入的方法参数值. <br>
	 * 与{@link #release(Method, Method, Object[])}方法成对使用，如果源方法有同名同类型参数，则直接使用，否则从bean中查询
	 * 
	 * @param targetMethod
	 *            目标方法
	 * @param sourceMethod
	 *            源方法
	 * @param args
	 *            原方法的参数
	 * @return 方法参数值
	 */
	public Object[] getInjectParameters(Method targetMethod, Method sourceMethod, Object[] args) {
		Parameter[] parameters = targetMethod.getParameters();
		if (parameters.length == 0) {
			return EMPTY_ARRAY;
		}
		Map<String, Object> params = getParameterMap(sourceMethod, args);
		Object[] rs = new Object[parameters.length];
		String[] names = CtMethodUtils.getMethodParamNames(targetMethod);
		int k = 0;
		for (Parameter parameter : parameters) {
			String key = names[k] + "_" + parameter.getType().getName();
			rs[k] = params.containsKey(key) ? params.get(key) : getParameter(parameter);
			k++;
		}
		return rs;
	}

	/**
	 * 完成资源的释放. <br>
	 * 与方法{@link #getInjectParameters(Method, Method, Object[])}成对使用（在finally中调用此方法）。
	 * 
	 * @param targetMethod
	 *            目标方法
	 * @param sourceMethod
	 *            源方法
	 * @param args
	 *            目标方法的参数
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void release(Method targetMethod, Method sourceMethod, Object[] args) {
		Parameter[] parameters = targetMethod.getParameters();
		if (parameters.length == 0) {
			return;
		}
		Map<String, Object> params = getParameterMap(sourceMethod, args);
		String[] names = CtMethodUtils.getMethodParamNames(targetMethod);
		int k = 0;
		for (Parameter parameter : parameters) {
			String key = names[k] + "_" + parameter.getType().getName();
			if (params.containsKey(key)) {
				k++;
				continue;
			}
			BeanInjecter injecter = map.get(parameter.getType());
			if (injecter != null) {
				injecter.afterUsed(args[k]);
			}
			k++;
		}
	}

	/**
	 * 获取方法参数名与值的映射
	 * 
	 * @param method
	 *            方法
	 * @param args
	 *            参数值
	 * @return
	 */
	private Map<String, Object> getParameterMap(Method method, Object[] args) {
		Map<String, Object> map = new HashMap<>();
		int k = 0;
		Class<?>[] types = method.getParameterTypes();
		for (String name : CtMethodUtils.getMethodParamNames(method)) {
			map.put(name + "_" + types[k].getName(), args[k++]);
		}
		return map;
	}

	/**
	 * 获取参数值
	 * 
	 * @param parameter
	 * @return
	 */
	private Object getParameter(Parameter parameter) {
		Annotation[] annotations=AnnotationUtils.getAnnotationByMeta(parameter.getAnnotations(), ParameterInject.class);
		if (annotations.length==1) {
			return getInjectParameter(parameter.getType(), annotations[0]);
		}
		Qualifier q = parameter.getAnnotation(Qualifier.class);
		Class<?> parameterType = parameter.getType();
		BeanInjecter<?> ip = map.get(parameterType);
		if (ip != null) {
			return q == null ? ip.getBean() : ip.getBean(q.value());
		}
		if (q != null) {
			return applicationContext.getBean(q.value());
		} else if (parameterType.isArray()) {
			Class<?> type = parameterType.getComponentType();
			Collection<?> values = applicationContext.getBeansOfType(type).values();
			Object[] rs = (Object[]) Array.newInstance(type, values.size());
			return values.toArray(rs);
		} else {
			return applicationContext.getBean(parameterType);
		}
	}

	/**
	 * 获取注入参数
	 * @param type 参数类型
	 * @param annotation
	 *            参数的所有注解
	 * @return 参数值
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getInjectParameter(Class<?> type,Annotation annotation) {
		ParameterInjecter injecter = parameterInjecters.get(annotation.annotationType());
		return injecter.inject(annotation,type);
	}

}
