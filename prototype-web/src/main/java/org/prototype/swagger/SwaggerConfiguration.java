package org.prototype.swagger;

import org.prototype.core.ConditionalHasClass;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@Configuration
@ConditionalHasClass(ApiInfoBuilder.class)
public class SwaggerConfiguration {

	@Value("${local.server.port:${server.port:8080}}")
	private int port;
	@Value("${spring.application.name:MyApplication}")
	private String name;

	@Bean
	public SwaggerConfig swaggerConfig() {
		return new SwaggerConfig();
	}

	@Bean
	public Docket createRestApi() {
		return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).select()
				.apis(RequestHandlerSelectors.withClassAnnotation(Controller.class))
				.paths(PathSelectors.any()).build();
	}

	private String getValue(String value, String defaultValue) {
		if (value == null || value.trim().length() == 0) {
			return defaultValue;
		}
		return value;
	}

	private ApiInfo apiInfo() {
		SwaggerConfig config = swaggerConfig();
		String url = getValue(config.getServerUrl(), "http://localhost:" + port);
		if (!url.endsWith("/")) {
			url = url + "/";
		}
		return new ApiInfoBuilder().title(getValue(config.getTitle(), name)) // 任意，请稍微规范点
				.description(getValue(config.getDescription(), "")) // 任意，请稍微规范点
				.termsOfServiceUrl(url + "swagger-ui.html") // 将“url”换成自己的ip:port
				.version(getValue(config.getVersion(), "1.0.0")).build();
	}
}