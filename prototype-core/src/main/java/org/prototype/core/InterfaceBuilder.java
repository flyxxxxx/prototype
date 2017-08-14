package org.prototype.core;

/**
 * 接口构建器. <br>
 * 
 * @author lj
 *
 */
public interface InterfaceBuilder {

	/**
	 * 构造新的方法
	 * @param code 方法代码
	 * @return 方法构建器
	 */
	MethodBuilder newMethod(String code);
	/**
	 * 构造新的方法
	 * @param returnType 方法的返回类型
	 * @param name 方法名
	 * @param parameterTypes 参数类型
	 * @param throwableTypes 抛出的异常
	 * @return 方法构建器
	 */
	MethodBuilder newMethod(Class<?> returnType,String name,Class<?>[] parameterTypes,Class<? extends Throwable>[] throwableTypes);

	/**
	 * 完成接口的创建. <br>创建完成之后不可再修改类、方法、成员变量及其注解
	 * @return 新创建的类
	 */
	Class<?> create();
}
