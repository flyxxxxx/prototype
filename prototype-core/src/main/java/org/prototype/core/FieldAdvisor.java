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
 * 仅对静态成员变量进行访问. <br>
 * 用于在加载类之后对静态成员变量的值进行修改或其它处理. 
 * @author flyxxxxx@163.com
 *
 */
@FunctionalInterface
public interface FieldAdvisor {

	/**
	 * 类加载之后对静态成员变量进行处理
	 * @param accessor 成员变量访问接口
	 * @param errors 处理中的错误
	 */
	void afterLoad(FieldInvoker accessor, Errors errors);
}
