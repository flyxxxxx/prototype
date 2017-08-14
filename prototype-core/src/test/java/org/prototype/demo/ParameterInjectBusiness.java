package org.prototype.demo;

import org.prototype.annotation.Chain;
import org.prototype.core.Prototype;
import org.springframework.util.Assert;

/**
 * 参数注入
 * @author lj
 *
 */
@Prototype
public class ParameterInjectBusiness {

	@Chain("method1")
	public void execute(){
		//do nothing
	}
	
	void method1(@Counter Long random){
		Assert.notNull(random);
	}
	
}
