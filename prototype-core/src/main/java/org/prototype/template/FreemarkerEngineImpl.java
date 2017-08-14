package org.prototype.template;

import java.util.Map;

import org.springframework.stereotype.Component;

//@Component
public class FreemarkerEngineImpl implements Engine{

	@Override
	public String getType() {
		return "freemarker";
	}

	@Override
	public String render(String template, Map<String, Object> properties) {
		// TODO Auto-generated method stub
		return null;
	}

}
