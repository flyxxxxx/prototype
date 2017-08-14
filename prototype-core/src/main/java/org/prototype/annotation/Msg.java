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
package org.prototype.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 消息. <br>
 * <pre>
	消息注解用来把一些通知、日志、异常等信息的处理逻辑与业务处理分隔.
	1、定义消息注解的方法可以返回任意对象(也就是返回类型可以是void以外的任意类型)，此对象将作为Message#getContent()的返回值。
	2、注解的属性type作为消息的类型，使用注解的类名作为消息的来源。
	3、如果返回Collection对象或其子类，将视为生成多个消息对象，返回null值，将不生成消息对象。
	4、构造消息的过程默认是同步的，如果需要异步生成消息对象，可在同一方法上加Spring的Async注解.
	5、消息的消费可通过注解{@link Subscribe}注解来完成.
	注意：此注解是在方法返回结果值后才执行发送消息的，因此，如下例所示：
	&#064;Msg&#064;Template("尊敬的客户${user.name}，您的验证码为${code}，五分钟内有效")
	String msg(Map&lt;String,Object&gt; params){return null}
	这个方法实际是先通过Template注解获取到消息内容后通过Msg注解才发送消息的。
 * </pre>
 * @author lj
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Msg {
	
	/**
	 * 分类
	 * @return 消息分类
	 */
	String type();
	
	/**
	 * 是否分布式消息
	 * @return 默认为false
	 */
	boolean distributed () default false;
}
