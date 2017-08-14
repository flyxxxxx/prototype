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
 * 注解访问接口。 <br>
 * 通过类、成员变量及方法均获取注解.
 * 
 * @author flyxxxxx@163.com
 *
 */
public interface AnnotatedAccessor {

	/**
	 * 获取所有注解
	 * @return 所有注解
	 */
	Annotation[] getAnnotations();

	/**
	 * 获取指定类型的注解
	 * @param <T> 结果类型
	 * @param annotationClass 注解类型
	 * @return 注解
	 */
	<T extends Annotation> T getAnnotation(Class<T> annotationClass);

	/**
	 * 获取注解的注解. <br>
	 * 类似于{@link org.prototype.business.BusinessDefine}与{@link org.prototype.core.Prototype}之间的关系. 
	 * 调用此方法参数为{@link org.prototype.core.Prototype}时，将会获取到注解{@link org.prototype.business.BusinessDefine}。
	 * 只支持一级级联关系的注解.
	 * @param annotationClass 注解类型
	 * @return 有指定注解的注解
	 */
	Annotation[] getAnnotationByMeta(Class<? extends Annotation> annotationClass);
}
