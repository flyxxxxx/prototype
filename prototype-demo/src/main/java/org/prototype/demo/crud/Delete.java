package org.prototype.demo.crud;

import java.sql.Connection;

import org.prototype.business.Input;
import org.prototype.business.Prop;
import org.prototype.business.ServiceDefine;
import org.prototype.sql.PreparedSql;
import org.prototype.sql.SQLBuilder;
import org.prototype.sql.StatementType;
import org.prototype.demo.Business;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@ServiceDefine( value = "删除用户")
@RequestMapping(value="/user/delete",method=RequestMethod.DELETE)
public class Delete extends Business{
	
	@Input(@Prop(desc="ID"))
	private Integer id;
	
	void business(Connection connection){
		SQLBuilder builder=new SQLBuilder("delete from common_user where id=?");
		builder.appendParam(id);
		delete(connection,builder);
	}
	
	@PreparedSql(type=StatementType.DELETE)
	void delete(Connection connection,SQLBuilder builder){
		//do nothing
	}
	
}
