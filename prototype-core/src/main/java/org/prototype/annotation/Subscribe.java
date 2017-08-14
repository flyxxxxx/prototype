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

import org.prototype.core.Prototype;


/**
 * 消息订阅. <br>
 * 消息处理方法采用一个或多个重载的方法处理不同类型的消息内容。方法必须只有一个参数，并且返回值类型为void.
 * 注意，同一条数据有且仅有一个订阅者能进行处理. 
 * @author flyxxxxx@163.com
 *
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Prototype
public @interface Subscribe {
	
	/**
	 * 消息类型
	 * @return 消息类型
	 */
	String[] type();
	/**
	 * 包括哪些来源（默认全部）(支持作为消息来源字符串的前缀)
	 * @return 包括哪些来源
	 */
	String[] incloudSource() default {};
	/**
	 * 排除哪些来源(支持作为消息来源字符串的前缀)
	 * @return 排除哪些来源
	 */
	String[] excloudSource() default {};
	
	/**
	 * 消息处理方法名（可重载）
	 * @return 消息处理方法名
	 */
	String onMessage() default "onMessage";
	/**
	 * 是否在异步线程中处理(默认为true)
	 * @return 是否在异步线程中处理
	 */
	boolean async() default true;
}
