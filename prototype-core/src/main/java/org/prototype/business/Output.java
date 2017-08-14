package org.prototype.business;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识类的一个成员变量可作为输出结果. <br>
 * 
 * @author lj
 *
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Output {

	/**
	 * 输出的数据类型
	 * 
	 * @return 输出的数据类型
	 */
	Class<?> type() default void.class;

	
	/**
	 * POJO对象的描述
	 * @return 复合属性的描述
	 */
	String desc() default "";
	
	/**
	 * POJO对象给客户端的提示
	 * @return 给客户端的提示
	 */
	String hint() default "";
	
	/**
	 * 是否必须(POJO对象)
	 * @return 是否必须
	 */
	boolean required() default true;
	/**
	 * 属性列表
	 * @return 属性列表
	 */
	Prop[] value();

}
