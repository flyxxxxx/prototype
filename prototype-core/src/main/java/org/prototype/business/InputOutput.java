package org.prototype.business;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标明类的一个成员变量可同时作为输入输出或是复合对象输入或复合对象输出
 * @author lj
 *
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface InputOutput {

	/**
	 * 输入参数
	 * 
	 * @return 输入参数
	 */
	Input[] input() default {};

	/**
	 * 输出结果
	 * 
	 * @return 输出结果
	 */
	Output[] output() default {};
}
