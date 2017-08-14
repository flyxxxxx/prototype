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

import org.prototype.core.MethodChain;
import org.prototype.core.PrototypeStatus;

import lombok.Getter;
import rx.subjects.PublishSubject;

/**
 * 消息. <br>
 * 
 * <pre>
 * 用于将消息的发送与处理解耦，避免处理端变化时需要修改多处代码，如以下场景：
 * 1、用户日志需要调用微服务或本地接口或通过MQ等不同形式处理。
 * 2、提供的模块或微服务，事先无法确认是否需要处理一些数据或事务，
 * 3、消息无法事先决定同步或异步处理。
 * </pre>
 * 
 * @author flyxxxxx@163.com
 *
 */
public class Message implements java.io.Serializable {

	private static final long serialVersionUID = -2095367815723095906L;

	/**
	 * Catch注解捕获到异常时的消息（建议异步处理以免影响正常业务处理），消息源为发生异常的类名，消息内容为ExceptionMessage类的实例
	 */
	public static final String CATCH = "catch";

	/**
	 * 被忽略的异常(HystrixCommand或Transactional），消息源为发生异常的类名，消息内容为ExceptionMessage类的实例
	 */
	public static final String IGNORE_EXCEPTION = "igorneException";

	/**
	 * 事务处理发生回滚（建议异步处理以免影响正常业务处理），消息源为发生回滚的方法名，消息内容为Throwable或其子类
	 */
	public static final String ROLLBACK = "rollback";

	/**
	 * 方法调用发生断路（建议异步处理以免影响正常业务处理），调用到fallback方法时的事件(因断路开头打开的调用不会计入)，消息源为发生异常的类名，消息内容为ExceptionMessage类的实例
	 */
	public static final String FALLBACK = "fallback";

	/**
	 * 执行出现异常（不包括被捕获的，但可能包括被忽略的）（建议异步处理以免影响正常业务处理），消息源为发生异常的方法名，消息内容为Throwable或其子类
	 */
	public static final String EXCEPTION = "exception";

	/**
	 * 业务执行事件（业务执行完成时，包括出现异常时）（建议异步处理以免影响正常业务处理），消息源为业务类名，消息内容为PrototypeStatus或其子类
	 */
	public static final String EXECUTE = "execute";
	/**
	 * 每个业务类中的异步调用（建议异步处理以免影响正常业务处理）（注解OverloadAsync/Async/Fork）的事件（每个异步线程执行完成时），消息源为异步处理的方法名，消息内容为PrototypeStatus或其子类
	 */
	public static final String ASYNC = "async";
	/**
	 * 异步调用时发生异常（建议异步处理以免影响正常业务处理），消息源为发生异常的方法名，内容为Throwable或其子类
	 */
	public static final String ASYNC_EXCEPTION = "asyncException";

	/**
	 * 请求被限流，消息源为限流实现类名，内容为业务类的实例
	 */
	public static final String LIMIT = "limit";

	// 消息主题
	private static final PublishSubject<Message> SUBJECT = PublishSubject.create();

	@Getter
	private String type;
	@Getter
	private String source;
	@Getter
	private Object content;

	/**
	 * 构造消息
	 * 
	 * @param type
	 *            类型
	 * @param source
	 *            来源（通常是发消息的类名）
	 * @param content
	 *            消息内容
	 */
	public Message(String type, String source, Object content) {
		this.type = type;
		this.source = source;
		this.content = content;
	}

	/**
	 * 获取消息主题
	 * 
	 * @return 消息主题
	 */
	public static PublishSubject<Message> getSubject() {
		return SUBJECT;
	}

	/**
	 * 异常消息
	 * 
	 * @author lj
	 *
	 */
	public static class ExceptionMessage {

		@Getter
		private PrototypeStatus status;
		@Getter
		private Throwable throwable;

		private MethodChain chain;

		public ExceptionMessage(MethodChain chain, Throwable throwable) {
			this.throwable = throwable;
			status = PrototypeStatus.getStatus();
			if (status == null) {
				status = new PrototypeStatus();
				status.setTarget(chain.getTarget());
			}
			this.chain = chain;
		}
		
		/**
		 * 发生异常的方法链.
		 * @return 方法链接，可能为null
		 */
		public MethodChain getChain(){
			return chain;
		}
	}

}
