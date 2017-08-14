package org.prototype.business.limit;

import org.prototype.business.ExecuteFilter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.RateLimiter;

/**
 * 速率限制执行过滤实现. <br>
 * @author flyxxxxx@163.com
 *
 */
@Component
@Order(ExecuteFilter.REQUEST)
public class RateLimitExecuteFilter extends AbstractLimitExecuteFilter<RateLimit,RateLimiter> {

	@Override
	protected boolean acquire(RateLimit limit, RateLimiter limiter) {
		if (limit.waitTimeout() > 0) {
			return limiter.tryAcquire(limit.waitTimeout(), limit.unit());
		} else {
			return limiter.tryAcquire();
		}
	}

	@Override
	protected void countdown(RateLimiter limiter) {
		// do nothing
	}

	@Override
	protected RateLimiter createLimiter(RateLimit limit) {
		if (limit.warmupPeriod() > 0) {
			return RateLimiter.create(limit.value(), limit.warmupPeriod(), limit.unit());
		} else {
			return RateLimiter.create(limit.value());
		}
	}
	
	public static void main(String[] args){
		new RateLimitExecuteFilter();
	}

}
