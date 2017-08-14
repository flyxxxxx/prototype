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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识一个类是原型类. <br>
 * 也可用于另一注解类，如{@link org.prototype.business.BusinessDefine}.
 * @author flyxxxxx@163.com
 *
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Prototype {
	/**
	 * 是否注册spring bean(默认不注册)
	 * @return 需要注册返回true
	 */
	boolean register() default false;
	/**
	 * 处理过程中需要忽略的注解
	 * @return 需要忽略的注解
	 */
	Class<?>[] ignore() default {};
}
