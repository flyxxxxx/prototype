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
 * 成员变量（仅静态）调用接口. <br>
 * 在完成类的修改及类的加载之后调用此接口的实现. 框架提供Srping Value注解的处理.
 * @see org.prototype.springframework.ValueFieldAdvisor
 * @author flyxxxxx@163.com
 *
 */
public interface FieldInvoker extends AnnotatedAccessor{
	
	/**
	 * 获得字段名
	 * @return 字段名
	 */
	String getName();
	/**
	 * 获得字段类型
	 * @return 字段类型
	 */
	Class<?> getType();

	/**
	 * 修改成员变量的值
	 * @param value 修改后的值
	 */
	void setValue(Object value);
}
