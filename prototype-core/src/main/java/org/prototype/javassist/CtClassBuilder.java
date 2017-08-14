package org.prototype.javassist;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prototype.core.AnnotationBuilder;
import org.prototype.core.ClassBuilder;
import org.prototype.core.ConstructorBuilder;
import org.prototype.core.Errors;
import org.prototype.core.FieldBuilder;
import org.prototype.core.MethodBuilder;
import org.prototype.core.MethodFilter;
import org.prototype.reflect.AnnotationUtils;
import org.springframework.util.Assert;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * javassist类构建器实现. <br>
 * 
 * @author flyxxxxx@163.com
 *
 */
@Slf4j
class CtClassBuilder implements ClassBuilder {

	/**
	 * javassist类
	 */
	private CtClass clazz;
	/**
	 * 类声明的方法
	 */
	@Getter
	private List<CtMethod> declaredMethods = new ArrayList<>();
	/**
	 * 给类添加的注解
	 */
	@Getter
	private List<javassist.bytecode.annotation.Annotation> newAnnotations = new ArrayList<>();

	/**
	 * 方法构建器
	 */
	private Map<CtMethod, CtMethodBuilder> builders = new HashMap<>();

	private ClassFactoryImpl factory;

	/**
	 * 构造
	 * 
	 * @param context
	 *            上下文
	 * @param clazz
	 *            javassist类
	 */
	public CtClassBuilder(ClassFactoryImpl factory, CtClass clazz) {
		this.factory = factory;
		this.clazz = clazz;
		declaredMethods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
	}

	/**
	 * 获取方法的构建器
	 * 
	 * @param method
	 *            方法
	 * @return
	 */
	public CtMethodBuilder getCtMethodBuilder(CtMethod method) {
		CtMethodBuilder rs = builders.get(method);
		if (rs == null) {
			synchronized (builders) {
				rs = new CtMethodBuilder(factory, method);
				builders.put(method, rs);
			}
		}
		return rs;
	}

	@Override
	public Annotation[] getAnnotations() {
		Map<String, Annotation> map = new HashMap<>();
		CtClass ctClass = clazz;
		try {
			while (ctClass != null && !Object.class.getName().equals(ctClass.getName())) {
				addAnnotations(map, ctClass, ctClass != clazz);
				ctClass = ctClass.getSuperclass();
			}
			return map.values().toArray(new Annotation[map.size()]);
		} catch (ClassNotFoundException | NotFoundException e) {
			throw new RuntimeException("Class not found", e);
		}
	}

	/**
	 * 添加类中的注解到映射
	 * 
	 * @param name_annotation
	 *            注解类名与注解的映射
	 * @param clazz
	 *            要查询的类
	 * @param inhert
	 *            是否考虑继承
	 * @throws ClassNotFoundException
	 */
	private void addAnnotations(Map<String, Annotation> name_annotation, CtClass clazz, boolean inhert)
			throws ClassNotFoundException {
		for (Object object : clazz.getAnnotations()) {
			Annotation annotation = (Annotation) object;
			String name = annotation.annotationType().getName();
			if (name_annotation.containsKey(name)) {
				continue;
			}
			if (inhert && annotation.annotationType().getAnnotation(Inherited.class) == null) {
				continue;
			}
			name_annotation.put(name, annotation);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		Assert.notNull(annotationClass);
		for (Annotation annotation : getAnnotations()) {// 循环检查注解
			if (annotation.annotationType().equals(annotationClass)) {
				return (T) annotation;
			}
		}
		return null;
	}

	@Override
	public Annotation[] getAnnotationByMeta(Class<? extends Annotation> annotationClass) {
		Assert.notNull(annotationClass);
		return AnnotationUtils.getAnnotationByMeta(getAnnotations(), annotationClass);
	}

	@Override
	public String getName() {
		return clazz.getName();
	}

	@Override
	public MethodBuilder findMethod(String methodName, Class<?>... parameterType) {
		Assert.hasLength(methodName);
		CtMethod method = CtMethodUtils.findMethod(clazz, methodName, parameterType);
		return method == null ? null : getCtMethodBuilder(method);
	}

	@Override
	public MethodBuilder[] findMethods(String methodName) {
		Assert.hasLength(methodName);
		List<CtMethod> methods = CtMethodUtils.findMethod(clazz, methodName);
		MethodBuilder[] rs = new MethodBuilder[methods.size()];
		for (int i = 0, k = rs.length; i < k; i++) {
			rs[i] = getCtMethodBuilder(methods.get(i));
		}
		return rs;
	}

	@Override
	public Class<?> create() {
		if (clazz.isFrozen()) {
			try {
				return Thread.currentThread().getContextClassLoader().loadClass(clazz.getName());
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Class not found", e);
			}
		}
		if (!newAnnotations.isEmpty()) {
			AnnotationsAttribute attr = (AnnotationsAttribute) clazz.getClassFile()
					.getAttribute(AnnotationsAttribute.visibleTag);
			if (attr == null) {
				attr = new AnnotationsAttribute(clazz.getClassFile().getConstPool(), AnnotationsAttribute.visibleTag);
			}
			for (javassist.bytecode.annotation.Annotation ann : newAnnotations) {
				attr.addAnnotation(ann);
			}
			clazz.getClassFile().addAttribute(attr);
			newAnnotations.clear();
		}
		try {
			return clazz.toClass(Thread.currentThread().getContextClassLoader(), null);
		} catch (CannotCompileException e) {
			log.warn("Compile class " + clazz.getName() + " error", e);
			try {
				return Thread.currentThread().getContextClassLoader().loadClass(clazz.getName());
			} catch (ClassNotFoundException e1) {
				throw new RuntimeException(e1);
			}
		}
	}

	@Override
	public MethodBuilder[] findDeclaredMethods(Class<? extends Annotation> annotationClass) {
		Assert.notNull(annotationClass);
		List<MethodBuilder> list = new ArrayList<>();
		try {
			for (CtMethod method : declaredMethods) {
				if (method.getAnnotation(annotationClass) != null) {
					list.add(getCtMethodBuilder(method));
				}
			}
			return list.toArray(new MethodBuilder[list.size()]);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Class not found", e);
		}
	}

	@Override
	public MethodBuilder newMethod(int modifiers, Class<?> returnType, String name, Class<?>[] parameterTypes,
			Class<? extends Throwable>[] throwableTypes, MethodFilter<?>... filter) {
		Assert.notNull(filter);
		boolean debug = log.isDebugEnabled();
		String code = CtMethodUtils.buildNewMethod(ClassScanerImpl.addBean(Arrays.asList(filter)), debug,
				clazz.getName(), returnType.getName(), name, parameterTypes);
		return newMethod(modifiers, returnType, name, parameterTypes, throwableTypes, code);
	}

	@SuppressWarnings("unchecked")
	@Override
	public MethodBuilder newMethod(int modifiers, Class<?> returnType, String name, Class<?>[] parameterTypes,
			Class<? extends Throwable>[] throwableTypes, String bodySrc) {
		Assert.notNull(returnType);
		Assert.hasLength(name);
		Class<?>[] pts = parameterTypes == null ? new Class<?>[0] : parameterTypes;
		Class<? extends Throwable>[] tsClasses = throwableTypes == null
				? (Class<? extends Throwable>[]) Array.newInstance(Class.class, 0) : throwableTypes;
		CtMethodBuilder rs = factory.newMethod(clazz, modifiers, returnType, name, pts, tsClasses, bodySrc);
		rs.setNewMethod(true);
		return rs;
	}
	
	/**
	 * 创建序列化ID成员变量
	 * @return 序列化ID成员变量
	 */
	FieldBuilder newSerialVersionUIDField(){
		try{
			long value=1L*clazz.getName().hashCode()*clazz.getName().hashCode();
			CtField field = CtField.make("private static final long serialVersionUID = "+value+"L;", clazz);
			return new CtFieldBuilder(factory, field,false);
		} catch (CannotCompileException e) {
			throw new RuntimeException(e);
		}
	}

	// @Override
	public FieldBuilder newField(int modifiers, String name, Class<?> type, boolean setAndGet) {
		Assert.hasLength(name);
		Assert.notNull(type);
		try {
			CtField field = new CtField(clazz.getClassPool().get(type.getName()), name, clazz);
			field.setModifiers(modifiers);
			return new CtFieldBuilder(factory, field,setAndGet);
		} catch (CannotCompileException | NotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ClassBuilder getSuperClassBuilder() {
		if (Object.class.getName().equals(clazz.getName())) {
			return null;
		}
		try {
			return factory.getClassBuilder(clazz.getSuperclass());
		} catch (NotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public MethodBuilder findUniqueMethod(String methodName, Errors errors, Class<?> annotationClass) {
		Assert.hasLength(methodName);
		Assert.notNull(errors);
		MethodBuilder[] mas = findMethods(methodName);
		Map<String, List<MethodBuilder>> map = new HashMap<>();
		for (MethodBuilder ma : mas) {
			String className = ma.getClassBuilder().getName();
			List<MethodBuilder> list = map.get(className);
			if (list == null) {
				list = new ArrayList<>();
				map.put(className, list);
			}
			list.add(ma);
		}
		ClassBuilder ca = this;
		while (ca != null) {
			List<MethodBuilder> list = map.get(ca.getName());
			if (list == null) {
				return null;
			}
			int k = list.size();
			if (k == 1) {
				return list.get(0);
			} else if (k > 1) {
				if (errors != null) {
					errors.add("method.morethanone", toString(),
							annotationClass == null ? "" : annotationClass.getName(), methodName);
				}
				return null;
			} else {
				ca = ca.getSuperClassBuilder();
			}
		}
		return null;
	}

	@Override
	public AnnotationBuilder getAnnotationBuilder(Class<? extends Annotation> annotationClass) {
		Assert.notNull(annotationClass);
		ConstPool cp = clazz.getClassFile().getConstPool();
		AnnotationsAttribute attr = (AnnotationsAttribute) clazz.getClassFile()
				.getAttribute(AnnotationsAttribute.visibleTag);
		AnnotationBuilderImpl rs = AnnotationBuilderImpl.create(cp, attr, annotationClass);
		newAnnotations.add(rs.getAnnotation());
		return rs;
	}

	@Override
	public void copyAnnotations(Annotation[] annotations) {
		Assert.notNull(annotations);
		if (annotations.length == 0) {
			return;
		}
		ConstPool cp = clazz.getClassFile().getConstPool();
		for (Annotation annotation : annotations) {
			newAnnotations.add(CtAnnotationUtils.createAnnotation(cp, annotation));
		}
	}

	// @Override
	public ConstructorBuilder[] listConstructors(boolean onlyPublic) {
		List<CtConstructorBuilder> list = new ArrayList<>();
		for (CtConstructor constructor : clazz.getConstructors()) {
			if (onlyPublic) {
				if (!Modifier.isPublic(constructor.getModifiers())) {
					continue;
				}
			}
			list.add(new CtConstructorBuilder(factory, constructor));
		}
		return list.toArray(new CtConstructorBuilder[list.size()]);
	}

	@Override
	public boolean enableConstructInject(Errors errors) {
		CtConstructor[] constructors = clazz.getConstructors();
		if (constructors.length != 1) {
			errors.add("construct.unique", clazz.getName());
			return false;
		}
		try {
			CtClass[] types = constructors[0].getParameterTypes();
			Object[][] annotations = constructors[0].getParameterAnnotations();
			boolean rs = true;
			for (int i = 0, k = types.length; i < k; i++) {
				if (!factory.getHelper().enableInject(factory.loadClass(types[i].getName()), annotations[i],false,errors)) {
					errors.add("construct.inject", clazz.getName(), Integer.toString(i));
					rs = false;
				}
			}
			return rs;
		} catch (NotFoundException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return clazz.getName();
	}

	@Override
	public ConstructorBuilder[] findConstructors() {
		CtConstructor[] cts = clazz.getConstructors();
		List<ConstructorBuilder> list = new ArrayList<>();
		for (CtConstructor ct : cts) {
			if (Modifier.isPublic(ct.getModifiers())) {
				list.add(new CtConstructorBuilder(factory, ct));
			}
		}
		return list.toArray(new ConstructorBuilder[list.size()]);
	}

	@Override
	public MethodBuilder newMethod(String code) {
		return factory.newMethod(clazz, code);
	}

}
