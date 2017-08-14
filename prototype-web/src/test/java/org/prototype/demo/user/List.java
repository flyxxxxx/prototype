package org.prototype.demo.user;

import java.util.Collection;

import org.prototype.business.Input;
import org.prototype.business.Output;
import org.prototype.business.Prop;
import org.prototype.business.ServiceDefine;
import org.prototype.sql.PreparedSql;
import org.prototype.sql.SQLBuilder;
import org.prototype.web.Business;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@ServiceDefine(hint = "查询用户列表", value = "用户列表")
@RequestMapping("/user/list")
@Slf4j
public class List extends Business {

	@Input(@Prop(desc = "关键字",maxLength=20,required=false))
	private String keyword;

	@Output(desc="用户列表",value={ @Prop(name = "id", desc = "ID"), @Prop(name = "name", desc = "姓名") })
	private Collection<User> users;

	void business() {
		SQLBuilder builder = new SQLBuilder("select id,name from common_user");
		builder.appendWhenNotEmpty(keyword, "%" + keyword + "%", "where name like ?");
		users = query(builder);
	}

	@PreparedSql
	private Collection<User> query(SQLBuilder builder) {
		return null;// do nothing
	}
	
	void async(){
		log.debug("Access user list ");
	}
}
