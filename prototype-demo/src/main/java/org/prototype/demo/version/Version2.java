package org.prototype.demo.version;

import org.prototype.business.Output;
import org.prototype.business.Prop;
import org.prototype.business.ServiceDefine;
import org.prototype.demo.Business;

/**
 * 接口版本1
 * @author lj
 *
 */
@ServiceDefine(value="接口版本2",url="/version",version="2.0")
public class Version2 extends Business{

	@Output(@Prop(desc="version"))
	private String version="2.0";

}
