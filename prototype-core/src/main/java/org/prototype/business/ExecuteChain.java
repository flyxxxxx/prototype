package org.prototype.business;

import java.lang.annotation.Annotation;

/**
 * 业务执行链. <br>
 * 
 * @author lj
 *
 */
public interface ExecuteChain {


	/**
	 * 缓存成功
	 */
	static final int CACHED=9999;

	/**
	 * 执行成功
	 */
	static final int SUCCESS=1;
	/**
	 * 框架或组件未知错误
	 */
	static final int ERROR=0;
	/**
	 * 参数验证错误
	 */
	static final int VALIDATE=-4;
	/**
	 * 执行出现异常
	 */
	static final int EXCEPTION=-1;
	/**
	 * 执行被拒绝
	 */
	static final int REJECT=-2;
	/**
	 * 执行超时
	 */
	static final int TIMEOUT=-3;

	/**
	 * 执行链
	 * @throws Exception 异常
	 */
	void doChain() throws Exception;
	
	/**
	 * 获取当前服务
	 * @return 当前服务
	 */
	Service getService();
	
	/**
	 * 当前业务是否在异步中执行.
	 * @return 是否异步执行
	 */
	boolean isAsync();
	
	/**
	 * 当前业务类
	 * @return 当前业务类
	 */
	Class<?> getType();
	
	/**
	 * 设定执行的目标对象（业务类的实例）
	 * @param target 执行的目标对象
	 */
	void setTarget(Object target);
	
	/**
	 * 获取执行的目标对象
	 * @return 执行的目标对象
	 */
	Object getTarget();
	
	/**
	 * 输入参数
	 * @return 输入参数
	 */
	Object[] getParams();
	
	/**
	 * 设定输出结果
	 * @param result 输出结果
	 */
	void setResult(Object result);
	
	/**
	 * 设定输出错结果类型
	 * @param resultType 结果类型值
	 */
	void setResultType(int resultType);
	
	/**
	 * 添加验证错误
	 * @param error 验证错误
	 */
	void addValidateError(String error);
	
	/**
	 * 是否通过参数验证
	 * @return 是否通过参数验证
	 */
	boolean isValidated();
	
	/**
	 * 获取输出结果
	 * @return 输出结果
	 */
	Object getResult();
	
	/**
	 * 获取类的指定类型注解(如果类没有，则取启动类定义的注解)
	 * @param annotationType 注解类型
	 * @param <T> 注解类型
	 * @return 注解
	 */
	<T extends Annotation> T getAnnotation(Class<T> annotationType);

}
