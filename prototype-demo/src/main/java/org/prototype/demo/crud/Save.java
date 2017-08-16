package org.prototype.demo.crud;

import java.sql.Connection;

import org.prototype.business.Input;
import org.prototype.business.Output;
import org.prototype.business.Prop;
import org.prototype.business.ServiceDefine;
import org.prototype.sql.PreparedSql;
import org.prototype.sql.SQLBuilder;
import org.prototype.sql.StatementType;
import org.prototype.demo.Business;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@ServiceDefine(hint = "保存用户", value = "保存用户")
@RequestMapping(value="/user/save",method=RequestMethod.POST)
public class Save extends Business{

	@Input(desc="用户列表",value={ @Prop(name = "name", desc = "姓名",maxLength=20) })
	private User user;
	
	@Output(@Prop(desc="ID"))
	private Integer id;
	
	void business(Connection connection){
		SQLBuilder builder=new SQLBuilder("insert into common_user(name) values(?)");
		builder.appendParam(user.getName());
		id=insert(connection,builder);
	}
	
	@PreparedSql(type=StatementType.INSERT)
	Integer insert(Connection connection,SQLBuilder builder){
		return null;//do nothing
	}
	
}
