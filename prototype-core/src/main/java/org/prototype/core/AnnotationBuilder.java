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
 * 注解构建器. <br>
 * <pre>
 * 例：
 * MethodBuilder builder=...//或是ClassBuilder、FiledBuilder
 * builder.newAnnotation(Transactional.class).setAttribute("readOnly",true);
 * builder.create();//完成方法的创建（必须调用此方法）
 * </pre>
 * @author flyxxxxx@163.com
 *
 */
public interface AnnotationBuilder {

	/**
	 * 指定注解属性值并返回当前对象（链式操作）. <br>
	 * @param property 属性名
	 * @param value 属性值(可以是另一个AnnotationBuilder或其集合或数组)
	 * @return 当前类的实例(链式编程)
	 */
	AnnotationBuilder setAttribute(String property,Object value);

	/**
	 * 给指定的注解的属性，构建一个注解
	 * @param annotationClass 注解类型
	 * @return 新注解的构建器
	 */
	AnnotationBuilder newAnnotation(Class<?> annotationClass);

}
