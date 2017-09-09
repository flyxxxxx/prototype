package org.prototype.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启使用Spring mvc的RequestBody方式. <br>
 * 如果业务类（或启动类）未指定此注解，仅有复杂参数（对象与集合的复合类型参数）才开启RequestBody作为参数。
 * 
 * @author lijin
 *
 */

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@interface EnableRequestBody {
	/**
	 * 是否只支持RequestBody方式
	 * @return 默认为false
	 */
	//TODO 暂未实现
	boolean only() default false;
}
