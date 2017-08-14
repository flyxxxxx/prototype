package org.prototype.javassist;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.prototype.core.FieldInvoker;
import org.prototype.reflect.AnnotationUtils;

class StaticFieldInvoker implements FieldInvoker{
	
	private Field field;

	public StaticFieldInvoker(Field field){
		this.field=field;
		field.setAccessible(true);
	}

	@Override
	public Annotation[] getAnnotations() {
		return field.getAnnotations();
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return field.getAnnotation(annotationClass);
	}

	@Override
	public Annotation[] getAnnotationByMeta(Class<? extends Annotation> annotationClass) {
		return AnnotationUtils.getAnnotationByMeta(field.getAnnotations(), annotationClass);
	}

	@Override
	public void setValue(Object value) {
		try {
			field.set(null, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return field.getName();
	}

	@Override
	public Class<?> getType() {
		return field.getType();
	}

}
