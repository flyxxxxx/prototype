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
package org.prototype.inject;

import java.sql.Connection;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.prototype.core.BeanInjecter;
import org.prototype.core.ComponentContainer;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

/**
 * 数据库连接注入实现. <br>
 * 提供java.sql.Connection注入到原方法中，避免注入数据源带来的问题（事务管理及资源释放等）。
 * @author lj
 *
 */
@Component
public class ConnectionInjecter implements BeanInjecter<Connection> {

	@Resource
	private ApplicationContext applicationContext;

	@Override
	public boolean isNeedTransaction() {
		return true;
	}

	@Override
	public boolean containsBean() {
		return applicationContext.getBean(ComponentContainer.class).containsBean(DataSource.class);
	}

	@Override
	public Connection getBean() {
		return DataSourceUtils.getConnection(applicationContext.getBean(DataSource.class));
	}

	@Override
	public Connection getBean(String beanName) {
		return DataSourceUtils.getConnection((DataSource) applicationContext.getBean(beanName));
	}

	@Override
	public boolean containsBean(String beanName) {
		return applicationContext.containsBean(beanName)
				&& DataSource.class.isInstance(applicationContext.getBean(beanName));
	}

	@Override
	public void afterUsed(Connection bean) {
		// do nothing
	}

}
