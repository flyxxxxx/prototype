package org.prototype.business;

import lombok.Data;

/**
 * 性能数据
 * @author lj
 *
 */
@Data
public class PerformanceData {
	
	/**
	 * 消息类型
	 */
	public static final String TYPE="Performance";
	
	/**
	 * 业务类型
	 */
	private Class<?> businessType;
	
	private Object[] parameters;
	
	/**
	 * 用时(毫秒)
	 */
	private long useTime;

}
