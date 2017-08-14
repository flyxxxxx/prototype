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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 方法执行链. <br>
 * @author flyxxxxx@163.com
 *
 */
public interface MethodChain {

	/**
	 * 方法调用的目标对象
	 * @return 目标对象
	 */
	Object getTarget();
	
	/**
	 * 调用的方法(可能是父类的方法，此方法可用于反射，不可直接调用)
	 * @return 调用的方法
	 */
	Method getMethod();
	
	/**
	 * 获取方法返回值类型的泛型
	 * @return 方法返回值类型的泛型
	 */
	Type getGenericReturnType();
	
	/**
	 * 方法调用
	 * @param args 方法参数
	 * @return 方法返回值
	 * @throws Exception 异常
	 */
	Object doFilter(Object[] args) throws Exception;

	/**
	 * 查找同名的多个方法(优先使用缓存)
	 * @param methodName 方法名
	 * @return 方法列表
	 */
	List<Method> findMethods(String methodName) ;

	/**
	 * 查找唯一名称的方法（忽略参数不同）
	 * @param methodName 方法名称
	 * @param required 是否必须，如果是并且没有找到指定方法，则抛出异常
	 * @return 方法调用接口
	 */
	Method findUniqueMethod(String methodName,boolean required);

	/**
	 * 查询匹配的重载方法(优先从缓存查询)
	 * 
	 * @param methodName 方法名
	 * @param parameterType 方法唯一参数
	 * @return 匹配的方法
	 */
	Method findOverloadMethod(String methodName,Class<?> parameterType);
	
}
