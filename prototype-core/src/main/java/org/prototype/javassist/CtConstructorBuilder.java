package org.prototype.javassist;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.prototype.core.AnnotationBuilder;
import org.prototype.core.ClassBuilder;
import org.prototype.core.ConstructorBuilder;
import org.prototype.reflect.AnnotationUtils;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.ParameterAnnotationsAttribute;
import lombok.Getter;

class CtConstructorBuilder implements ConstructorBuilder {

	private ClassFactoryImpl factory;
	private CtConstructor constructor;
	private List<javassist.bytecode.annotation.Annotation> newAnnotations = new ArrayList<>();

	@Getter
	private ParameterAnnotationsBuilderImpl parameterAnnotationsBuilder;

	public CtConstructorBuilder(ClassFactoryImpl factory, CtConstructor constructor) {
		this.factory = factory;
		this.constructor = constructor;
		ParameterAnnotationsAttribute attribute = (ParameterAnnotationsAttribute) constructor.getMethodInfo()
				.getAttribute(ParameterAnnotationsAttribute.visibleTag);
		try {
			javassist.bytecode.annotation.Annotation[][] anns = attribute == null
					? new javassist.bytecode.annotation.Annotation[constructor.getParameterTypes().length][0]
					: attribute.getAnnotations();
			parameterAnnotationsBuilder = new ParameterAnnotationsBuilderImpl(
					constructor.getMethodInfo().getConstPool(), anns);
		} catch (NotFoundException e) {
			throw new RuntimeException("Class not found", e);
		}
	}

	@Override
	public AnnotationBuilder getAnnotationBuilder(Class<? extends Annotation> annotationClass) {
		ConstPool cp = constructor.getMethodInfo().getConstPool();
		AnnotationsAttribute attr = (AnnotationsAttribute) constructor.getMethodInfo()
				.getAttribute(AnnotationsAttribute.visibleTag);
		AnnotationBuilderImpl rs = AnnotationBuilderImpl.create(cp, attr, annotationClass);
		newAnnotations.add(rs.getAnnotation());
		return rs;
	}

	@Override
	public void copyAnnotations(Annotation[] annotations) {
		try {
			for (Annotation annotation : annotations) {
				javassist.bytecode.annotation.Annotation ann = new javassist.bytecode.annotation.Annotation(
						annotation.annotationType().getName(), constructor.getMethodInfo().getConstPool());
				for (Method method : annotation.annotationType().getMethods()) {
					if (method.getParameterTypes().length > 0 || void.class.equals(method.getReturnType())) {
						continue;
					}
					ann.addMemberValue(method.getName(), CtAnnotationUtils.getMemberValue(
							this.constructor.getMethodInfo().getConstPool(), method.invoke(annotation)));
				}
				newAnnotations.add(ann);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Annotation[] getAnnotations() {
		try {
			return AnnotationUtils.getAnnotations(constructor.getAnnotations());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Class not found", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		try {
			return (T) constructor.getAnnotation(annotationClass);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Class not found", e);
		}
	}

	@Override
	public Annotation[] getAnnotationByMeta(Class<? extends Annotation> annotationClass) {
		try {
			return AnnotationUtils.getAnnotationByMeta(constructor.getAnnotations(), annotationClass);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Class not found", e);
		}
	}

	@Override
	public ClassBuilder getClassBuilder() {
		return factory.getClassBuilder(constructor.getDeclaringClass());
	}

	@Override
	public int getModifiers() {
		return constructor.getModifiers();
	}

	@Override
	public Class<?>[] getParameterTypes() {
		try {
			CtClass[] types = constructor.getParameterTypes();
			Class<?>[] rs = new Class<?>[types.length];
			int k = 0;
			for (CtClass type : types) {
				rs[k] = factory.loadClass(type.getName());
			}
			return rs;
		} catch (NotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void create() {
		if (!newAnnotations.isEmpty()) {
			ConstPool cp = constructor.getMethodInfo().getConstPool();
			AnnotationsAttribute attr = (AnnotationsAttribute) constructor.getMethodInfo()
					.getAttribute(AnnotationsAttribute.visibleTag);
			if (attr == null) {
				attr = new AnnotationsAttribute(cp, AnnotationsAttribute.visibleTag);
			}
			for (javassist.bytecode.annotation.Annotation ann : newAnnotations) {
				attr.addAnnotation(ann);
			}
			constructor.getMethodInfo().addAttribute(attr);
			newAnnotations.clear();
		}
		if (parameterAnnotationsBuilder.hasParameterAnnotations()) {
			ParameterAnnotationsAttribute attribute = new ParameterAnnotationsAttribute(
					constructor.getMethodInfo().getConstPool(), ParameterAnnotationsAttribute.visibleTag);
			attribute.setAnnotations(parameterAnnotationsBuilder.getAnnotations());
			constructor.getMethodInfo().addAttribute(attribute);
		}
	}

}
