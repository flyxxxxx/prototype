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

import javax.annotation.Resource;
import javax.persistence.EntityManager;

import org.prototype.core.BeanInjecter;
import org.prototype.core.ComponentContainer;
import org.prototype.core.ConditionalHasClass;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * EntityManager注入实现. <br>
 * 
 * @author lj
 *
 */
@Component
@ConditionalHasClass(EntityManager.class)
public class EntityManagerInjecter implements BeanInjecter<EntityManager>{

	@Resource
	private ApplicationContext applicationContext;

	@Override
	public boolean isNeedTransaction() {
		return true;
	}

	@Override
	public boolean containsBean() {
		return applicationContext.getBean(ComponentContainer.class).containsBean(EntityManager.class);
	}

	@Override
	public EntityManager getBean() {
		return applicationContext.getBean(EntityManager.class);
	}

	@Override
	public EntityManager getBean(String beanName) {
		return (EntityManager) applicationContext.getBean(beanName);
	}

	@Override
	public boolean containsBean(String beanName) {
		return applicationContext.containsBean(beanName)&&EntityManager.class.isInstance(applicationContext.getBean(beanName));
	}

	@Override
	public void afterUsed(EntityManager bean) {
		// do nothing
	}

}
