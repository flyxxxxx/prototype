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
package org.prototype.reflect;

import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prototype.reflect.CacheUtils.Cache;
import org.springframework.aop.Advisor;

/**
 * 方法工具.
 * 
 * @author flyxxxxx@163.com
 *
 */
public class MethodUtils {

	private static final String METHODS = MethodUtils.class.getName() + ".methods";

	private static final String FINDED = MethodUtils.class.getName() + ".finded";

	private static final String EMPTY_METHOD = "";

	@SuppressWarnings("unchecked")
	private static SoftReference<Object> findMethods(Class<?> clazz) {
		Cache cache = CacheUtils.getCache(METHODS);
		SoftReference<Object> reference = cache.get(clazz, SoftReference.class);
		if (reference == null) {
			reference = findAllMethods(clazz);
			cache.put(clazz, reference);
		}
		return reference;
	}

	private static SoftReference<Object> findAllMethods(Class<?> clazz) {
		Map<String, List<Method>> methods = new HashMap<>();
		addMethods(methods, clazz, 0);
		Class<?> parent = clazz.getSuperclass();
		while (parent != null && !Object.class.equals(parent)) {
			addMethods(methods, parent, parent.getPackage().getName().equals(clazz.getName()) ? 1 : 2);
			parent = parent.getSuperclass();
		}
		return new SoftReference<Object>(methods);
	}

	private static void addMethods(Map<String, List<Method>> map, Class<?> clazz, int range) {
		for (Method method : clazz.getDeclaredMethods()) {
			String name = method.getName();
			if (isInRange(method, range)) {
				List<Method> list = map.get(name);
				if (list == null) {
					list = new ArrayList<>();
				} else if (exists(list, method)) {
					continue;
				}
				method.setAccessible(true);
				list.add(method);
				map.put(name, list);
			}
		}
	}

	private static boolean exists(List<Method> list, Method method) {
		for (Method m : list) {
			Class<?>[] paramTypes1 = m.getParameterTypes();
			Class<?>[] paramTypes2 = method.getParameterTypes();
			if (isSame(paramTypes1, paramTypes2)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isSame(Class<?>[] paramTypes1, Class<?>[] paramTypes2) {
		int k = paramTypes1.length;
		if (k != paramTypes2.length) {
			return false;
		}
		for (int i = 0; i < k; i++) {
			if (!paramTypes1[i].equals(paramTypes2)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isInRange(Method method, int range) {
		switch (range) {
		case 0:
			return true;
		case 1:
			return !Modifier.isPrivate(method.getModifiers());
		default:
			int mod = method.getModifiers();
			return Modifier.isPublic(mod) || Modifier.isProtected(mod);
		}
	}

	/**
	 * 查找同名的多个方法(优先使用缓存)
	 * 
	 * @param clazz
	 *            指定类
	 * @param methodName
	 *            方法名
	 * @return 方法列表
	 */
	@SuppressWarnings("unchecked")
	public static List<Method> findMethods(Class<?> clazz, String methodName) {
		Map<String, List<Method>> methods = (Map<String, List<Method>>) (findMethods(clazz).get());
		List<Method> list = methods.get(methodName);
		return list == null ? new ArrayList<Method>() : list;
	}

	/**
	 * 查询匹配的重载方法(优先从缓存查询)
	 * 
	 * @param clazz
	 *            指定的类
	 * @param methodName
	 *            方法名
	 * @param parameterType
	 *            方法唯一参数
	 * @return 匹配的方法
	 */
	@SuppressWarnings("unchecked")
	public static Method findOverloadMethod(Class<?> clazz, String methodName, Class<?> parameterType) {
		String key = getMethodKey(clazz, methodName, parameterType);
		Cache cache=CacheUtils.getCache(FINDED);
		SoftReference<Object> reference = cache.get(key,SoftReference.class);
		if (reference != null) {
			Object object = reference.get();
			if (object != null) {
				return object == EMPTY_METHOD ? null : (Method) object;
			}
		}
		Method method = findOverloadMethod1(clazz, methodName, parameterType);
		cache.put(key, new SoftReference<Object>(method == null ? EMPTY_METHOD : method));
		return method;
	}

	/**
	 * 查询唯一方法(优先从缓存查询)
	 * 
	 * @param clazz
	 *            指定的类
	 * @param methodName
	 *            方法名
	 * @param required
	 *            是否必须
	 * @return 唯一方法或null
	 */
	public static Method findUniqueMethod(Class<?> clazz, String methodName, boolean required) {
		List<Method> mas = findMethods(clazz, methodName);
		if (mas.size() == 1) {
			return mas.get(0);
		}
		Map<String, List<Method>> map = new HashMap<>();
		for (Method ma : mas) {
			String className = ma.getDeclaringClass().getName();
			List<Method> list = map.get(className);
			if (list == null) {
				list = new ArrayList<>();
				map.put(className, list);
			}
			list.add(ma);
		}
		Class<?> ca = clazz;
		while (ca != null) {
			List<Method> list = map.get(ca.getName());
			if (list == null) {
				ca = ca.getSuperclass();
				continue;
			}
			int k = list.size();
			if (k == 1) {
				return list.get(0);
			} else if (k > 1) {
				throw new NullPointerException(
						"Method '" + methodName + " is more than one in class '" + clazz.getName() + "'");
			} else {
				ca = ca.getSuperclass();
			}
		}
		if (required) {
			throw new NullPointerException("Method '" + methodName + " not found in class '" + clazz.getName() + "'");
		}
		return null;
	}

	/**
	 * 查询匹配的重载方法
	 * 
	 * @param clazz
	 *            指定的类
	 * @param methodName
	 *            方法名
	 * @param parameterType
	 *            方法唯一参数
	 * @return 匹配的重载方法
	 */
	private static Method findOverloadMethod1(Class<?> clazz, String methodName, Class<?> parameterType) {
		Class<?> type = clazz;
		List<Method> methods = MethodUtils.findMethods(type, methodName);
		Map<Class<?>, Method> map = new HashMap<>();
		for (Method method : methods) {
			Class<?>[] types = method.getParameterTypes();
			if (types.length == 1) {// 找到所有匹配的方法
				if (types[0].isPrimitive()) {
					types[0] = getWrapperClass(types[0]);
				}
				if (!types[0].isInterface() && types[0].isAssignableFrom(parameterType)) {
					map.put(types[0], method);
				}
			}
		}
		if (map.isEmpty()) {
			return null;
		}
		Class<?> cls = parameterType;
		while (!Object.class.equals(cls)) {// 从子类向父类依次查找
			Method rs = map.get(cls);
			if (rs != null) {
				return rs;
			}
			cls = cls.getSuperclass();
		}
		return null;
	}

	/**
	 * 获得方法在缓存中的关键字
	 * 
	 * @param clazz
	 *            指定的类
	 * @param methodName
	 *            方法名
	 * @param parameterTypes
	 *            方法参数
	 * @return 方法在缓存中的关键字
	 */
	private static String getMethodKey(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		StringBuilder rs = new StringBuilder(clazz.getName());
		rs.append('_');
		rs.append(methodName);
		rs.append('_');
		for (Class<?> type : parameterTypes) {
			rs.append(type.getName());
			rs.append(',');
		}
		if (parameterTypes.length > 0) {
			rs.deleteCharAt(rs.length() - 1);
		}
		return rs.toString();
	}

	/**
	 * 获取包装类型（仅8种基本数据类型）.<br>
	 * 非基本类型直接返回原类型
	 * 
	 * @param type
	 *            类型
	 * @return 包装类型
	 */
	public static Class<?> getWrapperClass(Class<?> type) {
		if (!type.isPrimitive()) {
			return type;
		}
		String name = type.getName();
		switch (name) {
		case "int":
			return Integer.class;
		case "char":
			return Character.class;
		default:
			try {
				return Advisor.class.getClassLoader()
						.loadClass("java.lang." + Character.toUpperCase(name.charAt(0)) + name.substring(1));
			} catch (ClassNotFoundException e) {
				throw new UnsupportedOperationException("Type '" + name + "' is unsupported", e);
			}
		}
	}

	/**
	 * 查找指定名称和类型的方法. <br>
	 * 包括子类可见的父类的方法
	 * 
	 * @param clazz
	 *            指定的类
	 * @param methodName
	 *            方法名
	 * @param parameterTypes
	 *            方法参数
	 * @return 唯一的方法，未找到时返回null
	 */
	@SuppressWarnings("unchecked")
	public static Method findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		String key = getMethodKey(clazz, methodName, parameterTypes);
		Cache cache=CacheUtils.getCache(FINDED);
		SoftReference<Object> reference = cache.get(key,SoftReference.class);
		if (reference != null) {
			Object value = reference.get();
			if (value != null) {
				return value == EMPTY_METHOD ? null : (Method) value;
			}
		}
		Method method = findMethod1(clazz, methodName, parameterTypes);
		cache.put(key, new SoftReference<Object>(method == null ? EMPTY_METHOD : method));
		return method;
	}

	@SuppressWarnings("unchecked")
	private static Method findMethod1(Class<?> clazz, String methodName, Class<?>[] parameterType) {
		Map<String, List<Method>> methods = (Map<String, List<Method>>) (findMethods(clazz).get());
		List<Method> list = methods.get(methodName);
		if (list == null) {
			return null;
		}
		for (Method method : list) {
			if (matches(method, parameterType)) {
				return method;
			}
		}
		return null;
	}

	private static boolean matches(Method method, Class<?>[] parameterType) {
		Class<?>[] ps = method.getParameterTypes();
		if (ps.length != parameterType.length) {
			return false;
		}
		for (int i = 0, k = ps.length; i < k; i++) {
			if (!ps[i].equals(parameterType[i])) {
				return false;
			}
		}
		return true;
	}

}
