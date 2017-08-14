package org.prototype.demo;

import org.prototype.business.Output;
import org.prototype.business.Prop;
import org.prototype.business.ServiceDefine;
import org.prototype.business.View;
import org.prototype.web.Business;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.Data;

@ServiceDefine("用户信息")
@RequestMapping("/user")
public class FirstView extends Business{

	@View
	private String view="first";
	
	@Output(desc="User", value = { @Prop(name="id",desc="ID"),@Prop(name="name",desc="Name") })
	private User user;
	
	void business(){
		user=new User();
		user.id=5;
		user.name="张三";
	}
	
	@Data
	static class User{
		private Integer id;
		private String name;
	}
}
