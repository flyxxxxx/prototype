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

import java.util.concurrent.Executor;

/**
 * 并发 执行管理器. <br>
 * @author lj
 *
 */
public interface ExecutorManager {

	/**
	 * 获取指定对象的Executor
	 * @param object 对象
	 * @return Executor实例
	 */
	Executor getExecutor(Object object);
	/**
	 * 获取异步执行线程的Executor
	 * @param object 对象
	 * @return Executor实例
	 */
	Executor getAsyncExecutor(Object object);
}
