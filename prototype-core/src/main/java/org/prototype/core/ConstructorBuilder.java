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
 * 构造方法构建器.
 * @author lj
 *
 */
public interface ConstructorBuilder extends AnnotatedBuilder{

	
	/**
	 * 获取方法参数注解的构建器
	 * @return 方法参数注解的构建器
	 */
	ParameterAnnotationsBuilder getParameterAnnotationsBuilder();
	/**
	 * 获得类构建器
	 * @return 类构建器
	 */
	ClassBuilder getClassBuilder();
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
	 * 完成方法创建(新创建的方法或添加了注解)
	 */
	void create();
}
