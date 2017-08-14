package org.prototype.javassist;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.prototype.core.AnnotationBuilder;
import org.prototype.core.ClassBuilder;
import org.prototype.core.Errors;
import org.prototype.core.MethodBuilder;
import org.prototype.reflect.AnnotationUtils;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.ParameterAnnotationsAttribute;
import lombok.Getter;
import lombok.Setter;

/**
 * javassist的方法访问实现
 * 
 * @author flyxxxxx@163.com
 *
 */
class CtMethodBuilder implements MethodBuilder {

	private ClassFactoryImpl factory;
	private CtMethod method;

	private List<javassist.bytecode.annotation.Annotation> newAnnotations = new ArrayList<>();

	@Getter
	private ParameterAnnotationsBuilderImpl parameterAnnotationsBuilder;

	@Setter
	private boolean newMethod;

	public CtMethodBuilder(ClassFactoryImpl factory, CtMethod method) {
		this.factory = factory;
		this.method = method;
		ParameterAnnotationsAttribute attribute = (ParameterAnnotationsAttribute) method.getMethodInfo()
				.getAttribute(ParameterAnnotationsAttribute.visibleTag);
		try {
			javassist.bytecode.annotation.Annotation[][] anns = attribute == null
					? new javassist.bytecode.annotation.Annotation[method.getParameterTypes().length][0]
					: attribute.getAnnotations();
			parameterAnnotationsBuilder = new ParameterAnnotationsBuilderImpl(method.getMethodInfo().getConstPool(),
					anns);
		} catch (NotFoundException e) {
			throw new RuntimeException("Class not found", e);
		}
	}

	@Override
	public ClassBuilder getClassBuilder() {
		return factory.getClassBuilder(method.getDeclaringClass());
	}

	@Override
	public String getName() {
		return method.getName();
	}

	@Override
	public String toString() {
		return CtMethodUtils.getDescription(method);
	}

	@Override
	public Annotation[] getAnnotations() {
		try {
			return AnnotationUtils.getAnnotations(method.getAnnotations());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Class not found", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		try {
			return (T) method.getAnnotation(annotationClass);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Class not found", e);
		}
	}

	@Override
	public Annotation[] getAnnotationByMeta(Class<? extends Annotation> annotationClass) {
		try {
			return AnnotationUtils.getAnnotationByMeta(method.getAnnotations(), annotationClass);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Class not found", e);
		}
	}

	@Override
	public String getReturnType() {
		try {
			return method.getReturnType().getName();
		} catch (NotFoundException e) {
			throw new RuntimeException("Class not found", e);
		}
	}

	@Override
	public AnnotationBuilder getAnnotationBuilder(Class<? extends Annotation> annotationClass) {
		ConstPool cp = method.getMethodInfo().getConstPool();
		AnnotationsAttribute attr = (AnnotationsAttribute) method.getMethodInfo()
				.getAttribute(AnnotationsAttribute.visibleTag);
		AnnotationBuilderImpl rs = AnnotationBuilderImpl.create(cp, attr, annotationClass);
		newAnnotations.add(rs.getAnnotation());
		return rs;
	}

	@Override
	public Class<?>[] getParameterTypes() {
		try {
			CtClass[] types = method.getParameterTypes();
			Class<?>[] rs = new Class<?>[types.length];
			int k = 0;
			for (CtClass type : types) {
				rs[k++] = factory.loadClass(type.getName());
			}
			return rs;
		} catch (NotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Class<?>[] getExceptionTypes(){
		try {
			CtClass[] types = method.getExceptionTypes();
			Class<?>[] rs = new Class<?>[types.length];
			int k = 0;
			for (CtClass type : types) {
				rs[k++] = factory.loadClass(type.getName());
			}
			return rs;
		} catch (NotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	static List<javassist.bytecode.annotation.Annotation> getAnnotations(ConstPool cp, Annotation[] annotations) {
		List<javassist.bytecode.annotation.Annotation> rs = new ArrayList<javassist.bytecode.annotation.Annotation>();
		try {
			for (Annotation annotation : annotations) {
				javassist.bytecode.annotation.Annotation ann = new javassist.bytecode.annotation.Annotation(
						annotation.annotationType().getName(), cp);
				for (Method method : annotation.annotationType().getMethods()) {
					if (method.getParameterTypes().length > 0 || void.class.equals(method.getReturnType())) {
						continue;
					}
					ann.addMemberValue(method.getName(),
							CtAnnotationUtils.getMemberValue(cp, method.invoke(annotation)));
				}
				rs.add(ann);
			}
			return rs;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void copyAnnotations(Annotation[] annotations) {
		newAnnotations.addAll(getAnnotations(method.getMethodInfo().getConstPool(), annotations));
	}

	@Override
	public void create() {
		if (!newAnnotations.isEmpty()) {
			ConstPool cp = method.getMethodInfo().getConstPool();
			AnnotationsAttribute attr = (AnnotationsAttribute) method.getMethodInfo()
					.getAttribute(AnnotationsAttribute.visibleTag);
			if (attr == null) {
				attr = new AnnotationsAttribute(cp, AnnotationsAttribute.visibleTag);
			}
			for (javassist.bytecode.annotation.Annotation ann : newAnnotations) {
				attr.addAnnotation(ann);
			}
			method.getMethodInfo().addAttribute(attr);
			newAnnotations.clear();
		}
		if (parameterAnnotationsBuilder.hasParameterAnnotations()) {
			ParameterAnnotationsAttribute attribute = new ParameterAnnotationsAttribute(
					method.getMethodInfo().getConstPool(), ParameterAnnotationsAttribute.visibleTag);
			attribute.setAnnotations(parameterAnnotationsBuilder.getAnnotations());
			method.getMethodInfo().addAttribute(attribute);
		}
		if (newMethod) {
			try {
				method.getDeclaringClass().addMethod(method);
			} catch (CannotCompileException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public int getModifiers() {
		return method.getModifiers();
	}

	@Override
	public boolean isNeedTransaction(Errors errors) {
		try {
			Object[][] anns = method.getParameterAnnotations();
			CtClass[] types = method.getParameterTypes();
			for (int i = 0, k = types.length; i < k; i++) {
				Class<?> clazz = factory.loadClass(types[i].getName());
				if (factory.getHelper().isNeetTransaction(clazz, anns[i])) {
					return true;
				}
			}
			return false;
		} catch (ClassNotFoundException | NotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private Object[][] getParameterAnnotations() {
		try {
			return method.getParameterAnnotations();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean enableInjectFrom(MethodBuilder builder, Errors errors) {
		Set<String> names = new HashSet<>();
		if (builder != null) {
			names = ((CtMethodBuilder) builder).getParameterNameSet();
		}
		Class<?>[] types = getParameterTypes();
		int k = 0;
		boolean rs = true;
		Object[][] annotations = getParameterAnnotations();
		for (String name : CtMethodUtils.getMethodParamNames(method)) {
			if (names.contains(name + "_" + types[k].getName())) {
				k++;
				continue;
			}
			if (!factory.getHelper().enableInject(types[k], annotations[k], true, errors)) {
				errors.add("method.inject", CtMethodUtils.getDescription(method), Integer.toString(k));
				rs = false;
			}
			k++;
		}
		return rs;
	}

	@Override
	public boolean enableInject(Errors errors) {
		return enableInjectFrom(null, errors);
	}

	/**
	 * 获取参数名集合
	 * 
	 * @return 参数名集合
	 */
	private Set<String> getParameterNameSet() {
		Set<String> rs = new HashSet<>();
		Class<?>[] types = getParameterTypes();
		if (types.length > 0) {
			int k = 0;
			for (String name : CtMethodUtils.getMethodParamNames(method)) {
				rs.add(name + "_" + types[k++].getName());
			}
		}
		return rs;
	}

}
