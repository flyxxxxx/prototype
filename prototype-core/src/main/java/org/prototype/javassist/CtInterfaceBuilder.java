package org.prototype.javassist;

import org.prototype.core.InterfaceBuilder;
import org.prototype.core.MethodBuilder;

import javassist.CannotCompileException;
import javassist.CtClass;
import lombok.extern.slf4j.Slf4j;
@Slf4j
class CtInterfaceBuilder implements InterfaceBuilder{

	private ClassFactoryImpl factory;
	private CtClass clazz;

	public CtInterfaceBuilder(ClassFactoryImpl factory, CtClass clazz) {
		this.factory=factory;
		this.clazz=clazz;
	}

	@Override
	public MethodBuilder newMethod(Class<?> returnType, String name, Class<?>[] parameterTypes,
			Class<? extends Throwable>[] throwableTypes) {
		return factory.abstractMethod(clazz, returnType, name, parameterTypes, throwableTypes);
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
		try {
			return clazz.toClass(Thread.currentThread().getContextClassLoader(), null);
		} catch (CannotCompileException e) {
			log.warn("Compile class "+clazz.getName()+" error", e);
			try {
				return Thread.currentThread().getContextClassLoader().loadClass(clazz.getName());
			} catch (ClassNotFoundException e1) {
				throw new RuntimeException(e1);
			}
		}
	}

	@Override
	public MethodBuilder newMethod(String code) {
		return factory.newMethod(clazz, code);
	}

}
