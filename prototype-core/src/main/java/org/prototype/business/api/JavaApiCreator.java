package org.prototype.business.api;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.prototype.business.ApiCreator;
import org.prototype.business.BusinessExecutor;
import org.prototype.business.Service;
import org.prototype.business.View;
import org.prototype.core.ClassBuilder;
import org.prototype.core.ClassFactory;
import org.prototype.core.ClassScaner;
import org.prototype.core.ComponentContainer;
import org.prototype.core.FieldBuilder;
import org.prototype.core.InterfaceBuilder;
import org.prototype.reflect.ClassUtils;
import org.prototype.reflect.MethodUtils;
import org.springframework.stereotype.Component;

import javassist.Modifier;
import lombok.extern.slf4j.Slf4j;

/**
 * 提供java形式的标准API.
 * <pre>
 * 用于为dubbo或HttpInvoker、Hession形式的协议提供java接口.
 * 要求业务类有唯一的无参数构造方法，并且没有{@link org.prototype.business.View}注解的成员变量。
 * 此接口实现不会一次为一个业务类生成接口，只会根据配置一次性生成全部接口和实现。
 * </pre>
 * @author lj
 *
 */
@Slf4j
@Component
public class JavaApiCreator implements ApiCreator<Class<?>[]> {

	/**
	 * API类型: java
	 */
	public static final String TYPE = "java";

	@Resource
	private ComponentContainer container;//组件容器

	@Resource
	private JavaApiNameGenerator generator;//类名称及方法名生成器

	@Resource
	private ClassScaner scaner;//类扫描

	/**
	 * 已经生成的接口名与实现类的映射
	 */
	private Map<String, Class<?>> classes = new ConcurrentHashMap<>();

	@Override
	public boolean isSupportSingle() {
		return false;
	}
	@Override
	public String getType() {
		return TYPE;
	}


	/**
	 * 使用指定API命名生成器创建服务接口
	 * @param generator API命名生成器
	 * @param services 服务
	 * @return 服务接口
	 */
	private Class<?>[] create(JavaApiNameGenerator generator,Collection<Service> services) {
		List<Class<?>> list = new ArrayList<>();
		Map<String, List<Service>> map =Service. group(services);
		for (Map.Entry<String, List<Service>> entry : map.entrySet()) {
			Class<?> clazz = create(generator,entry.getKey(), entry.getValue());
			if (clazz != null) {
				list.add(clazz);
			}
		}
		return list.toArray(new Class<?>[list.size()]);
	}

	@Override
	public Class<?>[] create(Service services) {
		return new Class<?>[0];
	}

	/**
	 * 创建业务实现类（并实现和缓存一个接口）. <br>
	 * @param packageName
	 *            包名
	 * @param services
	 *            业务服务
	 * @return 业务实现类
	 */
	private Class<?> create(JavaApiNameGenerator generator,String packageName, Collection<Service> services) {
		Map<Service, String> names = new HashMap<>();
		List<Service> list = new ArrayList<>();
		for (Service service : services) {
			if (service.getConstructor().getParameterCount() > 0) {
				log.debug("Class {} cannot be service method , it's constructor has parameters",service.getType().getName());
			} else if (ClassUtils.findProperty(service.getType(), View.class).size() > 0) {
				log.debug("Class {} cannot be service method , it's web view",service.getType().getName());
			} else {
				String name = generator.newServiceMethodName(service);
				names.put(service, name);
				list.add(service);
			}
		}
		if (list.isEmpty()) {
			return null;
		}
		String interfaceName = generator.newInterfaceName(packageName);
		Class<?> rs = classes.get(interfaceName);
		if (rs != null) {
			return rs;
		}
		Class<?> interfaceType = createInterface(scaner.getClassFactory(), interfaceName, list, names);
		rs = createImplement(generator, packageName, list, names, interfaceType);
		classes.put(interfaceName, rs);
		return rs;
	}

	/**
	 * 创建接口实现
	 * 
	 * @param factory
	 * @param packageName
	 * @param services
	 * @param names
	 * @param interfaceType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Class<?> createImplement(JavaApiNameGenerator generator, String packageName, Collection<Service> services,
			Map<Service, String> names, Class<?> interfaceType) {
		Class<? extends Throwable>[] throwableTypes = (Class<? extends Throwable>[]) Array.newInstance(Class.class, 0);
		ClassBuilder builder = scaner.getClassFactory().newClass(generator.newServiceName(packageName),Object.class,
				new Class<?>[] { interfaceType });
		builder.getAnnotation(org.springframework.stereotype.Service.class);
		FieldBuilder fileBuilder = builder.newField(Modifier.PRIVATE, "executor", BusinessExecutor.class, false);
		fileBuilder.getAnnotationBuilder(Resource.class);
		fileBuilder.create();
		for (Service service : services) {
			Class<?> returnType = service.getResultType() == null ? void.class : service.getResultType();
			String name = names.get(service);
			Class<?>[] parameterTypes = service.getParamType() == null ? new Class<?>[0]
					: new Class<?>[] { service.getParamType() };
			builder.newMethod(Modifier.PUBLIC, returnType, name, parameterTypes, throwableTypes,
					buildServiceMethodCode(service.getType(), returnType, parameterTypes)).create();
			if (service.getParamType() != null) {// 创建一个参数的各成员变量拆开的方法
				parameterTypes = getAllParameters(service.getParamType());
				if (parameterTypes.length <= 5) {// 参数过多不创建拆开的方法
					builder.newMethod(Modifier.PUBLIC, returnType, name, parameterTypes, throwableTypes,
							buildServiceMethodCode(service.getType(), returnType, parameterTypes)).create();
				}
			}
		}
		return builder.create();
	}

	private Class<?>[] getAllParameters(Class<?> paramType) {
		Set<Field> set = new TreeSet<>(new Comparator<Field>() {
			@Override
			public int compare(Field o1, Field o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for (Field field : paramType.getDeclaredFields()) {
			if(Modifier.isStatic(field.getModifiers())){
				continue;
			}
			set.add(field);
		}
		List<Class<?>> list = new ArrayList<>();
		for (Field field : set) {
			list.add(field.getType());
		}
		return list.toArray(new Class<?>[list.size()]);
	}

	/**
	 * 创建接口
	 * 
	 * @param factory
	 * @param packageName
	 * @param services
	 * @param names
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Class<?> createInterface(ClassFactory factory, String interfaceName, Collection<Service> services,
			Map<Service, String> names) {
		InterfaceBuilder interfaceBuilder = factory.newInterface(interfaceName);
		Class<? extends Throwable>[] throwableTypes = (Class<? extends Throwable>[]) Array.newInstance(Class.class, 0);
		for (Service service : services) {
			Class<?> returnType = service.getResultType() == null ? void.class : service.getResultType();
			String name = names.get(service);
			names.put(service, name);
			Class<?>[] parameterTypes = service.getParamType() == null ? new Class<?>[0]
					: new Class<?>[] { service.getParamType() };
			interfaceBuilder.newMethod(returnType, name, parameterTypes, throwableTypes).create();
			if (service.getParamType() != null) {
				parameterTypes = getAllParameters(service.getParamType());
				if (parameterTypes.length <= 5) {// 参数过多不创建拆开的方法
					interfaceBuilder.newMethod(returnType, name, parameterTypes, throwableTypes).create();
				}
			}
		}
		return interfaceBuilder.create();
	}

	/**
	 * 构建服务方法
	 * 
	 * @param serviceType
	 * @param returnType
	 * @param parameterTypes
	 * @return
	 */
	private String buildServiceMethodCode(Class<?> serviceType, Class<?> returnType, Class<?>[] parameterTypes) {
		if (void.class.equals(returnType)) {
			return "{}";
		}
		StringBuilder builder = new StringBuilder("{ return (");
		builder.append(MethodUtils.getWrapperClass(returnType).getName());
		builder.append(")executor.execute(");
		builder.append(serviceType.getName());
		builder.append(".class,new java.lang.Object");
		if (parameterTypes.length == 0) {
			builder.append("[0]);");
		} else {
			builder.append("[]{");
			for (int i = 0, m = parameterTypes.length; i < m; i++) {
				if (parameterTypes[i].isPrimitive()) {
					builder.append("new " + ClassUtils.getWrapperType(parameterTypes[i]).getName() + "(");
					builder.append("$");
					builder.append(i + 1);
					builder.append(')');
				} else {
					builder.append("$");
					builder.append(i + 1);
				}
				builder.append(',');
			}
			builder.deleteCharAt(builder.length() - 1);
			builder.append("});");
		}
		return builder.toString();
	}

	/**
	 * java api（接口形式）类名及方法名生成器
	 * 
	 * @author lj
	 *
	 */
	@Component
	public static class JavaApiNameGenerator {

		/**
		 * 新的服务名（实现类名）
		 * 
		 * @param packageName
		 *            包名
		 * @return 实现类名
		 */
		public String newServiceName(String packageName) {
			int k = packageName.lastIndexOf('.');
			if (k == -1) {
				return packageName + ".StandardServiceImpl";
			}
			return packageName + "." + packageName.substring(k + 1, k + 2).toUpperCase() + packageName.substring(k + 2)
					+ "ServiceImpl";
		}

		/**
		 * 新的服务接口名
		 * 
		 * @param packageName
		 *            包名
		 * @return 服务接口名
		 */
		public String newInterfaceName(String packageName) {
			int k = packageName.lastIndexOf('.');
			if (k == -1) {
				return packageName + ".StandardService";
			}
			return packageName + "." + packageName.substring(k + 1, k + 2).toUpperCase() + packageName.substring(k + 2)
					+ "Service";
		}

		/**
		 * 以业务类名（首字母小写）为方法名
		 * 
		 * @param service
		 *            服务
		 * @return 方法名
		 */
		public String newServiceMethodName(Service service) {
			String name = service.getType().getSimpleName();
			return name.substring(0, 1).toLowerCase() + name.substring(1);
		}
	}

	@Override
	public void createForAll(Collection<Service> services) {
		create(generator, services);
	}

}
