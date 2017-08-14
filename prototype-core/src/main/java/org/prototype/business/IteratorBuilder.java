package org.prototype.business;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.prototype.reflect.ClassUtils;
import org.prototype.reflect.Property;

import javassist.Modifier;

/**
 * 类的循环构建抽象实现. <br>
 * 在构建业务类的输入参数类、输出结果类、参数注入、结果输出中，通过反射build开头的方法对不同类型的数据进行构建类或输入输出属性值。
 * @author lj
 *
 */
public abstract class IteratorBuilder {

	/**
	 * 类与反射到的方法
	 */
	private static final Map<Class<?>, Map<String, Method>> methods = new ConcurrentHashMap<>();

	/**
	 * 当前循环的属性的输入或输出注解
	 */
	protected Map<Class<?>, Annotation> inputOutputs = new HashMap<>(2);
	/**
	 * DateFormat或DecimalFormat格式化类(注意这里不能用于多线程，因这两个类线程不安全)
	 */
	private Map<String, Object> formatter = new HashMap<>(2);

	/**
	 * 获取成员变量类型的范型
	 * @param field 成员变量
	 * @param index 泛型索引位置
	 * @return 非Class的范型值返回null
	 */
	public static Class<?> getGeneric(Field field, int index) {
		Type type = field.getGenericType();
		if (type != null) {
			Type[] types = ((ParameterizedType) type).getActualTypeArguments();
			return Class.class.isInstance(types[index]) ? (Class<?>) types[index] : null;
		}
		return null;
	}

	public static List<Property> getProperties(Class<?> clazz,boolean isInput) {
		return isInput?getParameterProperties(clazz):getResultProperties(clazz);
	}

	private static List<Property> getResultProperties(Class<?> clazz) {
		List<Property> properties = new ArrayList<>();
		for (Property property : ClassUtils.properties(clazz).values()) {
			Field field = property.getField();
			if (field == null) {
				continue;
			}
			if (field.getAnnotation(Output.class) != null) {
				properties.add(property);
				continue;
			}
			InputOutput iOutput = field.getAnnotation(InputOutput.class);
			if (iOutput != null && iOutput.output().length > 0) {
				properties.add(property);
			}
		}
		return properties;
	}

	private static List<Property> getParameterProperties(Class<?> clazz) {
		List<Property> properties = new ArrayList<>();
		for (Property property : ClassUtils.properties(clazz).values()) {
			Field field = property.getField();
			if (field == null) {
				continue;
			}
			if (field.getAnnotation(Input.class) != null) {
				properties.add(property);
				continue;
			}
			InputOutput iOutput = field.getAnnotation(InputOutput.class);
			if (iOutput != null && iOutput.input().length > 0) {
				properties.add(property);
			}
		}
		return properties;
	}

	/**
	 * 初始化一个集合
	 * 
	 * @param clazz
	 *            集合类型
	 * @param array
	 *            集合中的数据
	 * @return 初始化的结果
	 * @throws Exception
	 *             异常
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Collection<?> initCollection(Class<?> clazz, Object[] array) throws Exception {
		if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
			Collection rs = (Collection) clazz.newInstance();
			rs.addAll(Arrays.asList(array));
			return rs;
		} else if (EnumSet.class.isAssignableFrom(clazz)) {
			Enum[] list = new Enum[array.length - 1];
			System.arraycopy(array, 1, list, 0, list.length);
			return EnumSet.of((Enum) array[0], list);
		} else if (Set.class.isAssignableFrom(clazz)) {
			Set set = new HashSet<>();
			set.addAll(Arrays.asList(array));
			return set;
		}
		return Arrays.asList(array);
	}

	/**
	 * 初始化一个映射
	 * 
	 * @param clazz
	 *            映射类型
	 * @param data
	 *            数据
	 * @return 初始化的结果
	 * @throws Exception
	 *             异常
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<?, ?> initMap(Class<?> clazz, Map<String, Object> data) throws Exception {
		if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
			Map rs = (Map) clazz.newInstance();
			rs.putAll(data);
			return rs;
		}
		return new HashMap<>(data);
	}

	/**
	 * 获得一个日期的格式化实例（只能用于当前线程）
	 * @param format 格式
	 * @return 日期的格式化实例
	 */
	protected final DateFormat getDateFormat(String format) {
		DateFormat rs = (DateFormat) formatter.get(format);
		if (rs == null) {
			rs = new SimpleDateFormat(format);
			formatter.put(format, rs);
		}
		return rs;
	}

	/**
	 * 获得一个数值的格式化实例（只能用于当前线程）
	 * @param format 格式
	 * @return 数值的格式化实例
	 */
	protected final DecimalFormat getDecimalFormat(String format) {
		DecimalFormat rs = (DecimalFormat) formatter.get(format);
		if (rs == null) {
			rs = new DecimalFormat(format);
			formatter.put(format, rs);
		}
		return rs;
	}

	/**
	 * 输入输出中输入的初始化
	 * 
	 * @param field
	 *            成员变量
	 * @param isInput 是否输入参数
	 */
	protected final void initInputOutput(Field field, boolean isInput) {
		inputOutputs.clear();
		InputOutput io = field.getAnnotation(InputOutput.class);
		if (io == null) {
			return;
		}
		addIntputOutput(io,isInput);
	}
	
	/**
	 * 添加处理中的属性的注解(子类可继承后对数据进行检查)
	 * @param io 输入输出注解
	 * @param isInput 是否输入
	 */
	protected void addIntputOutput(InputOutput io,boolean isInput) {
		if (isInput) {
			for (Input input : io.input()) {
				inputOutputs.put(input.type(), input);
			}
		} else {
			for (Output output : io.output()) {
				inputOutputs.put(output.type(), output);
			}
		}
	}

	/**
	 * 获取缓存的反射方法
	 * @param clazz 反射的类
	 * @return 缓存的反射方法
	 */
	private Map<String, Method> getMethods(Class<?> clazz) {
		Map<String, Method> map = methods.get(clazz);
		if (map != null) {
			return map;
		}
		map = findMethods(clazz);
		methods.put(clazz, map);
		return map;
	}

	/**
	 * 反射类的以build开头的方法
	 * @param clazz 反射的类
	 * @return build开头的方法
	 */
	private Map<String, Method> findMethods(Class<?> clazz) {
		Map<String, Method> rs = new HashMap<>();
		for (Method method : clazz.getDeclaredMethods()) {
			String name = method.getName();
			if (name.startsWith("build")) {
				method.setAccessible(true);
				rs.put(name.substring(5), method);
			}
		}
		return rs;
	}

	/**
	 * 找到指定数据类型对应的反射方法
	 * @param target 反射的目标对象
	 * @param dataType 数据类型
	 * @return 对应的反射方法
	 */
	protected final Method findMethod(Object target, Class<?> dataType) {
		String type = ClassUtils.getDataType(dataType);
		return getMethods(target.getClass()).get(type);
	}

	/**
	 * 在指定对象上反射指定数据类型对应的方法，并执行方法
	 * @param dataType 数据类型
	 * @param target 反射的目标对象
	 * @param args 方法调用参数
	 * @return 方法调用结果
	 * @throws Exception 异常
	 */
	protected final Object build(Class<?> dataType, Object target, Object... args) throws Exception {
		String type = ClassUtils.getDataType(dataType);
		Method method = getMethods(target.getClass()).get(type);
		return method.invoke(target, args);
	}

}
