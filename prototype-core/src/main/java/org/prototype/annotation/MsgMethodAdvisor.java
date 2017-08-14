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

import java.util.Collection;

import org.prototype.core.Errors;
import org.prototype.core.MethodAdvisor;
import org.prototype.core.MethodBuilder;
import org.prototype.core.MethodChain;
import org.prototype.core.MethodFilter;
import org.prototype.core.PrototypeStatus;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import rx.subjects.PublishSubject;

/**
 * 消息注解处理.
 * 
 * @author lj
 *
 */
@Slf4j
@Component
public class MsgMethodAdvisor implements MethodAdvisor {

	@Override
	public MethodFilter<?> matches(MethodBuilder accessor, Errors errors) {
		Msg msg = accessor.getAnnotation(Msg.class);
		if (msg == null) {
			return null;
		}
		if ("void".equals(accessor.getReturnType())) {
			errors.add("msg.method.return", accessor.toString());
			return null;
		}
		return new MsgMethodFilter(msg);
	}

	/**
	 * 消息发送实现
	 * 
	 * @author lj
	 *
	 */
	private class MsgMethodFilter implements MethodFilter<Msg> {

		private Msg msg;

		/**
		 * 构造
		 * 
		 * @param msg
		 *            注解
		 */
		public MsgMethodFilter(Msg msg) {
			this.msg = msg;
		}

		@Override
		public Object doFilter(Object[] args, MethodChain chain) throws Exception {
			Object rs = chain.doFilter(args);
			if (rs != null) {
				PublishSubject<Message> subject = Message.getSubject();
				String className = chain.getTarget().getClass().getName();
				PrototypeStatus status=PrototypeStatus.getStatus();
				if (rs instanceof Collection) {// 集合数据以单一形式发出
					Collection<?> collection = (Collection<?>) rs;
					log.debug("Business {} , Pulish msg in {}, number is : {}",status.getId(), chain.getMethod(), collection.size());
					for (Object obj : collection) {
						subject.onNext(new Message(msg.type(), className, obj));
					}
				} else {// 单一数据
					log.debug("Business {} , Pulish msg in {}, number is : 1",status.getId(), chain.getMethod());
					subject.onNext(new Message(msg.type(), className, rs));
				}
			}
			return rs;
		}

	}

}
