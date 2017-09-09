package org.prototype.javassist;

import java.util.Collection;

import org.prototype.core.AnnotationBuilder;
import org.springframework.util.Assert;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.AnnotationMemberValue;
import lombok.Getter;

/**
 * 注解构建实现. <br>
 * 可以处理新的或原有的注解.
 * 
 * @author flyxxxxx@163.com
 *
 */
class AnnotationBuilderImpl implements AnnotationBuilder {

	/**
	 * 注解
	 */
	@Getter
	private Annotation annotation;
	/**
	 * 常量池
	 */
	private ConstPool pool;

	/**
	 * 使用已经存在的注解构造
	 * 
	 * @param pool
	 *            常量池
	 * @param annotation
	 *            已经存在的注解
	 */
	public AnnotationBuilderImpl(ConstPool pool, Annotation annotation) {
		this.pool = pool;
		this.annotation = annotation;
	}

	/**
	 * 构造新的注解
	 * 
	 * @param pool
	 *            常量池
	 * @param annotationClass
	 *            注解类
	 */
	public AnnotationBuilderImpl(ConstPool pool, Class<?> annotationClass) {
		this.pool = pool;
		annotation = new Annotation(annotationClass.getName(), pool);
	}

	/**
	 * 修改属性值
	 */
	@Override
	public AnnotationBuilder setAttribute(String property, Object value) {
		Assert.hasLength(property);
		Assert.notNull(value);
		if (AnnotationBuilderImpl.class.isInstance(value)) {
			AnnotationBuilderImpl builder = (AnnotationBuilderImpl) value;
			annotation.addMemberValue(property, new AnnotationMemberValue(builder.getAnnotation(), pool));
			return this;
		}
		Object[] array = null;
		if (Collection.class.isInstance(value)) {
			array = ((Collection<?>) value).toArray();
		} else if (value.getClass().isArray()) {
			array = (Object[]) value;
		}
		if (array != null && array.length > 0 && AnnotationBuilderImpl.class.isInstance(array[0])) {
			annotation.addMemberValue(property, CtAnnotationUtils.getMemberArray(pool, getJavassistAnnotations(array)));
		} else {
			annotation.addMemberValue(property, CtAnnotationUtils.getMemberValue(pool, value));
		}
		return this;
	}

	/**
	 * 转换为javassist注解
	 * @param array AnnotationBuilderImpl数组
	 * @return
	 */
	private Object getJavassistAnnotations(Object[] array) {
		AnnotationMemberValue[] values=new AnnotationMemberValue[array.length];
		int k=0;
		for(Object object:array){
			values[k++]=new AnnotationMemberValue(((AnnotationBuilderImpl)object).getAnnotation(),pool);
		}
		return values;
	}

	/**
	 * 创建或获取注解构建器
	 * 
	 * @param cp
	 *            常量池
	 * @param attr
	 *            已经存在的注解属性
	 * @param annotationClass
	 *            注解类
	 * @return 注解构建器
	 */
	public static AnnotationBuilderImpl create(ConstPool cp, AnnotationsAttribute attr, Class<?> annotationClass) {
		AnnotationBuilderImpl rs = null;
		if (attr != null) {
			javassist.bytecode.annotation.Annotation ann = attr.getAnnotation(annotationClass.getName());// 原有的
			if (ann != null) {
				rs = new AnnotationBuilderImpl(cp, ann);
			}
		}
		if (rs == null) {// 创建新的
			rs = new AnnotationBuilderImpl(cp, annotationClass);
		}
		return rs;
	}

	@Override
	public AnnotationBuilder newAnnotation(Class<?> annotationClass) {
		Annotation ann = new javassist.bytecode.annotation.Annotation(annotationClass.getName(), pool);
		return new AnnotationBuilderImpl(pool, ann);
	}

}
