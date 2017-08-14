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
package org.prototype;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 框架参数配置. <br>
 * 
 * <pre>
 * 用于定义Prototype框架的一些配置参数，配置参数以"prototype."开头，
 * 属性{@link #apiRepository}：用于定义保存API文档的服务器路径，以备API的查阅及分析接口变更工作. 
 * 提交的数据将是JsonApiCreator.JsonApi的数组（以Spring RequestBody形式提交JSON数据），返回有冲突的业务的URL地址数组（String[]）
 * 属性{@link #api}：用于控制哪些业务类需要生成指定类型接口. 配置方式可参考{@link Api}。
 * 在生成接口的时，是调用{@link org.prototype.business.ApiCreator#createForAll(java.util.Collection)}方法来生成需要的接口及实现类。
 * </pre>
 * 
 * @author flyxxxxx@163.com
 *
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "prototype")
public class PrototypeConfig {

	/**
	 * 当前服务器的地址，默认为http://localhost:8080
	 */
	@Getter
	@Setter
	private String serverUrl = "http://localhost:8080";

	/**
	 * API接口存储服务器地址，提交的数据将是JsonApiCreator.JsonApi的数组（以Spring
	 * RequestBody形式提交JSON数据），返回有冲突的业务的URL地址数组（String[]）
	 */
	@Getter
	@Setter
	private String apiRepository;
	
	/**
	 * 创建API文档时的参数，如http://localhost:8080/list?_doc=json
	 */
	@Getter@Setter
	private String apiParameterName="_doc";

	/**
	 * 定义输出到API的业务
	 */
	@Getter
	@Setter
	private Map<String, Api> api = new HashMap<>();

	/**
	 * 主线程池（如果spring中已经定义，则直接使用，否则自动创建）
	 */
	@Getter
	@Setter
	private ThreadPoolTaskExecutor threadPool;
	/**
	 * 第二线程池(如果未指定，则自动创建)
	 */
	@Getter
	@Setter
	private ThreadPoolTaskExecutor secondThreadPool;

	@Autowired(required = false)
	private ThreadPoolExecutor threadPoolExecutor;

	@PostConstruct
	void init() {
		if (threadPoolExecutor == null && threadPool == null) {
			threadPool = createThreadPoolTaskExecutor(true);
		}
		if (secondThreadPool == null) {
			secondThreadPool = createThreadPoolTaskExecutor(false);
		}
	}

	/**
	 * 创建线程池任务执行器
	 * 
	 * @param main
	 *            是否主要任务执行器
	 * @return 线程池任务执行器
	 */
	private ThreadPoolTaskExecutor createThreadPoolTaskExecutor(boolean main) {
		int num = Runtime.getRuntime().availableProcessors();
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setThreadNamePrefix("Prototype " + (main ? "main" : "second") + " thread pool");
		executor.setCorePoolSize(main ? num * 2 : 1);
		executor.setThreadPriority(main ? Thread.MAX_PRIORITY : Thread.MIN_PRIORITY);
		executor.setMaxPoolSize(executor.getCorePoolSize() * (main ? 20 : 10));
		executor.setKeepAliveSeconds(300);
		int capacity = Integer.MAX_VALUE;
		if (main) {
			capacity = executor.getMaxPoolSize() * 2;
			executor.setQueueCapacity(capacity);
		}
		executor.afterPropertiesSet();
		log.info(
				"Create thread pool task executor '{}' , priority : {} , core pool size : {} , max pool size : {} , keep alive seconds : {} , queue capacity {}",
				executor.getThreadNamePrefix(), executor.getThreadPriority(), executor.getCorePoolSize(),
				executor.getMaxPoolSize(), executor.getKeepAliveSeconds(), capacity);
		return executor;
	}

	/**
	 * 获取主要的线程池
	 * 
	 * @return 线程池
	 */
	public ThreadPoolExecutor getThreadPoolExecutor() {
		return threadPool == null ? threadPoolExecutor : threadPool.getThreadPoolExecutor();
	}

	/**
	 * 给API生成提供的配置. <br>
	 * 
	 * <pre>
	 * 用于允许或禁止提供指定类型的API，排除（及包含）指定的业务类或指定包下的所有业务类。比如控制部分接口生成或不生成dubbo类型接口或springmvc控制器接口。
	 * 属性{@link #enable}：是否允许生成此类型的API，未指定时，如果其它几个参数如果非空，则返回true。
	 * 属性{@link #includePackages}：生成API时包含业务类的包名。包括指定包名及其子包，多个包名以逗号分开。
	 * 属性{@link #excludePackages}：生成API时需要排除的业务类的包。包括指定包名及其子包，多个包名以逗号分开。处理时将从includePackages中排除这些包及子包。
	 * 属性{@link #includeClasses}：生成API时包含的业务类。多个以逗号或分号分开），与includePackages是or关系，不会被excludePackages排除，会被excludeClasses排除。
	 * 属性{@link #excludeClasses}：生成API时被排除的业务类。多个以逗号或分号分开，此参数中的类，即使包含在includeClasses或includePackages中，也会被排除。
	 * </pre>
	 * 
	 * @author lj
	 *
	 */
	public static class Api {

		/**
		 * 是否允许(默认为false，未指定时，任意其它参数非空时为true)
		 */
		@Setter
		private Boolean enable;

		/**
		 * 包含的包（多个以逗号或分号分开），默认包含所有的包.
		 */
		@Setter
		@Getter
		private String includePackages = "";

		/**
		 * 排除的包（多个以逗号或分号分开）,从includePackages中排除一些包
		 */
		@Setter
		@Getter
		private String excludePackages = "";

		/**
		 * 包含的业务类（多个以逗号或分号分开），与includePackages是or关系，不会被excludePackages排除，会被excludeClasses排除
		 */
		@Setter
		@Getter
		private String includeClasses = "";
		/**
		 * 排除的业务类（多个以逗号或分号分开）
		 */
		@Setter
		@Getter
		private String excludeClasses = "";

		/**
		 * 内容类型
		 */
		@Setter
		@Getter
		private String contentType;

		/**
		 * 判断是否允许生成此类型的API
		 * 
		 * @return enable未指定时，则其它几个参数如果非空，则返回true
		 */
		public Boolean getEnable() {
			if (enable != null) {
				return enable;
			}
			return includePackages.length() > 0 || excludePackages.length() > 0 || excludeClasses.length() > 0
					|| includeClasses.length() > 0;
		}

	}

}
