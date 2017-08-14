package org.prototype.business;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 性能过滤. <br>
 * 此注解可在业务类或业务基类上定义. 所有性能数据均以Message形式记录，也就是需要定义一个业务类并使用Subscribe注解。<br>
 * 消息类型为{@link PerformanceData#TYPE}，数据类型为{@link Performance}.
 * @author lj
 *
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Executor
public @interface Performance {

	long value() default 0; //最小性能值
	
}
