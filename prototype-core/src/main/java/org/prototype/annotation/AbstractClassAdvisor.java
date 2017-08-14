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
package org.prototype.annotation;

import java.util.HashSet;
import java.util.Set;

import org.prototype.core.ClassAdvisor;
import org.prototype.core.ClassBuilder;
import org.prototype.core.ClassFactory;
import org.prototype.core.Errors;

/**
 * 抽象的类适配器实现. <br>
 * 在{@link #beforeLoad(ClassBuilder, Errors)}方法中，通过调用{@link #matches(ClassBuilder, Errors)}方法，将匹配成功的类名暂存。
 * 在{@link #onComplete(ClassFactory, Errors)}方法中，将beforeLoad方法暂存的类名循环，依次加载对应的类，然后调用{@link #register(ClassFactory, Class)}方法完成对象的注册.
 * 
 * @author flyxxxxx@163.com
 *
 */
public abstract class AbstractClassAdvisor implements ClassAdvisor {

	/**
	 * 匹配到的类名
	 */
	private Set<String> classes = new HashSet<>();

	/**
	 * {@inheritDoc}
	 * 匹配成交的类名将暂存
	 */
	@Override
	public final void beforeLoad(ClassBuilder builder, Errors errors) {
		if (matches(builder, errors)) {
			classes.add(builder.getName());
		}
	}

	/**
	 * 检查类是否需要进行后处理. <br>
	 * 也可在此方法进行类的预处理
	 * @param builder 类构建器
	 * @param errors 处理中的错误
	 * @return 需要后处理返回true
	 */
	protected abstract boolean matches(ClassBuilder builder, Errors errors);

	/**
	 * {@inheritDoc}
	 * 对暂存的类循环处理，加载后进行注册.
	 */
	@Override
	public final void onComplete(ClassFactory factory, Errors errors) {
		for (String className : classes) {
			Class<?> clazz = factory.loadClass(className);
			register(factory, clazz);
		}
	}

	/**
	 * 创建类或注册类
	 * @param factory 类工厂
	 * @param clazz 已经加载的类
	 */
	protected abstract  void register(ClassFactory factory, Class<?> clazz);

}
