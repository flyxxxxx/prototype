package org.prototype.web;

import java.util.HashMap;
import java.util.Map;

@WebDefine
public class WebBusiness extends Business{
	
	public Map<String, String> getHeaders(){
		Map<String, String> headers=new HashMap<>();
		
		return headers;
	}

	public long lastModified(){
		return -1;
	}
}
