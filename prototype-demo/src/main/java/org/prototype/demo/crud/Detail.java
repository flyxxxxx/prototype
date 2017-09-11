package org.prototype.demo.crud;

import java.sql.Connection;

import org.prototype.business.Input;
import org.prototype.business.Output;
import org.prototype.business.Prop;
import org.prototype.business.ServiceDefine;
import org.prototype.demo.Business;
import org.prototype.sql.PreparedSql;
import org.prototype.sql.SQLBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 动态路径
 * @author flyxxxxx@163.com
 *
 */
@ServiceDefine( value = "用户明细")
@RequestMapping(value="/user/{id}",method=RequestMethod.GET)
public class Detail extends Business{

	@Input(@Prop(desc="ID"))
	private Integer id;
	

	@Output(desc = "用户列表", value = { @Prop(name = "id", desc = "ID"),
			@Prop(name = "name", desc = "姓名") })
	private User user;
	
	void business(Connection connection) {
		SQLBuilder builder = new SQLBuilder("select id,name from common_user");
		builder.append(id, "where id=?");
		user = query(connection,builder);
	}

	@PreparedSql(value={"id","name"})
	private User query(Connection connection,SQLBuilder builder) {
		return null;// do nothing
	}
	
}
