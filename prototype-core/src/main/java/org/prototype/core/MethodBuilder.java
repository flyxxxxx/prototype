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

/**
 * 方法构建接口
 * @author flyxxxxx@163.com
 *
 */
public interface MethodBuilder extends AnnotatedBuilder{
	
	/**
	 * 获得类构建器
	 * @return 类构建器
	 */
	ClassBuilder getClassBuilder();
	
	/**
	 * 获取方法参数注解的构建器
	 * @return 方法参数注解的构建器
	 */
	ParameterAnnotationsBuilder getParameterAnnotationsBuilder();
	
	/**
	 * 获取方法名
	 * @return 方法名
	 */
	String getName();
	
	/**
	 * 获取方法返回值类型
	 * @return 方法返回值类型
	 */
	String getReturnType();
	
	/**
	 * 获取访问域值
	 * @return 访问域值
	 */
	int getModifiers();
	/**
	 * 获取方法参数数量
	 * @return 方法参数数量
	 */
	Class<?>[] getParameterTypes();
	
	/**
	 * 获取方法抛出的异常类型
	 * @return 异常类型
	 */
	Class<?>[] getExceptionTypes();

	/**
	 * 是否允许从指定的方法注入方法参数
	 * @param builder 调用当前构建器方法的方法构建器
	 * @param errors 错误
	 * @return 允许则返回true
	 */
	boolean enableInjectFrom(MethodBuilder builder,Errors errors);
	/**
	 * 是否允许从注入方法参数
	 * @param errors 错误
	 * @return 允许则返回true
	 */
	boolean enableInject(Errors errors);
	
	/**
	 * 方法是否需要事务处理
	 * @param errors 错误
	 * @return 需要则返回true
	 */
	boolean isNeedTransaction(Errors errors);
	/**
	 * 完成方法创建(新创建的方法或添加了注解)
	 */
	void create();
}
