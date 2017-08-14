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
 * 成员变量构建器. <br>
 * @author flyxxxxx@163.com
 *
 */
public interface FieldBuilder extends AnnotatedBuilder{
	
	/**
	 * 获取成员变量名
	 * @return 成员变量名
	 */
	String getName();
	
	/**
	 * 获取成员变量的类型
	 * @return 成员变量的类型
	 */
	String getType();
	
	/**
	 * 设定成员变量签名（范型）
	 * @param typeArguments 成员变量签名
	 */
	void setSignature(Class<?>...typeArguments);
	/**
	 * 完成成员变量的添加（之后不可再添加注解）
	 */
	void create();

	/**
	 * 获得类构建器
	 * @return 类构建器
	 */
	ClassBuilder getClassBuilder();
}
