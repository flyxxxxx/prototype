package org.prototype.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.prototype.core.ParameterInject;

/**
 * 获得今天0点. <br>
 * 可用于java.util.Date类型及子类型或java.util.Calendar类型的参数上
 * @author flyxxxxx@163.com
 *
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@ParameterInject
public @interface TodayZero {

}
