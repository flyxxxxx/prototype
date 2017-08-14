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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.prototype.core.BeanInjecter;
import org.prototype.core.ComponentContainer;
import org.prototype.core.ConditionalHasClass;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Hibernate会话注入支持. <br>
 * 可直接在原型类方法上注入Jedis对象，无需担心资源的释放问题.
 * 
 * <pre>
 * 使用需要在maven的pom.xml中加入hibernate相关依赖。
 * </pre>
 * 
 * @author lj
 *
 */
@Component
@ConditionalHasClass(Session.class)
public class SessionInjecter implements BeanInjecter<Session> {

	@Resource
	private ApplicationContext applicationContext;

	@Override
	public boolean isNeedTransaction() {
		return true;
	}

	@Override
	public boolean containsBean() {
		return applicationContext.getBean(ComponentContainer.class).containsBean(SessionFactory.class);
	}

	@Override
	public Session getBean() {
		return ((SessionFactory) applicationContext.getBean(SessionFactory.class)).getCurrentSession();
	}

	@Override
	public Session getBean(String beanName) {
		return ((SessionFactory) applicationContext.getBean(beanName)).getCurrentSession();
	}

	@Override
	public boolean containsBean(String beanName) {
		return applicationContext.containsBean(beanName)
				&& SessionFactory.class.isInstance(applicationContext.getBean(beanName));
	}

	@Override
	public void afterUsed(Session bean) {
	}

}
