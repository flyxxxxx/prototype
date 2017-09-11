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

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.prototype.core.PrototypeStatus;
import org.prototype.core.PrototypeStatus.TransactionStatus;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.util.Assert;

import lombok.Setter;

/**
 * 读写数据源. <br>
 * 自动根据事务状态切换读或写数据源.
 * 
 * @author lj
 *
 */
public class ReadWriteDataSource extends AbstractRoutingDataSource {

	private static final String[] NAMES = { "writeDataSource", "readDataSource" };

	@Setter
	private DataSource writeDataSource;
	@Setter
	private DataSource readDataSource;

	/**
	 * 准备读写分离数据源
	 */
	@Override
	public void afterPropertiesSet() {
		Assert.notNull(writeDataSource);
		Assert.notNull(readDataSource);
		Map<Object, Object> targetDataSources = new HashMap<>();
		targetDataSources.put(writeDataSource, writeDataSource);
		targetDataSources.put(readDataSource, readDataSource);
		setTargetDataSources(targetDataSources);
		super.afterPropertiesSet();
	}

	/**
	 * 根据线程变量PrototypeStatus中的读写区分数据源
	 */
	@Override
	protected Object determineCurrentLookupKey() {
		PrototypeStatus status = PrototypeStatus.getStatus();
		Assert.notNull(status);
		TransactionStatus trans = status.getTransaction();
		Assert.notNull(trans);
		return trans.isReadOnly() ? NAMES[1] : NAMES[0];
	}

}
