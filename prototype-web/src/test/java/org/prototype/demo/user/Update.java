package org.prototype.demo.user;

import org.prototype.business.Input;
import org.prototype.business.Prop;
import org.prototype.business.ServiceDefine;
import org.prototype.sql.PreparedSql;
import org.prototype.sql.SQLBuilder;
import org.prototype.sql.StatementType;
import org.prototype.web.Business;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@ServiceDefine(hint = "更新用户", value = "更新用户")
@RequestMapping(value = "/user/update", method = RequestMethod.PUT)
public class Update extends Business {

	@Input(desc = "用户列表", value = { @Prop(name = "id", desc = "ID"),
			@Prop(name = "name", desc = "姓名", maxLength = 20) })
	private User user;

	void business() {
		SQLBuilder builder = new SQLBuilder("update common_user set name=? where id=?");
		builder.appendParam(user.getName(), user.getId());
	}

	@PreparedSql(type = StatementType.UPDATE)
	void update(SQLBuilder builder) {
		// do nothing
	}

}
