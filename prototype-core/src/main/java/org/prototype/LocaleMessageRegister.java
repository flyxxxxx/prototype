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

import java.util.Map;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 将消息资源名加入PrototypeInitializer的属性messages中.
 * @author lj
 *
 */
class LocaleMessageRegister  implements ImportBeanDefinitionRegistrar{

	@Override
	public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
		Map<String, Object> map = metadata.getAnnotationAttributes(EnableLocaleMessage.class.getName(), false);
		String value=(String) map.get("value");
		ListableBeanFactory beanFactory = (ListableBeanFactory) registry;
		PrototypeInitializer initializer = beanFactory.getBean(PrototypeInitializer.class);
		initializer.getMessages().add(value);
	}

}
