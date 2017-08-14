package org.prototype.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 并行执行多个方法. <br>
 * <pre>
 * 主要用于将多个可能耗时的任务（如多次网络请求）并发执行，在所有并发请求执行完成之后再执行有Fork注解的方法（或先执行当前方法再并发执行）.
 * 1、并行的方法至少要有两个以上。
 * 2、并行的方法返回值类型只能是void.
 * 3、并行的方法允许有任何Spring中的bean或能够通过接口{@link org.prototype.core.BeanInjecter}注入的对象
 * 4、所有并行的方法执行完成后，定义Fork注解的方法开始执行或执行完成（根据注解属性after的值决定）
 * 	&#064;Fork(value = { "m1", "m2" })
	public void fork() {
		//...
	}
	void m1() throws InterruptedException{
		Thread.sleep(1000);
	}
	void m2() throws InterruptedException{
		Thread.sleep(1000);
	}
	以上执行方法fork时，总用时约1000毫秒。
 * </pre>
 * 
 * @author lj
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Fork {

	/**
	 * 需要并发执行的方法名（方法的参数来源于Spring中的bean，注入方式参考Inject注解）. <br>
	 * 
	 * @return 需要并发执行的方法名
	 */
	String[] value();
	
	/**
	 * {@link java.util.concurrent.Executor Executor}
	 * @return java.util.concurrent.Executor的bean名称
	 */
	String executor() default "";

	/**
	 * 决定并发在当前方法之前还是之后执行（默认为之前）
	 * 
	 * @return 在当前方法之前还是之后执行
	 */
	boolean after() default false;
}
