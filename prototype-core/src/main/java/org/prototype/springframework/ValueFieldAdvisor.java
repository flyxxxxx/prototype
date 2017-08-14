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
package org.prototype.springframework;

import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.prototype.core.ChainOrder;
import org.prototype.core.ClassBuilder;
import org.prototype.core.ClassScaner;
import org.prototype.core.Errors;
import org.prototype.core.FieldAdvisor;
import org.prototype.core.FieldBuilder;
import org.prototype.core.FieldInvoker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Spring {@link org.springframework.beans.factory.annotation.Value}注解的支持. <br>
 * 
 * <pre>
 * Value注解必须用在原型类的静态成员变量上：
 * &#064;Value("${prototype.apiRepository}
 * private static String apiRepository;//不可使用final关键字修饰!
 * 此实现类确保在任意spring环境下，均能获取Value注解的全部功能（在当前使用的Spring版本下的全部功能）。
 * </pre>
 * 
 * @author flyxxxxx@163.com
 *
 */
@Component
@Order(ChainOrder.VERY_HIGH)
public class ValueFieldAdvisor implements FieldAdvisor {

	@Resource
	private ApplicationContext context;//spring上下文
	
	@Resource
	private ClassScaner scaner;//类扫描

	private AtomicInteger sequence = new AtomicInteger(0);// 类名序号

	/**
	 * 这里采用向spring注册一个有相同成员变量的bean来获取Value注解的值，最后将注册的bean注销.
	 */
	@Override
	public void afterLoad(FieldInvoker invoker, Errors errors) {
		Value value = invoker.getAnnotation(Value.class);
		if (value == null) {// 没有注解的忽略之
			return;
		}
		ClassBuilder builder = scaner.getClassFactory()
				.newClass(ValueFieldAdvisor.class.getName() + sequence.incrementAndGet());// 要注册的bean
		builder.getAnnotationBuilder(Configuration.class);
		FieldBuilder fb = builder.newField(Modifier.PUBLIC, invoker.getName(), invoker.getType(), false);
		fb.getAnnotationBuilder(Value.class).setAttribute("value", value.value());// 构造一个相同的成员变量（非静态的）
		fb.create();
		Class<?> clazz = builder.create();
		BeanDefinitionBuilder define = BeanDefinitionBuilder.genericBeanDefinition(clazz);
		define.setScope(BeanDefinition.SCOPE_PROTOTYPE);
		DefaultListableBeanFactory registry = (DefaultListableBeanFactory) scaner.getBeanFactory();
		registry.registerBeanDefinition(clazz.getName(), define.getRawBeanDefinition());// 注册bean
		try {
			Object bean = context.getBean(clazz);// 获取bean
			Object v = bean.getClass().getField(invoker.getName()).get(bean);// 获取bean的成员变量值
			invoker.setValue(v);// 将bean的相同名称成员变量的值赋予业务类的成员变量
			registry.removeBeanDefinition(clazz.getName());// 注销bean
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			throw new RuntimeException("System error", e);
		}
	}

}
