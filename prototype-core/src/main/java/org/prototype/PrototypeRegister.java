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

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 原型类扫描. <br>
 * <pre>
 * 将注解Prototype的内容加入类PrototypeConfig的属性中，使其完成原型类的修改和加载。
 * </pre>
 * @author flyxxxxx@163.com
 *
 */
class PrototypeRegister implements ImportBeanDefinitionRegistrar {

	/**
	 * 准备原型需要的数据
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
		Map<String, Object> map = metadata.getAnnotationAttributes(EnablePrototype.class.getName(), false);
		String[] value = (String[]) map.get("value");
		if (value.length == 0) {// 获取默认包名
			String className = metadata.getClassName();
			int k = className.lastIndexOf('.');
			value = new String[] { className.substring(0, k) };
		}
		ListableBeanFactory beanFactory = (ListableBeanFactory) registry;
		PrototypeInitializer initializer = beanFactory.getBean(PrototypeInitializer.class);
		initializer.packageNames.addAll(Arrays.asList(value));
		initializer.ignore.addAll(Arrays.asList((Class<?>[]) map.get("ignore")));//忽略的类
		findBootClass(metadata, initializer);
	}

	/**
	 * 查询是否启动类
	 * @param metadata
	 * @param initializer
	 */
	private void findBootClass(AnnotationMetadata metadata, PrototypeInitializer initializer) {
		String bootName = "org.springframework.boot.SpringBootConfiguration";
		for (String name : metadata.getAnnotationTypes()) {
			if (bootName.equals(name) || metadata.getMetaAnnotationTypes(name).contains(bootName)) {
				try {
					initializer.bootClass = getClass().getClassLoader().loadClass(metadata.getClassName());
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

}
