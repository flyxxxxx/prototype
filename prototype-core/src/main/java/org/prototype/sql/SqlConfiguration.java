package org.prototype.sql;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * SQL操作配置
 * @author lj
 *
 */
@Component
@ConfigurationProperties(prefix = "prototype.sql")
@Data
public class SqlConfiguration {

	private boolean showSql;
	private int batchSize=100;
}
