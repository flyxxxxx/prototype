/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.prototype.sql;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.prototype.core.PrototypeStatus;

/**
 * 读写数据源. <br>
 * 自动根据事务状态切换读或写数据源.
 * @author lj
 *
 */
public class ReadWriteDataSource implements DataSource {
	
	private DataSource readDataSource;

	private DataSource writeDataSource;
	
	public ReadWriteDataSource(DataSource readDataSource,DataSource writeDataSource){
		this.readDataSource=readDataSource;
		this.writeDataSource=writeDataSource;
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return readDataSource.getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		writeDataSource.setLogWriter(out);
		readDataSource.setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		writeDataSource.setLoginTimeout(seconds);
		readDataSource.setLoginTimeout(seconds);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return readDataSource.getLoginTimeout();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return readDataSource.getParentLogger();
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return readDataSource.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return readDataSource.isWrapperFor(iface);
	}

	@Override
	public Connection getConnection() throws SQLException {
		if(PrototypeStatus.getStatus().getTransaction().isReadOnly()){
			return readDataSource.getConnection();
		}
		return writeDataSource.getConnection();
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		if(PrototypeStatus.getStatus().getTransaction().isReadOnly()){
			return readDataSource.getConnection(username,password);
		}
		return writeDataSource.getConnection(username,password);
	}

}
