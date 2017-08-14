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

import java.util.Collection;

import org.springframework.beans.factory.BeanFactory;

/**
 * 类扫描接口. <br>
 * 实现此接口时，如果需要从Spring
 * context中查询的bean，应该实现ApplicationContextAware接口获取ApplicationContext，不能通过&#064;Autowired,&#064;Resource等方式，也不可使用&#064;PostConstruct&#064;PreDestroy等注解. 包括所有的ClassAdvisor、MethodAdvisor等.
 * 
 * @author flyxxxxx@163.com
 *
 */
public interface ClassScaner {
	
	/**
	 * 获取BeanFactory
	 * @return BeanFactory
	 */
	BeanFactory getBeanFactory();

	/**
	 * 扫描指定的类. <br>
	 * 在此方法中不可对类直接进行加载，可采用javassist/asm等技术预加载类，以便在类真实加载之前进行字节码的修改。
	 * @param classNames 类名集合
	 * @param errors 扫描到的错误提示
	 */
	void scan(Collection<String> classNames,Errors errors);
	
	/**
	 * 获得类工厂
	 * @return 类工厂
	 */
	ClassFactory getClassFactory();
}
