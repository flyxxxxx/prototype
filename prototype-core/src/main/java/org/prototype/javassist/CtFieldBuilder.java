package org.prototype.javassist;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.prototype.core.AnnotationBuilder;
import org.prototype.core.ClassBuilder;
import org.prototype.core.FieldBuilder;
import org.prototype.reflect.AnnotationUtils;

import javassist.CannotCompileException;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.SignatureAttribute.ClassType;
import javassist.bytecode.SignatureAttribute.TypeArgument;

class CtFieldBuilder implements FieldBuilder {

	private CtField field;

	private List<javassist.bytecode.annotation.Annotation> newAnnotations = new ArrayList<>();

	private ClassFactoryImpl factory;

	private boolean setAndGet;

	public CtFieldBuilder(ClassFactoryImpl factory, CtField field,boolean setAndGet) {
		this.factory = factory;
		this.field = field;
		this.setAndGet=setAndGet;
	}

	@Override
	public Annotation[] getAnnotations() {
		try {
			return AnnotationUtils.getAnnotations(field.getAnnotations());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Class not found", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		try {
			return (T) field.getAnnotation(annotationClass);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Class not found", e);
		}
	}

	@Override
	public Annotation[] getAnnotationByMeta(Class<? extends Annotation> annotationClass) {
		try {
			return AnnotationUtils.getAnnotationByMeta(field.getAnnotations(), annotationClass);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Class not found", e);
		}
	}

	@Override
	public void create() {
		if (!newAnnotations.isEmpty()) {
			ConstPool cp = field.getFieldInfo().getConstPool();
			AnnotationsAttribute attr = (AnnotationsAttribute) field.getFieldInfo()
					.getAttribute(AnnotationsAttribute.visibleTag);
			if (attr == null) {
				attr = new AnnotationsAttribute(cp, AnnotationsAttribute.visibleTag);
			}
			for (javassist.bytecode.annotation.Annotation ann : newAnnotations) {
				attr.addAnnotation(ann);
			}
			field.getFieldInfo().addAttribute(attr);
			newAnnotations.clear();
		}
		try {
			field.getDeclaringClass().addField(field);
			if (setAndGet) {
				factory.newSetGetMethod(field.getDeclaringClass(), field.getName(), factory.loadClass(field.getType().getName()));
			}
		} catch (CannotCompileException|NotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ClassBuilder getClassBuilder() {
		return factory.getClassBuilder(field.getDeclaringClass());
	}

	@Override
	public String getName() {
		return field.getName();
	}

	@Override
	public AnnotationBuilder getAnnotationBuilder(Class<? extends Annotation> annotationClass) {
		ConstPool cp = field.getFieldInfo().getConstPool();
		AnnotationsAttribute attr = (AnnotationsAttribute) field.getFieldInfo()
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
						annotation.annotationType().getName(), field.getFieldInfo().getConstPool());
				for (Method method : annotation.annotationType().getMethods()) {
					if (method.getParameterTypes().length > 0 || void.class.equals(method.getReturnType())) {
						continue;
					}
					ann.addMemberValue(method.getName(), CtAnnotationUtils
							.getMemberValue(field.getFieldInfo().getConstPool(), method.invoke(annotation)));
				}
				newAnnotations.add(ann);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getType() {
		try {
			return field.getType().getName();
		} catch (NotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setSignature(Class<?>... typeArguments) {
		List<TypeArgument> arguments = new ArrayList<>();
		for (Class<?> typeArgument : typeArguments) {
			arguments.add(new TypeArgument(new ClassType(typeArgument.getName())));
		}
		ConstPool pool = field.getFieldInfo().getConstPool();
		try {
			SignatureAttribute attribute = new SignatureAttribute(pool,
					new ClassType(field.getType().getName(), arguments.toArray(new TypeArgument[arguments.size()]))
							.encode());
			field.getFieldInfo().addAttribute(attribute);
		} catch (NotFoundException e) {
			throw new RuntimeException(e);
		}

	}

}
