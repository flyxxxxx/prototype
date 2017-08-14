package org.prototype.demo;

import org.prototype.core.Prototype;
import org.prototype.template.Template;

import lombok.Data;

/**
 * 消息模板
 * @author lj
 *
 */
@Prototype
public class TemplateBusiness {
	
	static final String path;
	  
	static{
		path=System.getProperty("user.dir");
	}

	@Template("${user.name},你好")
	public String template1(User user){
		return null;//不需要做任何实现
	}

	@Template(file="classpath:template1.txt",engine="js")
	public String template3(User user){
		return null;//不需要做任何实现
	}

	@Template(file="template.txt")
	public String template2(String s1,String s2){
		return null;//不需要做任何实现
	}
	
	@Data
	public static class User {
		private String name;
	}
}
