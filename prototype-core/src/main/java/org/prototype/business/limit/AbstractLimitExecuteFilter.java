package org.prototype.business.limit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.prototype.business.ExecuteChain;
import org.prototype.business.ExecuteFilter;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

/**
 * 访问限制的抽象实现
 * 
 * @author flyxxxxx@163.com
 *
 * @param <T>
 *            注解类(必须)
 * @param <L>
 *            限制器(必须)
 */
public abstract class AbstractLimitExecuteFilter<T extends Annotation, L> implements ExecuteFilter<T> {

	/**
	 * 注解与限流实现的映射
	 */
	private Map<T, L> limiters = new ConcurrentHashMap<>();
	
	private Class<T> type;
	
	@SuppressWarnings("unchecked")
	public AbstractLimitExecuteFilter(){
		Type superType=getClass().getGenericSuperclass();
		Assert.notNull(superType);
		type=(Class<T>) ResolvableType.forClass(getClass()).as(ExecuteFilter.class).getGeneric(0).resolve();
	}

	@Override
	public final void doFilter(ExecuteChain chain) throws Exception {
		T limit = chain.getAnnotation(type);
		L limiter = getLimiter(limit);
		try {
			if (acquire(limit, limiter)) {
				chain.doChain();
			} else {
				chain.setResultType(ExecuteChain.REJECT);
			}
		} finally {
			countdown(limiter);
		}
	}

	/**
	 * 请求令牌
	 * @param limit 限制注解
	 * @param limiter 限制器实现
	 * @return 是否请求成功（注意不要一直等待）
	 */
	protected abstract boolean acquire( T limit,L limiter);

	/**
	 * 限流完成，计数减少
	 * @param limiter 限制器实现
	 */
	protected abstract void countdown(L limiter);

	/**
	 * 从缓存查找限制器实现，没有则创建一个
	 * @param limit 限制注解
	 * @return 限制器实现
	 */
	private L getLimiter(T limit) {
		L limiter = limiters.get(limit);
		if (limiter == null) {
			synchronized (limit) {
				limiter = createLimiter(limit);
				limiters.put(limit, limiter);
			}
		}
		return limiter;
	}

	/**
	 * 创建新的限制器实现
	 * @param limit 限制注解
	 * @return 限制器实现
	 */
	protected abstract  L createLimiter(T limit);

}
