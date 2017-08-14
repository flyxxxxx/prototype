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

import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * 类工厂. <br>
 * 用于加载、创建及调用.
 * @author flyxxxxx@163.com
 *
 */
public interface ClassFactory {
	/**
	 * 加载指定类
	 * @param className  类名
	 * @return 类名对应的类
	 */
	Class<?> loadClass(String className);
	/**
	 * 构造一个新的public类. <br>
	 * 注意构造出的类调用getPackage()返回的是null
	 * @param className 类名
	 * @return 类构建器
	 */
	ClassBuilder newClass(String className);
	/**
	 * 
	 * 构造一个新的public类. <br>
	 * 注意构造出的类调用getPackage()返回的是null
	 * @param className 类名
	 * @param superClass 父类
	 * @param interfaceTypes 实现的接口
	 * @return 类构建器
	 */
	ClassBuilder newClass(String className,Class<?> superClass,Class<?>... interfaceTypes);
	
	/**
	 * 新的接口构建器
	 * @param interfaceName 接口类名
	 * @return 接口构建器
	 */
	InterfaceBuilder newInterface(String interfaceName);
	
	/**
	 * bean注册器
	 * @return bean注册器
	 */
	BeanDefinitionRegistry getRegistry();
}
