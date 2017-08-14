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
package org.prototype.core;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.Order;

/**
 * 将多个接口的实现进行排序. <br>
 * <pre>
 * 对于指定接口的多个实现，优先按Spring注解Order的值进行排序；
 * 其次，如果没有指定Order注解，按实现接口时的接口泛型（必须是一个注解的类型）值，以注解在方法（或类或成员变量）上注解的先后顺序排序
 * 如果也没有接口泛型，则按类名排序.
 * 
 * </pre>
 * @author flyxxxxx@163.com
 *
 * @param <T> 接口类型
 */
public class AnnotationsChainComparator<T> implements Comparator<T> {

	/**
	 * 注解类型列表
	 */
	private List<Class<?>> annotationList = new ArrayList<>();
	
	/**
	 * 接口类型
	 */
	private Class<T> interfaceType;

	/**
	 * 要比较的接口类型
	 * 
	 * @param interfaceType
	 *            接口类型
	 * @param annotations
	 *            方法（或类或成员变量）上的注解
	 */
	public AnnotationsChainComparator(Class<T> interfaceType, Annotation[] annotations) {
		this.interfaceType = interfaceType;
		for (Annotation annotation : annotations) {
			annotationList.add(annotation.annotationType());
		}
	}

	/**
	 * 优先按Order的值排序
	 */
	@Override
	public int compare(T f1, T f2) {
		Class<?> t1 = f1.getClass();
		Class<?> t2 = f2.getClass();
		Order order1 = t1.getAnnotation(Order.class);
		Order order2 = t2.getAnnotation(Order.class);
		if (order1 == null && order2 == null) {
			return compare(t1, t2);
		} else if (order1 == null) {
			return 1;
		} else if (order2 == null) {
			return -1;
		}
		return order1.value() - order2.value();
	}

	/**
	 * 其次按接口实现上的泛型排序
	 * 
	 * @param t1 类1
	 * @param t2 类2
	 * @return 比较值
	 */
	private int compare(Class<?> t1, Class<?> t2) {
		Class<?> class1 = ResolvableType.forClass(t1).as(interfaceType).getGeneric(0).resolve();
		Class<?> class2 = ResolvableType.forClass(t2).as(interfaceType).getGeneric(0).resolve();
		int k1 = class1 == null ? -1 : annotationList.indexOf(class1);
		int k2 = class2 == null ? -1 : annotationList.indexOf(class2);
		if (k1 == -1 && k2 == -1) {
			return t1.getName().compareTo(t2.getName());
		} else if (k1 == -1) {
			return 1;
		} else if (k2 == -1) {
			return -1;
		}
		return k1 - k2;
	}

}