package org.prototype.business;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Executor
@interface Concurrent {

	/**
	 * 是否中断操作
	 * 
	 * @return 是否中断操作
	 */
	boolean interrupt() default true;

	/**
	 * 超时时间（毫秒），默认10秒
	 * 
	 * @return 超时时间
	 */
	long timeout() default 10 * 1000l;
}
