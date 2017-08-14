package org.prototype.business.limit;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.prototype.business.ExecuteFilter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import lombok.extern.slf4j.Slf4j;

/**
 * 时间窗口限制实现. <br>
 * 
 * @author flyxxxxx@163.com
 *
 */
@Component
@Order(ExecuteFilter.REQUEST)
@Slf4j
public class TimeWindowLimitExecuteFilter
		extends AbstractLimitExecuteFilter<TimeWindowLimit, LoadingCache<Long, AtomicInteger>> {

	@Override
	protected boolean acquire(TimeWindowLimit limit, LoadingCache<Long, AtomicInteger> limiter) {
		try {
			AtomicInteger value = limiter.get(System.currentTimeMillis() / 1000);
			if (value.get() < limit.value()) {
				value.incrementAndGet();
				return true;
			}
		} catch (ExecutionException e) {
			log.warn("LoadingCache error", e);
			return false;
		}
		return false;
	}

	@Override
	protected void countdown(LoadingCache<Long, AtomicInteger> limiter) {
		// do nothing
	}

	@Override
	protected LoadingCache<Long, AtomicInteger> createLimiter(TimeWindowLimit limit) {
		return CacheBuilder.newBuilder().expireAfterAccess(limit.duration(), limit.unit())
				.build(new AtomicIntegerCacheLoader());
	}

	private static class AtomicIntegerCacheLoader extends CacheLoader<Long, AtomicInteger> {

		@Override
		public AtomicInteger load(Long key) throws Exception {
			return new AtomicInteger(0);
		}

	}

}
