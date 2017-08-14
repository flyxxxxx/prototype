package org.prototype.springframework;

import org.prototype.core.ChainOrder;
import org.prototype.core.Errors;
import org.prototype.core.MethodAdvisor;
import org.prototype.core.MethodBuilder;
import org.prototype.core.MethodChain;
import org.prototype.core.MethodFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
class CachePutMethodAdvisor implements MethodAdvisor{
	
	@Autowired(required=false)
	private CacheManager manager;

	@Override
	public MethodFilter<?> matches(MethodBuilder builder, Errors errors) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Order(ChainOrder.HIGH)
	private class SpringCachePutMethodFilter implements MethodFilter<CachePut>{
		private CachePut cachePut;

		@Override 
		public Object doFilter(Object[] args, MethodChain chain) throws Exception {
			 Object rs=chain.doFilter(args);

			// TODO Auto-generated method stub
			return null;
		}
		
	}

}
