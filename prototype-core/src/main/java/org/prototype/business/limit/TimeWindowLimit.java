package org.prototype.business.limit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import org.prototype.business.Executor;

/**
 * 时间窗口内调用次数限制。 <br>
 * 
 * <pre>
 * 使用此注解需要加入guava依赖： 
	maven: 
	&lt;dependency&gt;
		&lt;groupId&gt;com.google.guava&lt;/groupId&gt;
		&lt;artifactId&gt;guava&lt;/artifactId&gt;
	&lt;/dependency&gt;
 * </pre>
 * 注意，此次数可能会有少量误差
 * @author flyxxxxx@163.com
 *
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Executor
public @interface TimeWindowLimit {
	/**
	 * 单位时间内调用的次数
	 * 
	 * @return 单位时间内调用的次数
	 */
	int value();
	
	/**
	 * 持续的时间（默认为1秒）
	 * @return 持续的时间
	 */
	int duration() default 1;

	/**
	 * 时间单位（默认为秒）
	 * 
	 * @return 时间单位
	 */
	TimeUnit unit() default TimeUnit.SECONDS;
}
