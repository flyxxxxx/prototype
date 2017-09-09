package org.prototype.javassist;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prototype.core.ClassBuilder;
import org.prototype.core.ClassFactory;
import org.prototype.core.InterfaceBuilder;
import org.prototype.inject.InjectHelper;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javassist.CannotCompileException;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.SignatureAttribute.ClassType;
import javassist.bytecode.SignatureAttribute.MethodSignature;
import javassist.bytecode.SignatureAttribute.TypeArgument;
import lombok.Getter;

/**
 * 类创建工厂. <br>
 * 
 * @author lj
 *
 */
class ClassFactoryImpl implements ClassFactory {

	private static final Map<String, Class<?>> TYPES = new HashMap<>();

	static {
		Class<?>[] list = new Class<?>[] { boolean.class, byte.class, short.class, char.class, int.class, long.class,
				float.class, double.class };
		for (Class<?> c : list) {
			TYPES.put(c.getName(), c);
		}
	}
	private Map<String, WeakReference<CtClassBuilder>> builders = new HashMap<>();
	@Getter
	private ClassPool classPool = new ClassPool(true);

	@Getter
	private BeanDefinitionRegistry registry;

	@Getter
	private InjectHelper helper;

	public ClassFactoryImpl(BeanDefinitionRegistry registry, InjectHelper helper) {
		this.registry = registry;
		this.helper = helper;
		classPool.insertClassPath(new ClassPathImpl());
	}

	private class ClassPathImpl implements ClassPath {

		private PathMatchingResourcePatternResolver resolver=new PathMatchingResourcePatternResolver();

		@Override
		public InputStream openClassfile(String classname) throws NotFoundException {
			try {
				return find(classname).openStream();
			} catch (IOException e) {
				throw new NotFoundException("Class " + classname + " not found", e);
			}
		}

		@Override
		public URL find(String classname) {
			Resource resource = resolver.getResource("classpath:" + classname.replace('.', '/') + ".class");
			if(resource==null||!resource.exists()){
				return null;
			}
			try {
				return resource.getURL();
			} catch (IOException e) {
				throw new RuntimeException("IO error : "+classname, e);
			}
		}

		@Override
		public void close() {
			// do nothing
		}

	}

	@Override
	public Class<?> loadClass(String className) {
		Class<?> rs = TYPES.get(className);
		if (rs != null) {
			return rs;
		}
		int k = className.indexOf("[]");
		if (k != -1) {
			return loadArrayClass(loadClass(className.substring(0, k)), className.substring(k + 1));
		}
		try {
			return classPool.getClassLoader().loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Class '" + className + "' not found", e);
		}
	}

	/**
	 * 数组的类类型
	 * 
	 * @param componentType
	 *            组件类型
	 * @param suffix
	 *            后缀
	 * @return 数组的类类型
	 */
	private Class<?> loadArrayClass(Class<?> componentType, String suffix) {
		int k = suffix.length() / 2;
		Object object = Array.newInstance(componentType, 0);
		for (int i = 1; i < k; i++) {
			object = Array.newInstance(object.getClass(), 0);
		}
		return object.getClass();
	}

	@Override
	public ClassBuilder newClass(String className) {
		return newClass(className, Object.class, new Class<?>[0]);
	}

	@Override
	public ClassBuilder newClass(String className, Class<?> superClass, Class<?>... interfaceTypes) {
		CtClass clazz = classPool.makeClass(className);
		try {
			clazz.setSuperclass(getCtClass(superClass)[0]);
			if (interfaceTypes.length > 0) {
				clazz.setInterfaces(getCtClass(interfaceTypes));
			}
		} catch (NotFoundException | CannotCompileException e) {
			throw new RuntimeException("Class not found", e);
		}
		CtClassBuilder builder = new CtClassBuilder(this, clazz);
		for (Class<?> type : interfaceTypes) {
			if (Serializable.class.equals(type)) {
				builder.newSerialVersionUIDField().create();
				break;
			}
		}
		builders.put(className, new WeakReference<>(builder));
		return builder;
	}

	@Override
	public InterfaceBuilder newInterface(String interfaceName) {
		CtClass clazz = classPool.makeInterface(interfaceName);
		return new CtInterfaceBuilder(this, clazz);
	}

	CtClassBuilder getClassBuilder(CtClass clazz) {
		WeakReference<CtClassBuilder> reference = builders.get(clazz.getName());
		if (reference == null || reference.get() == null) {
			reference = new WeakReference<>(new CtClassBuilder(this, clazz));
			builders.put(clazz.getName(), reference);
		}
		return reference.get();
	}

	public CtMethodBuilder newMethod(CtClass clazz, String code) {
		try {
			CtMethod method = CtNewMethod.make(code, clazz);
			CtMethodBuilder builder = new CtMethodBuilder(this, method);
			builder.setNewMethod(true);
			CtClassBuilder ca = (CtClassBuilder) builder.getClassBuilder();
			ca.getDeclaredMethods().add(method);
			return builder;
		} catch (CannotCompileException e) {
			throw new RuntimeException("Compile error", e);
		}
	}

	public CtMethodBuilder newMethod(CtClass clazz, int modifiers, Class<?> returnType, String name,
			Class<?>[] parameterTypes, Class<? extends Throwable>[] throwableTypes, String code) {
		try {
			if (code.charAt(0) != '{') {
				code = '{' + code;
			}
			if (code.charAt(code.length() - 1) != '}') {
				code = code + '}';
			}
			CtMethod method = CtNewMethod.make(modifiers, getCtClass(new Class<?>[] { returnType })[0], name,
					getCtClass(parameterTypes), getCtClass(throwableTypes), code, clazz);
			CtMethodBuilder builder = new CtMethodBuilder(this, method);
			builder.setNewMethod(true);
			CtClassBuilder ca = (CtClassBuilder) builder.getClassBuilder();
			ca.getDeclaredMethods().add(method);
			return builder;
		} catch (CannotCompileException e) {
			throw new RuntimeException("Compile error", e);
		} catch (NotFoundException e) {
			throw new RuntimeException("Class not found", e);
		}
	}

	public CtMethodBuilder abstractMethod(CtClass clazz, Class<?> returnType, String name, Class<?>[] parameterTypes,
			Class<? extends Throwable>[] throwableTypes) {
		try {
			CtMethod method = CtNewMethod.abstractMethod(getCtClass(new Class<?>[] { returnType })[0], name,
					getCtClass(parameterTypes), getCtClass(throwableTypes), clazz);
			CtMethodBuilder builder = new CtMethodBuilder(this, method);
			builder.setNewMethod(true);
			CtClassBuilder ca = (CtClassBuilder) builder.getClassBuilder();
			ca.getDeclaredMethods().add(method);
			return builder;
		} catch (NotFoundException e) {
			throw new RuntimeException("Class not found", e);
		}
	}

	private CtClass[] getCtClass(Class<?>... classes) throws NotFoundException {
		CtClass[] rs = new CtClass[classes.length];
		for (int i = 0, k = classes.length; i < k; i++) {
			rs[i] = classPool.get(classes[i].getName());
		}
		return rs;
	}

	public void newSetGetMethod(CtClass clazz, String name, Class<?> type,Class<?>[] typeArguments) {
		try {
			CtMethod method = CtNewMethod.make(CtMethodUtils.buildGetMethod(name, type), clazz);
			setGetSignature(method,type,typeArguments);
			clazz.addMethod(method);
			method = CtNewMethod.make(CtMethodUtils.buildSetMethod(name, type), clazz);
			clazz.addMethod(method);
			//未处理set的泛型
		} catch (CannotCompileException e) {
			throw new RuntimeException("Compile error", e);
		}
	}

	private void setGetSignature(CtMethod method,Class<?> type,Class<?>[] typeArguments) {
		if(typeArguments==null||typeArguments.length==0){
			return;
		}
		List<TypeArgument> arguments = new ArrayList<>();
		for (Class<?> typeArgument : typeArguments) {
			arguments.add(new TypeArgument(new ClassType(typeArgument.getName())));
		}
		ClassType ct=new ClassType(type.getName(), arguments.toArray(new TypeArgument[arguments.size()]));
		MethodSignature signature=new MethodSignature(null,null,ct,null);
		method.setGenericSignature(signature.encode());
	}
}
