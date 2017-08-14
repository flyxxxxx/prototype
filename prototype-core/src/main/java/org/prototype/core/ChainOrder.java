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

/**
 * 责任链顺序
 * @author flyxxxxx@163.com
 *
 */
public interface ChainOrder {

	/**
	 * 优先级：非常高，值为{@value}，OverloadAsync/Async的优先级
	 */
	int VERY_HIGH = -20000;
	/**
	 * 优先级：高，值为{@value}，Catch/HystrixCommand的优先级
	 */
	int HIGH = -10000;
	/**
	 * 优先级：中，值为{@value}，Transactional的优先级
	 */
	int MIDDLE = 0;
	/**
	 * 优先级：低，值为{@value}
	 */
	int LOWER = 10000;
	/**
	 * 优先级：非常低，值为{@value}
	 */
	int VERY_LOWER = 20000;
	
}
