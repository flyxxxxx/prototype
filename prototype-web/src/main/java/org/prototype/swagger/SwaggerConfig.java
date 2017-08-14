package org.prototype.swagger;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Swagger配置
 * 
 * @author lj
 *
 */
@Data
@ConfigurationProperties(prefix = "prototype.swagger")
public class SwaggerConfig {

	private String serverUrl;
	private String title;
	private String description;
	private String version = "1.0";
}
