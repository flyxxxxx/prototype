package org.prototype.demo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.transaction.Transactional;

import org.prototype.annotation.Chain;
import org.prototype.core.Prototype;

import lombok.Getter;

/**
 * 事务处理
 * @author lj
 *
 */
@Prototype
public class TransactionalBusiness {
	
	@Getter
	private Number current;

	@Chain("date")
	@Transactional
	public void execute(){}
	
	void date(Connection connection) throws SQLException{
		ResultSet rs = connection.prepareStatement("SELECT * FROM   INFORMATION_SCHEMA.TABLES").executeQuery();
		rs.next();
		current=1;
		rs.close();
		
	}
}
