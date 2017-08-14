package org.prototype.business.limit;

import java.util.concurrent.atomic.AtomicInteger;

import org.prototype.business.ExecuteFilter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 并发数限制实现. <br>
 * @author flyxxxxx@163.com
 *
 */
@Component
@Order(ExecuteFilter.REQUEST)
public class ConcurrentLimitExecuteFilter extends AbstractLimitExecuteFilter<ConcurrentLimit,AtomicInteger> {


	@Override
	protected AtomicInteger createLimiter(ConcurrentLimit limit) {
		return new AtomicInteger(0);
	}

	@Override
	protected boolean acquire(ConcurrentLimit limit, AtomicInteger limiter) {
		return limiter.incrementAndGet()<=limit.value();
	}

	@Override
	protected void countdown(AtomicInteger limiter) {
		limiter.decrementAndGet();
	}


}
