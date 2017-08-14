/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.prototype;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * 开始Prototype模式. <br>
 * <pre>
 * Spring Boot环境下，在启动类(SpringBootApplication注解的类)可定义多个注解作为当前系统的公共配置，只要它们的元注解为{@link org.prototype.business.Executor}。
 * 在一般的Spring环境下，参考{@link PrototypeInitializer}
 * 例：
 * &#064;SpringBootApplication
 * &#064;EnableAsync//使用Servlet3异步方式提供Spring MVC的控制器
 * &#064;ConcurrentLimit(500)//整体并发限制为500
 * public class MyApplication{...}
 * </pre>
 * @author flyxxxxx@163.com
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({PrototypeConfiguration.class, PrototypeRegister.class })
public @interface EnablePrototype {
	
	/**
	 * 扫描的包名(默认扫描当前类所在的包及子包)
	 * @return 扫描的包名
	 */
	String[] value() default {};
	
	/**
	 * 忽略哪些组件
	 * @return 忽略哪些组件
	 */
	Class<?>[] ignore() default {};
	
}
