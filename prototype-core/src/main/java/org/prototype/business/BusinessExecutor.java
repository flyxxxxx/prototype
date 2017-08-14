package org.prototype.business;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 业务执行接口. <br>
 * 通过此业的实现，注册服务、创建接口及文档、执行业务类.
 * @author lj
 *
 */
public interface BusinessExecutor {

	/**
	 * 执行业务并获取结果
	 * @param type 业务类型
	 * @param params 业务参数
	 * @return 执行结果
	 */
	Object execute(Class<?> type,Object[] params);
		
	/**
	 * 异步执行业务
	 * @param type 业务类型
	 * @param params 业务参数
	 * @return 异步调用
	 */
	CompletableFuture<Object> submit(Class<?> type,Object[] params);
	
	/**
	 * 获取业务类对应的服务
	 * @param type 业务类型
	 * @return 服务
	 */
	Service getService(Class<?> type);
	
	/**
	 * 列出所有服务
	 * @param onlyPublic 是否只列出公开的服务
	 * @return 服务列表
	 */
	Collection<Service> listServices(boolean onlyPublic);
	
	/**
	 * 注册服务
	 * @param service 服务
	 */
	void registerService(Service service);
	
	/**
	 * 查找指定版本的服务
	 * @param url 服务URL
	 * @param version 服务版本号（可为null）
	 * @return 服务
	 */
	Service findService(String url,String version);
	/**
	 * 对服务进行分组（按URL）并按版本排序
	 * @param services 服务
	 * @return 服务URL与同一服务多个版本实现的映射
	 */
	Map<String, List<Service>> groupByUrl(Collection<Service> services);
	
	/**
	 * 获取API
	 * @param type API类型（jar\java class\json\javascript。。。）
	 * @param businessName 业务类名
	 * @return InputStream\String等各种形式数据，根据接口定义
	 * @throws UnsupportedOperationException 当API类型不被支持或业务类不存在时抛出此异常
	 */
	Object getApi(String type,String businessName ) throws UnsupportedOperationException ;
}
