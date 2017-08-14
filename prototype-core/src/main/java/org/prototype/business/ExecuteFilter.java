package org.prototype.business;

import java.lang.annotation.Annotation;

/**
 * 业务类执行过滤器. <br>
 * 业务类在执行时，如果有某个注解类，并且此接口的实现类指定了同样的注解作为此接口的范型，则接口实现类对业务类的执行生效，否则不生效。如果接口的实现类未指定范型，则对所有业务类的执行生效。
 * @author flyxxxxx@163.com
 *
 * @param <T>
 *            支持的注解类
 */
@FunctionalInterface
public interface ExecuteFilter<T extends Annotation> {
	/**
	 * 优先级：请求资源
	 */
	int REQUEST = -20000;
	/**
	 * 优先级：线程池运行
	 */
	int POOL = -10000;
	/**
	 * 优先级：初始化
	 */
	int INIT = 0;
	
	/**
	 * 检查缓存
	 */
	int CHECK_CACHE=10000;
	/**
	 * 优先级：获取结果
	 */
	int RESULT = 20000;

	/**
	 * 业务执行过滤
	 * @param chain 业务执行链
	 * @throws Exception 异常
	 */
	void doFilter(ExecuteChain chain) throws Exception;
}
