package org.prototype.demo;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.prototype.core.ParameterInject;

/**
 * 计数
 * @author lj
 *
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ParameterInject
public @interface Counter {

}
