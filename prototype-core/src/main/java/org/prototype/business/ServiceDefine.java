package org.prototype.business;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 控制器方法
 * 
 * @author flyxxxxx@163.com
 *
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServiceDefine {

	/**
	 * 服务的文档
	 * 
	 * @return 服务的文档
	 */
	String value();
	
	/**
	 * 资源访问路径，也可通过RequestMapping定义
	 * @return 资源访问路径
	 */
	String url() default "";

	/**
	 * 此服务接口的用法说明
	 * 
	 * @return 用法说明
	 */
	String hint() default "";

	/**
	 * 接口版本号
	 * 
	 * @return 接口版本号
	 */
	String version() default "1.0";

	/**
	 * 服务名称，默认值时，当前业务类定义为服务的一个方法，名称为业务类名（首字母小写）
	 * 
	 * @return 服务名称
	 */
	String name() default "";
	
	/**
	 * 作者
	 * @return 作者
	 */
	String author() default "";
	
	/**
	 * 服务是否只读事务
	 * @return 默认为只读
	 */
	boolean readOnly() default true;
	
	/**
	 * 标识此服务对外公开（默认为true）
	 * @return 标识此服务对外公开
	 */
	boolean open() default true;
}
