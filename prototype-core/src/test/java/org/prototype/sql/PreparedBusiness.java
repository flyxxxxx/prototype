package org.prototype.sql;

import java.util.ArrayList;
import java.util.List;

import org.prototype.core.Prototype;
import org.prototype.entity.Dict;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Prototype
public class PreparedBusiness {

	private Integer id;

	@Transactional
	public boolean execute() {
		insertDict("insert into sys_dict(name) values(?)", new Object[] { "test1" });
		List<Dict> list = select("select id,name from sys_dict");
		query();
		if (updateDict("update sys_dict set name=? where id=?", new Object[] { "test2", list.get(0).getId() }) > 0) {
			Dict dict = selectOne("select id,name from sys_dict where id=?", new Object[] { list.get(0).getId() });
			deleteDict("delete from sys_dict where id=?", new Object[] { dict.getId() });
			return true;
		}
		return false;
	}

	private void query() {
		SQLBuilder builder = new SQLBuilder();
		builder.append("select id,name from sys_dict where id>0");
		builder.appendWhenNotEmpty(id, "and id=?");
		builder.appendWhenNotEmpty(new ArrayList<Integer>(), "and id in");
		Dict rs = query(builder);
		Assert.notNull(rs);
	}

	@PreparedSql(type = StatementType.SELECT)
	private Dict query(SQLBuilder builder) {
		return null;
	}

	@PreparedSql(type = StatementType.DELETE)
	private void deleteDict(String sql, Object[] params) {
	}

	@PreparedSql({ "id", "name" })
	private Dict selectOne(String sql, Object[] params) {
		return null;
	}

	@PreparedSql({ "id", "name" })
	private List<Dict> select(String sql) {
		return null;
	}

	@Transactional
	public int update() {
		return updateDict("update sys_dict set name=? where id=?", new Object[] { "test2", id });
	}

	@PreparedSql(type = StatementType.UPDATE)
	private int updateDict(String sql, Object[] params) {
		return 0;
	}

	@PreparedSql(type = StatementType.INSERT)
	Integer insertDict(String sql, Object[] params) {
		return null;
	}

}
