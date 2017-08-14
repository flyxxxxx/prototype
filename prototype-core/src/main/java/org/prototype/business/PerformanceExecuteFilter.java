package org.prototype.business;

import org.prototype.annotation.Message;
import org.prototype.core.PrototypeStatus;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 性能分析. <br>
 * 
 * @author lj
 *
 */
@Component
@Order(Integer.MIN_VALUE)
public class PerformanceExecuteFilter implements ExecuteFilter<Performance> {

	@Override
	public void doFilter(ExecuteChain chain) throws Exception {
		try {
			chain.doChain();
		} finally {
			Performance performance = chain.getAnnotation(Performance.class);
			PrototypeStatus status = PrototypeStatus.getStatus();
			long useTime = System.currentTimeMillis() - status.getStartTime();
			if (useTime < performance.value()) {
				return;
			}
			PerformanceData data = new PerformanceData();
			data.setBusinessType(chain.getType());
			data.setParameters(chain.getParams());
			data.setUseTime(useTime);
			Message.getSubject()
					.onNext(new Message(PerformanceData.TYPE, PerformanceExecuteFilter.class.getName(), data));
		}
	}

}
