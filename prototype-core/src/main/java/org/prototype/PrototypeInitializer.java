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

import java.awt.IllegalComponentStateException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.prototype.core.ClassScaner;
import org.prototype.core.Errors;
import org.prototype.reflect.CacheUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Prototype框架初始化类. <br>
 * 
 * <pre>
 * 在Spring Boot环境中，不需要配置此类的实例。
 * 在没有Spring Boot的环境中（仅使用Spring框架），并且需要定义一些全局配置时(全局配置见下面第3项)，需要满足以下条件
 * 1、定义一个此类的子类
 * 2、定义一个子类的Spring bean，同时声明为&#064;Primary.
 * 3、根据需要定义一些{@link org.prototype.business.Executor}注解的注解作为全局配置.
 * 例：
 * &#064;Component
 * &#064;Primary
 * &#064;EnableAsync//使用Servlet3异步方式提供Spring MVC的控制器
 * &#064;ConcurrentLimit(500)//整体并发限制为500
 * public class PrototypeInitializerImpl extends PrototypeInitializer{}
 * 
 * </pre>
 * 
 * @author flyxxxxx@163.com
 *
 */
@Slf4j
@Component
public class PrototypeInitializer {

	/**
	 * 所有的消息资源
	 */
	@Getter
	Set<String> messages = new HashSet<>();

	/**
	 * 所有要扫描的包
	 */
	Set<String> packageNames = new HashSet<>();

	/**
	 * 所有要忽略的组件类
	 */
	@Getter
	Set<Class<?>> ignore = new HashSet<>();

	/**
	 * 启动类
	 */
	@Getter
	Class<?> bootClass;

	/**
	 * 构造
	 */
	public PrototypeInitializer() {
		bootClass = getClass();
	}

	@Component
	static class Initialization {

		@javax.annotation.Resource
		private PrototypeInitializer initializer;
		@javax.annotation.Resource
		private ApplicationContext context;

		@PostConstruct
		public void init() {
			CacheUtils.clear();
			List<String> classNames = new ArrayList<>();
			for (String packageName : initializer.packageNames) {
				classNames.addAll(initializer.scanPackage(packageName));
			}
			if(classNames.isEmpty()){
				log.warn("No classes found in package {}",initializer.packageNames);
				return;
			}
			Errors errors = new Errors(initializer);
			for (ClassScaner scaner : context.getBeansOfType(ClassScaner.class).values()) {
				scaner.scan(classNames, errors);
			}
			if (!errors.getMessages().isEmpty()) {
				throw new IllegalComponentStateException(errors.getMessages().toString());
			}
		}
	}

	/**
	 * 扫描出包中所有的类
	 * 
	 * @param packageName
	 *            包名
	 * @return 所有的类
	 */
	private Collection<String> scanPackage(String packageName) {
		log.info("Scan package : {}", packageName);
		List<String> list = new ArrayList<>();
		try {
			Resource[] res = new PathMatchingResourcePatternResolver()
					.getResources("classpath*:" + packageName.replace('.', '/') + "/**/*.class");
			CachingMetadataReaderFactory factory = new CachingMetadataReaderFactory(
					Thread.currentThread().getContextClassLoader());
			for (Resource resource : res) {// 循环处理类资源
				String className = getClassName(factory, resource);
				if (className != null) {
					list.add(className);
				}
			}
		} catch (IOException e) {
			log.warn("IO error : ", e);
		}
		return list;
	}

	/**
	 * 获取资源对应类名
	 * 
	 * @param factory
	 *            工厂
	 * @param resource
	 *            资源
	 * @return 类名
	 * @throws IOException
	 */
	private static String getClassName(CachingMetadataReaderFactory factory, Resource resource) throws IOException {
		if (!resource.isReadable()) {
			return null;
		}
		MetadataReader metadataReader = factory.getMetadataReader(resource);
		ClassMetadata metadata = metadataReader.getClassMetadata();
		if (!metadata.isAbstract() && !metadata.isAnnotation() && !metadata.isInterface()) {
			return metadata.getClassName();
		}
		return null;
	}
}
