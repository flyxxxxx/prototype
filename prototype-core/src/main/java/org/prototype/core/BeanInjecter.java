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
package org.prototype.core;

/**
 * 非spring容器中的对象注入到方法参数中的接口. <br>
 * 包括spring中的ApplicationContext、BeanFactory、MessageSource、ResourceLoader、ApplicationEventPublisher、WebApplicationContext、NotificationPublisher、
 * LoadTimeWeaver、AnnotationMetadata、ServletConfig、PortletContext、PortletConfig、SchedulerContext等接口的注入.
 * 包括{@link java.sql.Connection}、{@link org.hibernate.Session}、{@link redis.clients.jedis.Jedis}及其它用户自定义对象的注入.
 * 因为此类对象需要处理事务或进行转换（如打开会话）<br>
 * ServletContext、ObjectMapper等在spring boot中已经注入到applicationContext中，因此不需要实现此接口.
 * <br>
 * 实现此接口必须指定一个类(此类作为可用于注入的数据类型)作为此接口的泛型. <br>
 * 
 * @author flyxxxxx@163.com
 *
 * @param <T>
 *            支持的数据类型
 */
public interface BeanInjecter<T> {
	/**
	 * 是否需要事务
	 * @return 需要则返回true，如{@link java.sql.Connection}、{@link org.hibernate.Session}等数据库相关对象.
	 */
	boolean isNeedTransaction();

	/**
	 * 是否包括默认的bean（存在多个同类型的bean时以有&#064;Primary的为准）
	 * @return 存在返回true
	 */
	boolean containsBean();

	/**
	 * 获取默认的bean（存在多个同类型的bean时以有&#064;Primary的为准）
	 * @return 默认的bean
	 */
	T getBean();

	/**
	 * 获取指定名称的bean
	 * @param beanName bean的名称
	 * @return 指定名称的bean
	 */
	T getBean(String beanName);

	/**
	 * 判断是否包含指定名称的bean
	 * @param beanName bean的名称
	 * @return 存在则返回true
	 */
	boolean containsBean(String beanName);

	/**
	 * 在对象用完之后的处理. <br>
	 * 对于需要事务处理的对象，不需要在此方法进行数据源的关闭过程。用于如Jedis对象回收到对象池JedisPool.
	 * @param bean bean的实例
	 */
	void afterUsed(T bean);
}
