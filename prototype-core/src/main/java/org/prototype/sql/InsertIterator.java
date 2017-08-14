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
package org.prototype.sql;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;

/**
 * 保存数据用的循环. <br>
 * 使用它必须在实体类的ID字段上加jpa注解&#064;Id或任意其它名称为Id的注解.
 * 
 * @author lj
 *
 * @param <T>
 *            保存数据的类型
 */
public abstract class InsertIterator<T> extends CollectionIterator<T> {

	/**
	 * ID属性
	 */
	private Property idProp;

	/**
	 * 构造
	 * 
	 * @param coll
	 *            数据集合
	 */
	public InsertIterator(Collection<T> coll) {
		super(coll);
	}

	/**
	 * 保存数据之后修改ID属性值
	 * 
	 * @param index
	 *            对象在集合（构造参数）中的索引
	 * @param id
	 *            对象的ID值
	 * @throws InvocationTargetException 调用异常
	 * @throws IllegalAccessException 访问异常
	 */
	public final void setId(int index, Object id) throws InvocationTargetException, IllegalAccessException {
		if (idProp == null) {
			idProp = findIdProperty(list.get(0).getClass());
		}
		idProp.set(list.get(index), id);
	}

	/**
	 * 查询指定的类的ID属性
	 * @param clazz 指定的类
	 * @return
	 */
	private Property findIdProperty(Class<? extends Object> clazz) {
		for (Field field : clazz.getDeclaredFields()) {
			if (!Modifier.isStatic(field.getModifiers()) && isIdField(field)) {// ID
				Property property = new Property();
				field.setAccessible(true);
				property.field = field;
				return property;
			}
		}
		throw new UnsupportedOperationException("Id not found in class " + clazz.getName());
	}

	/**
	 * 判断是否ID成员变量
	 * 
	 * @param field
	 * @return
	 */
	private boolean isIdField(Field field) {
		for (Annotation ann : field.getAnnotations()) {
			if ("Id".equalsIgnoreCase(ann.annotationType().getSimpleName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 属性
	 * 
	 * @author lj
	 *
	 */
	private static class Property {

		private Field field;

		public final void set(Object target, Object id) {
			try {
				field.set(target, id);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

	}
}
