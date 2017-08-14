package org.prototype.sql;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 数据库批处理. <br>
 * 批处理大小由MvcConfiguration的属性定义(默认为100). <br>
 * 
 * <pre>
 * 例：
 * 
 * </pre>
 * 
 * @author lj
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Batch {
	/**
	 * 执行哪些批处理方法（有BatchSql注解的）
	 * 
	 * @return 批处理方法
	 */
	String[] value();

	/**
	 * 是在加此注解的方法之前执行还是之后
	 * 
	 * @return 默认之前执行
	 */
	boolean after() default false;
}
