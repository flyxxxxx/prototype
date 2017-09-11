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
package org.prototype.core;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Getter;
import lombok.Setter;

/**
 * 原型的状态
 * 
 * @author flyxxxxx@163.com
 *
 */
public class PrototypeStatus implements Cloneable {

	private static final ThreadLocal<PrototypeStatus> current = new ThreadLocal<>();

	private static final AtomicLong SEQ = new AtomicLong();
	

	@Getter
	@Setter
	private TransactionStatus transaction;

	@Getter
	@Setter
	private Object target;

	@Getter
	@Setter
	private Object result;

	@Getter
	private long startTime;

	/**
	 * 唯一业务序号
	 */
	@Getter
	private String id;

	public PrototypeStatus() {
		startTime = System.currentTimeMillis();
		id=Long.toString(SEQ.incrementAndGet());
	}

	/**
	 * 结束执行时计时并清除线程变量
	 * 
	 * @return 当前实例本身
	 */
	public PrototypeStatus end() {
		current.set(null);
		return this;
	}
	
	/**
	 * 获取Locale
	 * @return 默认Locale
	 */
	public Locale getLocale(){
		return Locale.getDefault();
	}

	/**
	 * 设定当前线程变量值
	 * 
	 * @param status
	 *            原型状态
	 */
	public static void setStatus(PrototypeStatus status) {
		current.set(status);
	}

	/**
	 * 获取当前线程变量
	 * 
	 * @return 原型状态
	 */
	public static PrototypeStatus getStatus() {
		return current.get();
	}

	/**
	 * 获取当前线程变量
	 * 
	 * @param <T>
	 *            结果类型
	 * @param type
	 *            指定的子类型
	 * @return 原型状态
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getStatus(Class<T> type) {
		return (T) current.get();
	}

	/**
	 * 复制到异步线程中使用. <br>
	 * 此方法清理事务状态. 有其它类似数据需要子类继承.
	 * 此方法需要在当前线程中复制，然后在异步线程中{@link #setStatus(PrototypeStatus)}设置
	 * 
	 * @return 复制的数据
	 */
	public PrototypeStatus copyToAsync() {
		PrototypeStatus rs;
		try {
			rs = (PrototypeStatus) clone();
			rs.transaction = null;
			rs.startTime=System.currentTimeMillis();
			return rs;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Clone error", e);
		}
	}

	/**
	 * 事务状态
	 * 
	 * @author flyxxxxx@163.com
	 *
	 */
	@Getter@Setter
	public static class TransactionStatus {
		/**
		 * 只读
		 */
		private boolean readOnly = false;
		
		/**
		 * 数据分区
		 */
		private String partion;
	}
}
