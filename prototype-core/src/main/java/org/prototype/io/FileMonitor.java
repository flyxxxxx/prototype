package org.prototype.io;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.prototype.core.Prototype;

/**
 * 对文件或目录的监控
 * @author lj
 *
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Prototype
public @interface FileMonitor {
	String value();//监控的目录或文件
	int deep() default 1;//，对于非目录（一般文件），此值无效。对于目录，0表示仅监控目录本身，1表示监控目录及子目录，2表示监控目录的子目录及子目录的子目录，3表示目录及下面的3级目录，以此类推。-1表示所有下级子目录。注意不要监控太多的文件(尤其是大量文件是否有更新或删除).
	String onCreate() default "onCreate";
	String onUpdate() default "onUpdate";
	String onDelete() default "onDelete";
	//扫描周期（秒）
	int period() default 10;
}
