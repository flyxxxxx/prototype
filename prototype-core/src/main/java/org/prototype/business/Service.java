package org.prototype.business;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * 服务. <br>
 * 有定义ServiceDefine注解的类均视为一个服务.
 * @author flyxxxxx@163.com
 *
 */
public class Service{
	
	@Getter@Setter(lombok.AccessLevel.PROTECTED)
	private ServiceDefine define;

	/**
	 * 业务类型
	 */
	@Getter@Setter(lombok.AccessLevel.PROTECTED)
	private Class<?> type;
	
	/**
	 * 业务基类：定义BusinessDefine注解的类
	 */
	@Getter@Setter(lombok.AccessLevel.PROTECTED)
	private Class<?> baseType;

	/**
	 * 参数类型
	 */
	@Getter@Setter(lombok.AccessLevel.PROTECTED)
	private Class<?> paramType;
	/**
	 * 结果类型
	 */
	@Getter@Setter(lombok.AccessLevel.PROTECTED)
	private Class<?> resultType;

	/**
	 * 获取业务类的构建方法
	 */
	@Getter@Setter(lombok.AccessLevel.PACKAGE)
	private Constructor<?> constructor;

	@Getter(lombok.AccessLevel.PACKAGE)@Setter(lombok.AccessLevel.PACKAGE)
	private Method setResult;
	@Getter(lombok.AccessLevel.PACKAGE)@Setter(lombok.AccessLevel.PACKAGE)
	private Method addValidateError;
	

	/**
	 * 对服务按包名分组
	 * 
	 * @param services
	 *            服务
	 * @return 按包名分组的服务列表
	 */
	public static Map<String, List<Service>> group(Collection<Service> services) {
		Map<String, List<Service>> map = new HashMap<>();
		for (Service service : services) {
			String group = getGroupName(service);
			List<Service> list = map.get(group);
			if (list == null) {
				list = new ArrayList<>();
				map.put(group, list);
			}
			list.add(service);
		}
		return map;
	}

	/**
	 * 获取服务的组名
	 * 
	 * @param service
	 *            服务名
	 * @return
	 */
	private static String getGroupName(Service service) {
		String name = service.getType().getName();
		int k = name.lastIndexOf('.');
		return name.substring(0, k);
	}
}
