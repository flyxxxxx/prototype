package org.prototype.reflect;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.prototype.reflect.CacheUtils.Cache;

/**
 * 类反射工具
 * 
 * @author lj
 *
 */
public class ClassUtils {

	private static final List<Class<?>> BASE_TYPES = Arrays.asList(boolean.class, byte.class, short.class, char.class,
			int.class, long.class, float.class, double.class);
	private static final List<Class<?>> WRAPPER_TYPES = Arrays.asList(Boolean.class, Byte.class, Short.class,
			Character.class, Integer.class, Long.class, Float.class, Double.class);

	private static final String PROPERTIES = ClassUtils.class.getName() + ".properties";
	private static final String IDS = ClassUtils.class.getName() + ".ids";
	private static final String TYPES = ClassUtils.class.getName() + ".types";
	private static final String ANNOTATION_PROPS = ClassUtils.class.getName() + ".annotationprops";

	/**
	 * 除boolean和char之外的6种基本数据类型
	 */
	public static final String PRIMITIVE = "Primitive";
	/**
	 * 字符串类型
	 */
	public static final String STRING = "String";
	/**
	 * 数值类型：Byte、Short、Integer、Long、Float、Double、BigInteger、BigDecimal及其它Number的子类
	 */
	public static final String NUMBER = "Number";
	/**
	 * 布尔数据类型：boolean/Boolean
	 */
	public static final String BOOLEAN = "Boolean";
	/**
	 * java.util.Date及其子类
	 */
	public static final String DATE = "Date";
	/**
	 * 枚举类型
	 */
	public static final String ENUM = "Enum";
	/**
	 * char/Character类型
	 */
	public static final String CHARACTER = "Character";
	/**
	 * 数组类型
	 */
	public static final String ARRAY = "Array";
	/**
	 * List(Collection的所有子类中不包括Set及其子类的部分）
	 */
	public static final String LIST = "List";
	/**
	 * Set及其子类
	 */
	public static final String SET = "Set";
	/**
	 * Map及其子类
	 */
	public static final String MAP = "Map";
	/**
	 * 除其它类型的类型均视为POJO对象
	 */
	public static final String POJO = "Pojo";

	/**
	 * 字节数组
	 */
	public static final String BYTES = "Bytes";
	
	/**
	 * 查找有指定注解的属性并缓存
	 * @param clazz 类
	 * @param annotationClass 注解类
	 * @return 有指定注解的属性
	 */
	@SuppressWarnings("unchecked")
	public static List<Property> findProperty(Class<?> clazz,Class<? extends Annotation> annotationClass){
		Cache cache=CacheUtils.getCache(ANNOTATION_PROPS);
		String key=clazz.getName()+"_"+annotationClass.getName();
		List<Property> list=cache.get(key,List.class);
		if(list!=null){
			return list;
		}
		list=new ArrayList<>();
		for(Property property:properties(clazz).values()){
			if(property.getField()!=null&&property.getField().getAnnotation(annotationClass)!=null){
				list.add(property);
			}
		}
		cache.put(key, list);
		return list;
	}
	
	/**
	 * 获得8种基本数据类型的包装类型，非基本数据类型直接返回原类
	 * @param clazz 数据类型
	 * @return 基本数据类型的包装类型或参数的类型
	 */
	public static Class<?> getWrapperType(Class<?> clazz){
		if(clazz.isPrimitive()){
			return WRAPPER_TYPES.get(BASE_TYPES.indexOf(clazz));
		}
		return clazz;
	}

	/**
	 * 获得包装类型的8种基本数据类型的，非8种包装类型直接返回原类
	 * @param clazz 数据类型
	 * @return 包装类型的8种基本数据类型的
	 */
	public static Class<?> getBaseType(Class<?> clazz){
		int k=WRAPPER_TYPES.indexOf(clazz);
		if(k!=-1){
			return BASE_TYPES.get(k);
		}
		return clazz;
	}

	/**
	 * 获取数据类型
	 * 
	 * @param type
	 *            类型
	 * @return 数据类型
	 */
	public static String getDataType(Class<?> type) {
		Cache cache = CacheUtils.getCache(TYPES);
		String rs = cache.get(type, String.class);
		if (rs != null) {
			return rs;
		}
		rs = getDataTypeInner(type);
		cache.put(type, rs);
		return rs;
	}

	private static String getDataTypeInner(Class<?> type) {
		if (String.class.equals(type)) {
			return STRING;
		} else if (boolean.class == type || Boolean.class.isAssignableFrom(type)) {
			return BOOLEAN;
		} else if (char.class == type || Character.class.equals(type)) {
			return CHARACTER;
		} else if (type.isPrimitive()) {
			return PRIMITIVE;
		} else if (Number.class.isAssignableFrom(type)) {
			return NUMBER;
		} else if (Date.class.isAssignableFrom(type)) {
			return DATE;
		} else if (type.isEnum()) {
			return ENUM;
		} else if (byte[].class.equals(type)) {
			return BYTES;
		} else if (type.isArray()) {
			return ARRAY;
		} else if (Set.class.isAssignableFrom(type)) {
			return SET;
		} else if (Collection.class.isAssignableFrom(type)) {
			return LIST;
		} else if (Map.class.isAssignableFrom(type)) {
			return MAP;
		}
		return POJO;
	}

	private ClassUtils() {
		// do nothing
	}

	/**
	 * 获取类的属性. <br>
	 * 如果父子类有重复的属性，则忽略父类的属性
	 * 
	 * @param type
	 *            指定的类型
	 * @return 属性名与属性的映射
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Property> properties(Class<?> type) {
		Cache cache = CacheUtils.getCache(PROPERTIES);
		SoftReference<?> reference = cache.get(type, SoftReference.class);
		if (reference != null) {
			Object map = reference.get();
			if (map != null) {
				return (Map<String, Property>) map;
			}
		}
		Map<String, Property> map = findProperties(type);
		reference = new SoftReference<Object>(map);
		cache.put(type, reference);
		return map;
	}

	/**
	 * 查找ID成员变量
	 * 
	 * @param clazz
	 *            指定的类
	 * @return ID成员变量
	 */
	public static Property findIdProperty(Class<?> clazz) {
		Cache cache = CacheUtils.getCache(IDS);
		SoftReference<?> id = cache.get(clazz, SoftReference.class);
		if (id != null) {
			Property property = (Property) id.get();
			if (property != null) {
				return property;
			}
		}
		for (Property property : ClassUtils.properties(clazz).values()) {
			for (Annotation annotation : property.getField().getAnnotations()) {
				if (annotation.annotationType().getName().endsWith(".Id")) {
					id = new SoftReference<Property>(property);
					cache.put(clazz, id);
					return property;
				}
			}
		}
		return null;
	}

	/**
	 * 列出所有非静态成员变量（包括父类的，私有的），如果父子类有重复的属性，则忽略父类的属性
	 * 
	 * @param type
	 *            类的类型
	 * @return 所有成员变量
	 */
	public static List<Field> listFields(Class<?> type) {
		List<Field> list = new ArrayList<>();
		for (Property property : properties(type).values()) {
			Field field = property.getField();
			if (field != null) {
				list.add(field);
			}
		}
		return list;
	}

	/**
	 * 列出有指定注解的所有非静态成员变量
	 * 
	 * @param type
	 *            类的类型
	 * @param annotationClass
	 *            注解类
	 * @return 有指定注解的所有成员变量
	 */
	public static List<Field> listFields(Class<?> type, Class<? extends Annotation> annotationClass) {
		List<Field> list = new ArrayList<>();
		for (Property property : properties(type).values()) {
			Field field = property.getField();
			if (field != null && field.getAnnotation(annotationClass) != null) {
				list.add(field);
			}
		}
		return list;
	}

	/**
	 * 反射获取所有属性
	 * 
	 * @param type
	 *            指定的类型
	 * @return 属性名与属性的映射
	 */
	private static Map<String, Property> findProperties(Class<?> type) {
		try {
			Map<String, Property> map = new HashMap<>();
			PropertyDescriptor[] descriptors = Introspector.getBeanInfo(type).getPropertyDescriptors();
			for (PropertyDescriptor descriptor : descriptors) {
				map.put(descriptor.getName(), new Property(descriptor));
			}
			for (Map.Entry<String, Field> entry : mapFields(type).entrySet()) {
				Property property = map.get(entry.getKey());
				if (property == null) {
					map.put(entry.getKey(), new Property(entry.getValue()));
				} else {
					property.setField(entry.getValue());
				}
			}
			map.remove("class");
			return Collections.unmodifiableMap(map);
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}
	}

	private static Map<String, Field> mapFields(Class<?> type) {
		Map<String, Field> rs = new HashMap<>();
		Class<?> clazz = type;
		while (clazz != null) {
			for (Field field : clazz.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				if (!rs.containsKey(field.getName())) {
					field.setAccessible(true);
					rs.put(field.getName(), field);
				}
			}
			clazz = clazz.getSuperclass();
		}
		return Collections.unmodifiableMap(rs);
	}

}
