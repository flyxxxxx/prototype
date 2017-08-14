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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.prototype.core.MethodChain;
import org.prototype.core.PrototypeStatus;
import org.prototype.inject.InjectHelper;

import lombok.extern.slf4j.Slf4j;

/**
 * 方法异步调用Runnable实现. <br>
 * 支持引种池使用PriorityBlockingQueue，以分别给予不同类型的异步线程不同的优先级。
 * 
 * @author lj
 *
 */
@Slf4j
public class MethodInvokerRunnable implements Runnable, Comparable<MethodInvokerRunnable> {

	private static final AtomicLong SEQ = new AtomicLong();

	private Method method;// 方法
	private PrototypeStatus status;// 状态
	private InjectHelper helper;// 注入helper
	private Object target;// 目标对象
	private CountDownLatch latch;// 计数
	private MethodChain chain;// 方法链
	private int priority;
	private long sequence;

	/**
	 * 构造
	 * 
	 * @param helper
	 *            注入helper
	 * @param chain
	 *            方法链
	 * @param priority
	 *            优先级
	 */
	public MethodInvokerRunnable(InjectHelper helper, MethodChain chain, int priority) {
		this.helper = helper;
		this.chain = chain;
		this.target = chain.getTarget();
		this.priority = priority;
		sequence = SEQ.incrementAndGet();
		initStatus();
	}

	/**
	 * 构造
	 * 
	 * @param helper
	 *            注入helper
	 * @param method
	 *            方法
	 * @param target
	 *            目标对象
	 * @param latch
	 *            计数
	 * @param priority
	 *            优先级
	 */
	public MethodInvokerRunnable(InjectHelper helper, Method method, Object target, CountDownLatch latch,
			int priority) {
		this.helper = helper;
		this.method = method;
		this.target = target;
		this.latch = latch;
		this.priority = priority;
		sequence = SEQ.incrementAndGet();
		initStatus();
	}

	/**
	 * 初始化原型状态
	 */
	private void initStatus() {
		this.status = PrototypeStatus.getStatus();
		if (status != null) {
			status = status.copyToAsync();
		}
	}

	@Override
	public void run() {
		PrototypeStatus.setStatus(status);
		Method m=method == null ? chain.getMethod() : method;
		log.debug("Business {} , Async method : {}", status.getId(), m);
		long nano = log.isDebugEnabled() ? 0 : System.nanoTime();
		if (status != null) {
			PrototypeStatus.setStatus(status);
			Message.getSubject().onNext(new Message(Message.ASYNC,m.toString(), status));
		}
		Object[] args = null;
		try {// 调用
			if (chain != null) {
				chain.doFilter(args);
			} else {
				args = helper.getInjectParameters(m);
				method.invoke(target, args);
			}
		} catch (InvocationTargetException e) {
			onThrowable(e.getTargetException());
		} catch (Exception e) {
			onThrowable(e);
		} finally {// 结束线程处理
			if (latch != null) {
				latch.countDown();
			}
			helper.release(m, args);
			if (status != null) {
				PrototypeStatus.setStatus(null);
			}
			if (nano > 0) {
				log.debug("Business {} , Async end : {} , use time {} nanoseconds", status.getId(),
						m, System.nanoTime() - nano);
			}
		}
	}

	/**
	 * 异常处理
	 * 
	 * @param throwable
	 *            Throwable
	 */
	private void onThrowable(Throwable throwable) {
		if (status != null) {
			Message.getSubject().onNext(new Message(Message.ASYNC_EXCEPTION, method.toString(), throwable));
		}
		if (throwable instanceof RuntimeException) {
			throw (RuntimeException) throwable;
		} else {
			throw new RuntimeException(throwable);
		}
	}

	@Override
	public int compareTo(MethodInvokerRunnable o) {
		int k = priority - o.priority;
		if (k == 0) {
			k = (int) (sequence - o.sequence);
		}
		return k;
	}

	@Override
	public boolean equals(Object object) {
		if (getClass().isInstance(object)) {
			MethodInvokerRunnable runnable = (MethodInvokerRunnable) object;
			return sequence == runnable.sequence && hashCode() == object.hashCode();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (int) sequence + target.hashCode();
	}
}
