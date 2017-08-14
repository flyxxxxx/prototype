package org.prototype.business;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.prototype.core.Prototype;

/**
 * 业务定义. <br>
 * 用于定义业务基类.
 * @author lj
 *
 */

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Prototype
public @interface BusinessDefine {
	
	/**
	 * 同步调用的责任链方法
	 * @return 同步调用的责任链方法
	 */
	BusinessMethod[] sync();
	
	/**
	 * 异步调用的方法
	 * @return 异步调用的方法
	 */
	BusinessMethod[] async();
	/**
	 * 入口方法名(不支持有参数)
	 * @return 入口方法名
	 */
	String execute() default "execute";
	
	/**
	 * 指定结果类型值，要求方法：void setResult(int result,String reason);
	 * @return 设定结果类型值
	 */
	String setResult() default "setResult";
	
	/**
	 * 添加验证错误
	 * @return 添加验证错误，要求方法：void addValidateError(String error);
	 */
	String addValidateError() default "addValidateError";
	
}
