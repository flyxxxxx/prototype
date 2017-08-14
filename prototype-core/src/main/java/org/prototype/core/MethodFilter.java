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

/**
 * 方法过滤
 * @author lj
 *
 * @param <T> 支持的注解（可选）
 */
@FunctionalInterface
public interface MethodFilter<T extends Annotation> {
	
	/**
	 * 执行过滤
	 * @param args 调用参数
	 * @param chain 方法链
	 * @return 执行结果
	 * @throws Exception 异常
	 */
	Object doFilter(Object[] args,MethodChain chain) throws Exception;
}
