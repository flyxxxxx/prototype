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
 * 方法责任链. <br>
 * <pre>
 * 可将多个方法的调用形成一个责任链，当多个方法(包括加此注解的方法)中任意一个方法返回false时，将不再执行之后的所有方法.
 * 1、加Chain注解的方法及Chain#value中指定的方法，返回值必须是boolean或void。
 * 2、Chain#value中指定的每个方法，必须在当前类中有并且仅有一个(忽略方法参数的不同).
 * 3、Chain#value中指定的每个方法允许有以下几种类型的参数（并按此优先顺序）:
 * 	  a)、可以是与加Chain注解的方法的参数，只要相同类型和名称（顺序不做要求）.
	  b)、能通过接口{@link org.prototype.core.BeanInjecter}注入。
	  c)、来源于Spring applicationContext中的bean.
 * 例：
 *  &#064;Chain({"a1","a2"})
	public void business(){
	}
	boolean a1(){
	}
	void a2(FooService service){
	}
	相当于代码：
	public void business(){
	  if(!a1()){
	    return;
	  }
	  a2(..);
	  //原business方法的代码
	}
 * </pre>
 * @author flyxxxxx@163.com
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Chain {

	/**
	 * 责任链依次调用的方法
	 * 
	 * @return 依次调用的方法
	 */
	String[] value();

	/**
	 * 是否在当前方法执行完成后再执行责任链
	 * 
	 * @return 默认为false
	 */
	boolean after() default false;

	/**
	 * 是否动态决定责任链接方法（允许方法缺失）
	 * 
	 * @return 默认为false
	 */
	boolean dynamic() default false;
}
