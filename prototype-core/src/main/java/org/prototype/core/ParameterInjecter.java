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
import java.util.List;

/**
 * 参数注入接口. <br>
 * 与注解{@link ParameterInject}配合使用，还可同时实现Spring ApplicationContextAware接口.
 * 此接口可用于如用户会话信息的注入等.
 * @author lj
 *
 * @param <T> 注解的类型
 * @param <V> 注入的类型
 */
public interface ParameterInjecter<T extends Annotation,V> {
	
	/**
	 * 进行注解的验证
	 * @param annotation 注解
	 * @param type 参数类型
	 * @return 错误消息的关键字
	 */
	List<String> validate(T annotation,Class<V> type);
	/**
	 * 根据注解注入参数
	 * @param annotation 注解
	 * @param type 参数类型
	 * @return 注入的参数的值
	 */
	V inject(T annotation,Class<V> type);
}
