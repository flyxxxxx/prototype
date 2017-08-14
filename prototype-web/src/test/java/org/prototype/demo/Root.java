package org.prototype.demo;

import org.prototype.business.ServiceDefine;
import org.prototype.business.View;
import org.prototype.web.Business;
import org.springframework.web.bind.annotation.RequestMapping;

@ServiceDefine("根路径")
@RequestMapping("/")
public class Root extends Business{

	@View
	private String view="redirect:index.html";
}
