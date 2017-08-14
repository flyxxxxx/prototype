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
package org.prototype.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 多个同名不同参数的异步方法同时执行.
 * <pre>
 * 方法定义此注解后，多个同名方法（不同参数）将异步执行.
 * 1、after属性决定异步先处理还是后处理。
 * 2、每个异步执行的方法允许有参数，但必须全部来源于Spring applicationContext或是能通过接口{@link org.prototype.core.BeanInjecter}注入。
 * 3、允许结注解指定一线程池的名称（executor属性值），如果未指定将使用applicationContext中默认的线程池，如果没有默认线程池，将创建新线程执行方法.
 * 4、如果有父子类中都定义了同名的异步执行的方法，只执行子类中的（包括父类中对子类可见的方法）
 * </pre>
 * @author flyxxxxx@163.com
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OverloadAsync {
	/**
	 * 异步执行的方法名
	 * @return 异步执行的方法名
	 */
	String[] value();
	/**
	 * 执行用的线程池名称(使用默认线程池)
	 * @return 执行用的线程池名称
	 */
	String executor() default "";
	
	/**
	 * 当前方法之前执行还是之后。
	 * @return 默认为之前执行
	 */
	boolean after() default false;
}
