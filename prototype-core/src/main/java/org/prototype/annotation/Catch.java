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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于方法的异常处理. <br>
 * 
 * <pre>
 * 定义在一个方法上，用于捕获此方法可能抛出的异常。
 * 1、异常处理方法可以有多个，分别处理不同类型的异常。
 * 2、异常处理方法的名称是原方法名称加上此注解{@link #suffix()}的值（默认为Exception）。
 * 3、异常处理方法的参数只允许有一个，类型是各种不同的异常类型。
 * 4、异常处理方法的返回值类型必须与被捕获异常的方法返回值类型一致。
 * 5、多个异常处理方法最终只有一个方法会接收被捕获异常的方法抛出的异常。
 * 6、父子类关系时，子类如果有匹配的异常处理方法（包括父类中对子类可见的方法），则以子类的方法处理.
 * 7、异常注解的优先级低于异步方法，高于事务处理.
 * &#064;Prototype
public class CatchBusiness {
	private int value;
	&#064;Catch//加了注解后，至少要有一个异常处理的方法
	public void execute() {
	    if(...){
			throw new java.lang.RuntimeException();
		}
	}
	void executeException(UnsupportedOperationException exception) {//方法返回值类型必须与execute一致
		value = -3;//此方法将接收execute方法抛出的异常UnsupportedOperationException
	}
	void executeException(Exception exception) {
		value = -1;//所有execute中抛出的异常，如果没有被其它方法处理，将转到此方法处理
	}
	void executeException(RuntimeException exception) {
		value = -2;//此方法将接收execute方法抛出的RuntimeException异常或RuntimeException的子类实例，接收子类时仅当没有其它合适的异常处理方法时生效
	}
}
 * </pre>
 * 注意：Catch与Async/OverloadAsync注解同时使用时，低于它们的优先级，和HystrixCommand优先级相同（实际执行顺序取决于两个注解的顺序），高于事务处理注解的优先级。
 * @author lj
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Catch {
	/**
	 * 异常处理方法名称后缀
	 * 
	 * @return 异常处理方法名称后缀
	 */
	String suffix() default "Exception";

}
