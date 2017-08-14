package org.prototype.demo;

import org.prototype.business.Output;
import org.prototype.business.Prop;
import org.prototype.business.ServiceDefine;
import org.prototype.web.Business;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 服务端推送
 * @author lj
 *
 */
@ServiceDefine("服务端推送")
@RequestMapping(value="/eventsource",produces="text/event-stream")
public class EventSource extends Business{
	
	private int k=0;
	@Output(@Prop(desc="value"))
	private String value="";

	void business() throws InterruptedException{
		Thread.sleep(3000);
		value= Integer.toString(k++);
	}
}
