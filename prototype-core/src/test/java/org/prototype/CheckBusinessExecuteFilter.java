package org.prototype;

import org.prototype.business.ExecuteChain;
import org.prototype.business.ExecuteFilter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 在业务类注入参数后，进行检查. <br>
 * 在业务类注入参数之后 ，调用{@link DataTest#checkBusiness()}方法检查数据
 * @author lj
 *
 */
@SuppressWarnings("rawtypes")
@Order(ExecuteFilter.INIT+1)
@Component
public class CheckBusinessExecuteFilter implements   ExecuteFilter {

	@Override
	public void doFilter(ExecuteChain chain) throws Exception {
		Object target=chain.getTarget();
		if(DataTest.class.isInstance(target)){
			((DataTest)target).checkBusiness();
		}
		chain.doChain();
	}

}
