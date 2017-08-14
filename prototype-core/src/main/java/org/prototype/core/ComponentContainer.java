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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.prototype.PrototypeInitializer;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

/**
 * 组件容器. <br>
 * 可通过@Primary替换.
 * 
 * @author flyxxxxx@163.com
 *
 */
@Component
public class ComponentContainer implements BeanFactoryAware {

	@Resource
	private ApplicationContext applicationContext;

	@Resource
	private PrototypeInitializer initializer;

	private ConfigurableListableBeanFactory factory;

	/**
	 * 获取容器中的指定类型bean. <br>
	 * 有@Primary注解的bean优先于没有的
	 * 
	 * @param <T> 结果类型
	 * @param componentType
	 *            组件类型
	 * @return 容器中的指定类型bean
	 */
	public <T> T getComponent(Class<T> componentType) {
		Map<String, T> map = applicationContext.getBeansOfType(componentType);
		if (map != null) {
			List<T> beans=new ArrayList<>();
			for (Map.Entry<String, T> bean : map.entrySet()) {
				if(isIgnored(bean.getKey())){
					continue;
				}
				if (factory.getBeanDefinition(bean.getKey()).isPrimary()) {
					return bean.getValue();
				}else{
					beans.add(bean.getValue());
				}
			}
			return beans.size()==1?beans.get(0):null;
		}
		return null;
	}

	/**
	 * 判断是否包括指定类型的bean,存在多个时判断是否有标记Primary的bean。
	 * 
	 * @param type bean的类型
	 * @return 存在则返回true
	 */
	public boolean containsBean(Class<?> type) {
		String[] names = factory.getBeanNamesForType(type);
		List<String> beans=new ArrayList<>();
		for(String name:names){
			if(isIgnored(name)){
				continue;
			}
			if(factory.getBeanDefinition(name).isPrimary()){
				return true;
			}
			beans.add(name);
		}
		return beans.size()==1;
	}

	/**
	 * 是否需要忽略的Bean
	 * @param beanName bean名称
	 * @return 忽略返回true
	 */
	private boolean isIgnored(String beanName) {
		Class<?> realType = AopUtils.getTargetClass(factory.getBean(beanName));
		return initializer.getIgnore().contains(realType);
	}

	/**
	 * 获取组件列表。 <br>
	 * @param <T> 结果类型
	 * @param componentType
	 *            组件类型
	 * @return 组件实现列表
	 */
	public <T> List<T> getComponents(Class<T> componentType) {
		Map<String, T> map = applicationContext.getBeansOfType(componentType);
		if (map != null) {
			Map<Class<?>,T> components=new HashMap<>();
			for (Map.Entry<String, T> entry : map.entrySet()) {
				if(isIgnored(entry.getKey())){
					continue;
				}
				T bean = entry.getValue();
				Class<?> type=AopUtils.getTargetClass(bean);
				Class<?> clazz= ResolvableType.forClass(type).as(componentType)
						.getGeneric(0).resolve();
				if(clazz==null||Annotation.class.equals(clazz)){
					clazz=type;
				}
				if (!components.containsKey(clazz) || factory.getBeanDefinition(entry.getKey()).isPrimary()) {
					components.put(clazz, bean);
				}
			}
			return new ArrayList<>(components.values());
		}
		return new ArrayList<>();
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.factory = (ConfigurableListableBeanFactory) beanFactory;
	}

}
