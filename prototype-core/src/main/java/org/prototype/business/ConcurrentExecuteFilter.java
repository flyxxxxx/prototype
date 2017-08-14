package org.prototype.business;

import java.util.concurrent.Executor;

import javax.annotation.Resource;

import org.prototype.core.ComponentContainer;
import org.prototype.core.ExecutorManager;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(ExecuteFilter.POOL)
@Component
class ConcurrentExecuteFilter implements ExecuteFilter<Concurrent>,ExecutorManager {

	@Resource
	private ApplicationContext applicationContext;
	
	@Resource
	private ComponentContainer container;

	@Override
	public void doFilter(ExecuteChain chain) throws Exception {
		Concurrent concurrent=chain.getAnnotation(Concurrent.class);
		
	}

	@Override
	public Executor getExecutor(Object object) {
		// TODO Auto-generated method stub
		return container.containsBean(Executor.class)?applicationContext.getBean(Executor.class):null;
	}

	@Override
	public Executor getAsyncExecutor(Object object) {
		// TODO Auto-generated method stub
		return container.containsBean(Executor.class)?applicationContext.getBean(Executor.class):null;
	}
}
