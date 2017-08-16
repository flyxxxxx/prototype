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
@ServiceDefine(value="接口版本1",url="/version")
public class Version1 extends Business{

	@Output(@Prop(desc="version"))
	private String version="1.0";

}
