package org.prototype.business;

import java.util.Collection;

/**
 * API创建接口. <br>
 * 根据业务类来创建java接口、json形式接口文档、javascript验证代码等.
 * @author flyxxxxx@163.com
 *
 */
public interface ApiCreator<T> {
	
	/**
	 * 服务提供类型（名称全局唯一）
	 * @return 服务提供类型
	 */
	String getType();
	
	/**
	 * 是否支持单一服务创建API（也就是{@link #create(Service)}方法是否可用.
	 * @return 是否支持单一服务创建API
	 */
	boolean isSupportSingle();

	/**
	 * 创建服务。此接口可能在运行期间多次调用，因此注意不能生成重复的类。
	 * @return T 结果类型
	 * @param service 服务
	 */
	T create(Service service);
	
	/**
	 * 框架启动中为所有服务创建接口. <br>
	 * 此方法将在框架启动中完成调用，以创建如Spring Controller类.
	 * @param services 服务
	 */
	void createForAll(Collection<Service> services);

}
