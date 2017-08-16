package org.prototype.demo.hello;

import org.prototype.business.Input;
import org.prototype.business.Output;
import org.prototype.business.Prop;
import org.prototype.business.ServiceDefine;
import org.prototype.demo.Business;

/**
 * hello程序
 * @author lj
 *
 */

@ServiceDefine(value = "Hello",url="/hello")
public class Hello extends Business{

	@Input(@Prop(desc="姓名",maxLength=20))
	private String name;

	@Output(@Prop(desc="welcome"))
	private String welcome;
	
	void business(){
		welcome="Hello "+name;
	}
	
}
