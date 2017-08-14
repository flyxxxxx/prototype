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
package org.prototype.reflect;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * 注解工具
 * @author flyxxxxx@163.com
 *
 */
public class AnnotationUtils {

	private AnnotationUtils(){
		//do nothing
	}

	/**
	 * 获取指定类型的注解
	 * @param <T> 结果类型
	 * @param annotations 注解
	 * @param annotationClass 注解类型
	 * @return 指定类型的注解或null
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Annotation> T getAnnotation(Object[] annotations,
			Class<T> annotationClass) {
		for(Object object :annotations){
			Annotation annotation=(Annotation) object;
			if(annotationClass.equals(annotation.annotationType())){
				return (T) annotation;
			}
		}
		return null;
	}

	/**
	 * 获取元注解（注解的注解）
	 * 
	 * @param annotations
	 *            注解
	 * @param annotationClass
	 *            注解类型
	 * @return 元数据
	 */
	public static Annotation[] getAnnotationByMeta(Object[] annotations,
			Class<? extends Annotation> annotationClass) {
		List<Annotation> list = new ArrayList<>();
		for (Object obj : annotations) {
			Class<? extends Annotation> cls = ((Annotation) obj).annotationType();
			if (annotationClass.equals(cls) || cls.getAnnotation(annotationClass) != null) {
				list.add((Annotation) obj);
			}
		}
		return list.toArray(new Annotation[ list.size()]);
	}

	/**
	 * 注解数组类型转换
	 * @param annotations 注解对象数组
	 * @return 注解数组
	 */
	public static Annotation[] getAnnotations(Object[] annotations){
		int len = annotations.length;
		Annotation[] rs = new Annotation[len];
		if (len > 0) {
			System.arraycopy(annotations, 0, rs, 0, len);
		}
		return rs;
	}

}
