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
 * 对方法进行适配
 * @author lj
 *
 */
@FunctionalInterface
public interface MethodAdvisor {

	/**
	 * 对方法进行匹配
	 * @param builder 方法构建器
	 * @param errors 处理中的错误
	 * @return 方法过滤
	 */
	MethodFilter<?> matches(MethodBuilder builder,Errors errors);

}
