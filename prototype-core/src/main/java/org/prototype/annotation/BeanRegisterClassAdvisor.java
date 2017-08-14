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
package org.prototype.annotation;

import java.lang.annotation.Annotation;

import org.prototype.core.ChainOrder;
import org.prototype.core.ClassBuilder;
import org.prototype.core.ClassFactory;
import org.prototype.core.Errors;
import org.prototype.core.Prototype;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 原型bean的注册. <br>
 * 对于标记为Prototype的类（或注解类有注解Prototype），如果属性{@link org.prototype.core.Prototype#register()}值为true，则通过此类将注册为Spring
 * bean，注册的bean的scope为prototype(原型，非单一实例).
 * 
 * @author flyxxxxx@163.com
 *
 */

@Component
@Order(ChainOrder.VERY_LOWER)
public class BeanRegisterClassAdvisor extends AbstractClassAdvisor implements BeanFactoryAware {
	private DefaultListableBeanFactory registry;

	/**
	 * 检查类的元注解Prototype的属性register是否为true
	 */
	@Override
	protected boolean matches(ClassBuilder builder, Errors errors) {
		Annotation[] annotations = builder.getAnnotationByMeta(Prototype.class);
		for (Annotation annotation : annotations) {
			Prototype prototype = null;
			if (Prototype.class.equals(annotation.annotationType())) {
				prototype = (Prototype) annotation;
			} else {
				prototype = annotation.annotationType().getAnnotation(Prototype.class);
			}
			if (prototype.register()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 注册prototype类型的bean
	 */
	@Override
	protected void register(ClassFactory factory, Class<?> clazz) {
		BeanDefinitionBuilder define = BeanDefinitionBuilder.genericBeanDefinition(clazz);
		define.setScope(BeanDefinition.SCOPE_PROTOTYPE);
		registry.registerBeanDefinition(clazz.getName(), define.getRawBeanDefinition());
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		registry = (DefaultListableBeanFactory) beanFactory;
	}

}
