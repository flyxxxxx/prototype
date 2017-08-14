package org.prototype.core;

import java.lang.annotation.Annotation;

/**
 * 方法及构造方法的参数注解构建器
 * @author lj
 *
 */
public interface ParameterAnnotationsBuilder {

	/**
	 * 获取下一个参数的注解构建器
	 * @param parameterIndex 参数索引
	 * @param annotationClass 注解类
	 * @return 注解构建器
	 */
	@SuppressWarnings("unchecked")
	AnnotationBuilder[] getAnnotationBuilder(int parameterIndex,Class<? extends Annotation>... annotationClass);

	/**
	 * 将多个注解复制到指定参数上
	 * @param parameterIndex 参数索引
	 * @param annotations 注解
	 */
	void copyAnnotations(int parameterIndex,Annotation[] annotations);
}
