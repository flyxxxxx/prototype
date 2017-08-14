package org.prototype.business;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 业务方法定义. <br>
 * @author lj
 *
 */
@Target({ ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface BusinessMethod {
	
	/**
	 * 业务方法名
	 * @return 业务方法名
	 */
	String value();
	/**
	 * 业务方法是否可重载(支持同名方法不同参数),只有异步方法才允许此属性为true
	 * @return 是否可重载，默认false
	 */
	boolean overload() default false;
	/**
	 * 业务方法是否支持事务
	 * @return 业务方法是否支持事务，默认true
	 */
	boolean transaction() default true;
	
	/**
	 * 业务方法是否只支持只读事务
	 * @return 业务方法是否只支持只读事务，默认false
	 */
	boolean readOnly() default false;
}
