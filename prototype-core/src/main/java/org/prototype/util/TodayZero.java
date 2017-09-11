package org.prototype.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.prototype.core.ParameterInject;

/**
 * 获得今天0点. <br>
 * 可用于java.util.Date类型及子类型或java.util.Calendar类型的参数上
 * 
 * <pre>
 * 在业务类或其它有注解Prototype的类中：
 * void now(&#064;TodayZero Date today){//today这个参数会自动注解当前日期的0点
	}
 * </pre>
 * 
 * @author flyxxxxx@163.com
 *
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@ParameterInject
public @interface TodayZero {

}
