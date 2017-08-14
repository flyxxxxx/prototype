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

import org.prototype.core.BeanInjecter;

/**
 * Spring Aware接口对象的注入抽象类. <br>
 * @author lj
 *
 * @param <T> 注入对象的类型
 */

public abstract class AwareBeanInjecter<T> implements BeanInjecter<T> {
	
	/**
	 * 返回false
	 */
	@Override
	public boolean isNeedTransaction() {
		return false;
	}

	/**
	 * 返回true
	 */
	@Override
	public boolean containsBean() {
		return true;
	}

	/**
	 * 不支持
	 * @exception UnsupportedOperationException 总是抛出此异常
	 */
	@Override
	public T getBean(String beanName) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 返回false
	 */
	@Override
	public boolean containsBean(String beanName) {
		return false;
	}
	/**
	 * do nothing
	 */
	@Override
	public void afterUsed(T bean) {
		//do nothing
	}
}
