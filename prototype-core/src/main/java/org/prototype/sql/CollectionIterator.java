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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 集合循环器. <br>
 * 注解循环器只能使用一次，除非调用
 * 
 * @author lj
 *
 * @param <T> 循环对象的类型
 */
public abstract class CollectionIterator<T> implements Iterator<Object[]> {

	/**
	 * 数据列表
	 */
	List<T> list;

	/**
	 * 当前索引
	 */
	private int index;

	/**
	 * 构造
	 * 
	 * @param collection
	 *            列表
	 */
	public CollectionIterator(Collection<T> collection) {
		this.list = collection == null ? new ArrayList<T>()
				: ((collection instanceof List) ? (List<T>) collection : new ArrayList<T>(collection));
	}

	@Override
	public boolean hasNext() {
		return index < list.size();
	}

	/**
	 * 重置索引，可从循环的开始再次进行循环
	 */
	public void reset() {
		index = 0;
	}

	/**
	 * 下一组数据
	 * @param t 循环的对象
	 * @return 对象的属性
	 */
	public abstract Object[] next(T t);

	@Override
	public Object[] next() {
		if (index < list.size()) {
			Object[] os = next(list.get(index));
			index++;
			return os;
		}
		return null;
	}

	/**
	 * 不支持此方法的调用
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
