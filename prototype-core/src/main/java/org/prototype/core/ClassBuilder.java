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

/**
 * 类构建器. <br>
 * 用于构建新的类. 在方法{@link ClassAdvisor#beforeLoad(ClassBuilder, Errors)}中可以使用此接口.
 * 
 * @author flyxxxxx@163.com
 *
 */
public interface ClassBuilder extends AnnotatedBuilder {

	/**
	 * 获得对父类的访问
	 * @return 父类的构建器（可能为null或Object类的构建器，实际上是不可以进行修改的）
	 */
	ClassBuilder getSuperClassBuilder();

	/**
	 * 获取类名
	 * @return 类名
	 */
	String getName();

	/**
	 * 列出所有构造方法的构建器
	 * @param onlyPublic 是否仅列出公开的构造方法
	 * @return 构造方法的构建器
	 */
	ConstructorBuilder[] listConstructors(boolean onlyPublic);

	/**
	 * 查询类定义的指定名称的方法（忽略参数不同），如果当前类没有，则在父类中查询(仅查询子类可见的方法).
	 * @param methodName 方法名
	 * @return 多个同名方法的构建器
	 */
	MethodBuilder[] findMethods(String methodName);
	
	/**
	 * 查询所有的构造方法构建器(仅公开的构造方法)
	 * @return 所有的构造方法构建器
	 */
	ConstructorBuilder[] findConstructors();

	/**
	 * 查询唯一的方法构建器。 <br>
	 * 如果当前类没有，则到父类中查询(仅查询子类可见的方法).
	 * @param methodName 方法名
	 * @param errors 错误（当有多个方法时将在此写入异常信息）
	 * @param annotationClass 哪个注解要求查询此方法（可为null）
	 * @return 方法构建器
	 */
	MethodBuilder findUniqueMethod(String methodName,Errors errors,Class<?> annotationClass);

	/**
	 * 查找当前类声明的所有方法中有指定注解的方法
	 * @param annotationClass 注解类
	 * @return 多个方法的构建器
	 */
	MethodBuilder[] findDeclaredMethods(Class<? extends Annotation> annotationClass);

	/**
	 * 查找符合指定参数的方法（仅当前类）
	 * @param methodName 方法名
	 * @param parameterTypes 方法参数类型
	 * @return 完全匹配的方法
	 */
	MethodBuilder findMethod(String methodName,Class<?>... parameterTypes);

	/**
	 * 是否有无参数的构造方法
	 * @return 有无参数构造方法返回true
	 */
	//boolean hasEmptyConstruct();
	/**
	 * 判断是否有唯一的公开可注入参数的构造方法
	 * @param errors 错误
	 * @return 允许遇返回true
	 */
	 boolean enableConstructInject(Errors errors) ;
	
	/**
	 * 构造新的方法
	 * @param code 方法代码
	 * @return 方法构建器
	 */
	MethodBuilder newMethod(String code);

	/**
	 * 构造新的方法
	 * @param modifiers 方法的访问域
	 * @param returnType 方法的返回类型
	 * @param name 方法名
	 * @param parameterTypes 参数类型
	 * @param throwableTypes 抛出的异常
	 * @param filter 方法调用代理
	 * @return 方法构建器
	 */
	MethodBuilder newMethod(int modifiers,Class<?> returnType,String name,Class<?>[] parameterTypes,Class<? extends Throwable>[] throwableTypes,MethodFilter<?>... filter);

	/**
	 * 构造新的方法
	 * @param modifiers 方法的访问域
	 * @param returnType 方法的返回类型
	 * @param name 方法名
	 * @param parameterTypes 参数类型
	 * @param throwableTypes 抛出的异常
	 * @param bodySrc 方法代码
	 * @return 方法构建器
	 */
	MethodBuilder newMethod(int modifiers,Class<?> returnType,String name,Class<?>[] parameterTypes,Class<? extends Throwable>[] throwableTypes,String bodySrc);

	/**
	 * 添加新的成员变量
	 * @param modifiers 成员变量的访问域
	 * @param name 成员变量名
	 * @param type 成员变量类型
	 * @param setAndGet 是否创建set和get方法
	 * @return 成员变量构建器
	 */
	FieldBuilder newField(int modifiers,String name,Class<?> type,boolean setAndGet);
	/**
	 * 添加新的成员变量
	 * @param modifiers 成员变量的访问域
	 * @param name 成员变量名
	 * @param type 成员变量类型
	 * @param typeArguments 泛型参数
	 * @param setAndGet 是否创建set和get方法
	 * @return 成员变量构建器
	 */
	FieldBuilder newField(int modifiers,String name,Class<?> type,Class<?>[] typeArguments,boolean setAndGet);

	/**
	 * 完成类的创建. <br>创建完成之后不可再修改类、方法、成员变量及其注解
	 * @return 新创建的类
	 */
	Class<?> create();
}
