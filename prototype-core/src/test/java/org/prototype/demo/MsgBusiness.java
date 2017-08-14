package org.prototype.demo;

import org.prototype.annotation.Msg;
import org.prototype.core.Prototype;

@Prototype
public class MsgBusiness {

	@Msg(type="test")
	public int message1(){
		return 0;
	}
}
