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

import java.util.Map;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 是否有指定类的条件判断. <br>
 * 实现注解{@link ConditionalHasClass}的条件判断. 用于根据类是否存在决定bean是否加载到Spring上下文中.
 * 
 * @author flyxxxxx@163.com
 *
 */
public class HasClassCondition implements Condition {

	/**
	 * 判断注解中是否完成相关类的加载
	 */
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		Map<String, Object> map = metadata.getAnnotationAttributes(ConditionalHasClass.class.getName());
		Object object=map.get("value");
		if (ClassNotFoundException.class.isInstance(object)) {
			return false;
		}
		for (Object obj : (Object[]) object) {
			if (ClassNotFoundException.class.isInstance(obj)) {
				return false;
			}
		}
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			for (String name : (String[]) map.get("name")) {
				loader.loadClass(name);
			}
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

}
