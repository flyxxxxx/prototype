package org.prototype.sql;

import java.sql.Connection;

import javax.annotation.Resource;

import org.prototype.core.MethodAdvisor;
import org.prototype.inject.ConnectionInjecter;
import org.prototype.inject.InjectHelper;

/**
 * 抽象的SQL方法处理适配
 * @author lj
 *
 */
public abstract class AbstractSqlMethodAdvisor implements MethodAdvisor {

	@Resource
	private ConnectionInjecter injecter;

	@Resource
	protected InjectHelper helper;

	protected final Connection getConnection(Object[] args) {
		for (Object obj : args) {
			if (Connection.class.isInstance(obj)) {
				return (Connection) obj;
			}
		}
		return injecter.getBean();
	}
}
