package org.prototype.sql;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.prototype.business.Business;
import org.prototype.business.ServiceDefine;
import org.prototype.entity.Dict;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@ServiceDefine(value = "prepared", url = "/prepared")
public class PreparedBusiness extends Business {

	private Integer id;

	void business(Connection connection) {
		insertDict(connection, "insert into sys_dict(name) values(?)", new Object[] { "test1" });
		List<Dict> list = select(connection, "select id,name from sys_dict");
		query(connection);
		if (updateDict(connection, "update sys_dict set name=? where id=?",
				new Object[] { "test2", list.get(0).getId() }) > 0) {
			Dict dict = selectOne(connection, "select id,name from sys_dict where id=?",
					new Object[] { list.get(0).getId() });
			deleteDict(connection, "delete from sys_dict where id=?", new Object[] { dict.getId() });
		}
	}

	private void query(Connection connection) {
		SQLBuilder builder = new SQLBuilder();
		builder.append("select id,name from sys_dict where id>0");
		builder.appendWhenNotEmpty(id, "and id=?");
		builder.appendWhenNotEmpty(new ArrayList<Integer>(), "and id in");
		Dict rs = query(connection, builder);
		Assert.notNull(rs);
	}

	@PreparedSql(type = StatementType.SELECT)
	private Dict query(Connection connection, SQLBuilder builder) {
		return null;
	}

	@PreparedSql(type = StatementType.DELETE)
	private void deleteDict(Connection connection, String sql, Object[] params) {
	}

	@PreparedSql({ "id", "name" })
	private Dict selectOne(Connection connection, String sql, Object[] params) {
		return null;
	}

	@PreparedSql({ "id", "name" })
	private List<Dict> select(Connection connection, String sql) {
		return null;
	}

	@Transactional
	public int update(Connection connection) {
		return updateDict(connection, "update sys_dict set name=? where id=?", new Object[] { "test2", id });
	}

	@PreparedSql(type = StatementType.UPDATE)
	private int updateDict(Connection connection, String sql, Object[] params) {
		return 0;
	}

	@PreparedSql(type = StatementType.INSERT)
	Integer insertDict(Connection connection, String sql, Object[] params) {
		return null;
	}

}
