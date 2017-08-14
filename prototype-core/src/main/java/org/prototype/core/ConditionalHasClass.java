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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;

/**
 * 类路径中是否有指定的类条件判断注解. <br>
 * 用于在没有spring boot环境时，对当前是否有指定的类进行判断，以在类存在时才注入相应的bean.
 * @author flyxxxxx@163.com
 *
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(HasClassCondition.class)
public @interface ConditionalHasClass {

	/**
	 * 需要判断是否存在的类的列表
	 * @return 需要判断是否存在的类的列表
	 */
	Class<?>[] value() default {};
	
	/**
	 * 需要判断是否存在的类名
	 * @return 类名
	 */
	String[] name() default {};
}
