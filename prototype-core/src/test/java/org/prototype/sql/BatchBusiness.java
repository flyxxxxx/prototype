package org.prototype.sql;

import java.util.ArrayList;
import java.util.List;

import org.prototype.core.Prototype;
import org.prototype.entity.Dict;
import org.springframework.transaction.annotation.Transactional;

/**
 * 批处理业务
 * 
 * @author lj
 *
 */
@Prototype
public class BatchBusiness {

	private List<Dict> inserted = new ArrayList<>();
	private List<Dict> updated = new ArrayList<>();

	@Transactional
	@Batch(value = { "update", "insert" }, after = true)
	public void execute() {
		updated.add(new Dict(1, "Item1"));
		updated.add(new Dict(2, "Item2"));
		inserted.add(new Dict("Item3"));
		inserted.add(new Dict("Item4"));
	}

	@Transactional
	public int count() {
		return countSql("select count(id) from sys_dict");
	}

	@PreparedSql()
	private int countSql(String sql){return 0;}

	@BatchSql("update sys_dict set name=? where id=?")
	CollectionIterator<?> update() {
		return new CollectionIterator<Dict>(updated) {

			@Override
			public Object[] next(Dict t) {
				return new Object[] { t.getName(), t.getId() };
			}

		};
	}

	@BatchSql("insert into sys_dict(name) values(?)")
	CollectionIterator<?> insert() {
		return new InsertIterator<Dict>(inserted) {

			@Override
			public Object[] next(Dict t) {
				return new Object[] { t.getName() };
			}

		};
	}
}
