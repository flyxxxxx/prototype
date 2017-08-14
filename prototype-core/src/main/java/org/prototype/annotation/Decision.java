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
 * 决策. <br>
 * <pre>
 * 根据方法返回值（整数或布尔或方法名），决定调用另外的方法. 
 * 1、加入此注解的方法返回值类型只能是boolean/int/String类型，
 * 2、返回值类型为boolean时，根据方法返回值调用注解{@link #value()}中的第1个方法或第2个方法
 * 3、返回值类型为int时，根据返回结果调用注解{@link #value()}中的第N个方法
 * 4、返回值类型为String时，根据返回结果匹配注解{@link #value()}中的名字对应的方法
 * 5、在以上条件都没有匹配成功时，将调用defaultValue对应的方法（如果有定义默认值的话），没有定义defaultValue将不调用任何方法
 * 6、决策调用的方法返回值类型只能是void，允许有任何Spring中的bean或能够通过接口{@link org.prototype.core.BeanInjecter}注入的对象
 * </pre>
 * @author lj
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Decision {
	/**
	 * 决策后转向的方法名列表
	 * @return 方法名列表
	 */
	String[] value();
	/**
	 * 无法决策时调用的方法名（默认值时不调用任何方法）
	 * @return 无法决策时调用的方法名
	 */
	String defaultValue() default "";
}
