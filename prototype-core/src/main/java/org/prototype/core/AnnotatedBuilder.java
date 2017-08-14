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
 * 注解的构建支持. <br>
 * 
 * @author flyxxxxx@163.com
 *
 */
public interface AnnotatedBuilder extends AnnotatedAccessor{
	
	/**
	 * 获取已经存在的注解并进行修改(如果不存在则创建)
	 * @param annotationClass 注解类型
	 * @return 注解构建器
	 */
	AnnotationBuilder getAnnotationBuilder(Class<? extends Annotation> annotationClass);
	
	/**
	 * 将多个注解复制到当前类/方法/成员变量上
	 * @param annotations 注解
	 */
	void copyAnnotations(Annotation[] annotations);
}
