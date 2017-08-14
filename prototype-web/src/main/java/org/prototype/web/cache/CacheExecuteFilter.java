package org.prototype.web.cache;

import javax.annotation.Resource;

import org.prototype.business.ExecuteChain;
import org.prototype.core.ConditionalHasClass;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;

import org.prototype.business.*;

public class CacheExecuteFilter implements ExecuteFilter<Cacheable> {

	@Resource
	private ApplicationContext context;

	@Override
	public void doFilter(ExecuteChain chain) throws Exception {
		Cacheable cacheable = chain.getType().getAnnotation(Cacheable.class);
		CacheManager manager = findCacheManager(cacheable.cacheManager());
		String key = chain.getType().getName();
		manager.getCache("").get(key).get();
	}

	private CacheManager findCacheManager(String name) {
		if (name.length() == 0) {
			return context.getBean(CacheManager.class);
		}
		return (CacheManager) context.getBean(name);
	}

}
