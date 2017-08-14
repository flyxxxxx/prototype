package org.prototype.io;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 将字节数组，输入流中的数据输出到返回结果
 * @author lj
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Stream {
	int bufferSize() default 4096;
	String inputCharset() default "";
	String outputCharset() default "";
	boolean closeInput() default true;
	boolean closeOutput() default true;
	FileConfig fileConfig() default @FileConfig;
}
