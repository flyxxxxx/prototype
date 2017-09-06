package org.prototype.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.prototype.core.ParameterInjecter;
import org.springframework.stereotype.Component;

/**
 * 计数参数注入
 * 
 * @author lj
 *
 */
@Component
public class CounterInjector implements ParameterInjecter<Counter,Long> {

	private AtomicLong seq = new AtomicLong();

	@Override
	public List<String> validate(Counter annotation,Class<Long> type) {
		return new ArrayList<>();
	}

	@Override
	public Long inject(Counter annotation,Class<Long> type) {
		return seq.incrementAndGet();
	}


}
