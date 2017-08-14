package org.nospringboot;

import org.prototype.EnablePrototype;
import org.prototype.PrototypeInitializer;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;

/**
 * 配置类
 * @author lj
 *
 */
@Component
@Primary
@EnablePrototype
@DefaultProperties(ignoreExceptions=RuntimeException.class)
public class PrototypeInitializerImpl extends PrototypeInitializer{

}
