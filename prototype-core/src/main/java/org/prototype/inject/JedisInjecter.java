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

import org.prototype.core.BeanInjecter;
import org.prototype.core.ComponentContainer;
import org.prototype.core.ConditionalHasClass;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Jedis对象支持. <br>
 * 可直接在原型类方法上注入Jedis对象，无需担心资源的释放问题.
 * <pre>
 * 使用需要：
 * maven:
		&lt;dependency&gt;
			&lt;groupId&gt;redis.clients&lt;/groupId&gt;
			&lt;artifactId&gt;jedis&lt;/artifactId&gt;
		&lt;/dependency&gt;
 * </pre>
 * 
 * @author lj
 *
 */
@ConditionalHasClass(Jedis.class)
@Component
public class JedisInjecter implements BeanInjecter<Jedis> {

	@Resource
	private ApplicationContext applicationContext;

	@Override
	public boolean isNeedTransaction() {
		return false;
	}

	@Override
	public boolean containsBean() {
		return applicationContext.getBean(ComponentContainer.class).containsBean(JedisPool.class);
	}

	@Override
	public Jedis getBean() {
		return applicationContext.getBean(JedisPool.class).getResource();
	}

	@Override
	public Jedis getBean(String beanName) {
		return ((JedisPool) applicationContext.getBean(beanName)).getResource();
	}

	@Override
	public boolean containsBean(String beanName) {
		return applicationContext.containsBean(beanName)
				&& JedisPool.class.isInstance(applicationContext.getBean(beanName));
	}

	@Override
	public void afterUsed(Jedis bean) {
		bean.close();
	}

}
