package org.prototype.business.limit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import org.prototype.business.Executor;

/**
 * 访问速率限制。 <br>
 * 可平滑限流接口的请求数.
 * <pre>
 * 使用此注解需要加入guava依赖： 
	maven: 
	&lt;dependency&gt;
		&lt;groupId&gt;com.google.guava&lt;/groupId&gt;
		&lt;artifactId&gt;guava&lt;/artifactId&gt;
	&lt;/dependency&gt;
 * </pre>
 * 
 * @author flyxxxxx@163.com
 *
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Executor
public @interface RateLimit {
	
	/**
	 * 每秒新增的令牌数
	 * @return 令牌数
	 */
	int value();
	/**
	 * 从冷启动速率过度到平均速率的时间间隔(默认为0不使用)
	 * @return 从冷启动速率过度到平均速率的时间间隔
	 */
	int warmupPeriod() default 0;
	
	/**
	 * 获取令牌之前的最大等待时间，默认不等待（注意默认时间单位）
	 * @return 获取令牌之前的最大等待时间
	 */
	int waitTimeout() default 0;
	/**
	 * 时间单位（默认为豪秒）
	 * 
	 * @return 时间单位
	 */
	TimeUnit unit() default TimeUnit.MILLISECONDS;
}
