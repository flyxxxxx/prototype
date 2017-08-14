package org.prototype.demo;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

import org.prototype.business.Output;
import org.prototype.business.Prop;
import org.prototype.business.ServiceDefine;
import org.prototype.web.WebBusiness;

@ServiceDefine(hint = "查询一个列表", value = "列表")
@RequestMapping("/list")
@Cacheable
public class List extends WebBusiness{
	
	@Output(@Prop(desc="IP"))
	private String ip;

	public List(HttpServletRequest request){
		ip=request.getRemoteAddr();
	}
}
