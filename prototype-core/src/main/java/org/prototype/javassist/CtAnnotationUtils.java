package org.prototype.javassist;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.AnnotationMemberValue;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ByteMemberValue;
import javassist.bytecode.annotation.CharMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.DoubleMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.FloatMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.LongMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.ShortMemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import lombok.extern.slf4j.Slf4j;

/**
 * javassist注解工具. <br>
 * 
 * @author flyxxxxx@163.com
 *
 */
@Slf4j
class CtAnnotationUtils {

	/**
	 * javassist反射时生成注解值
	 */
	private static final Map<Class<?>, Class<?>> TYPES = new HashMap<>();

	static {// int.class特殊，不要加进来
		TYPES.put(String.class, StringMemberValue.class);
		TYPES.put(short.class, ShortMemberValue.class);
		TYPES.put(Short.class, ShortMemberValue.class);
		TYPES.put(char.class, CharMemberValue.class);
		TYPES.put(Character.class, CharMemberValue.class);
		TYPES.put(byte.class, ByteMemberValue.class);
		TYPES.put(Byte.class, ByteMemberValue.class);
		TYPES.put(long.class, LongMemberValue.class);
		TYPES.put(Long.class, LongMemberValue.class);
		TYPES.put(float.class, FloatMemberValue.class);
		TYPES.put(Float.class, FloatMemberValue.class);
		TYPES.put(double.class, DoubleMemberValue.class);
		TYPES.put(Double.class, DoubleMemberValue.class);
	}

	/**
	 * 判断是否有指定注解
	 * 
	 * @param cls
	 * @param annotationType
	 * @return
	 * @throws ClassNotFoundException
	 */
	static boolean hasAnnotation(CtClass cls, Class<? extends Annotation> annotationType) {
		try {
			if (cls.getAnnotation(annotationType) != null) {
				return true;
			}
			for (Object obj : cls.getAnnotations()) {
				if (((Annotation) obj).annotationType().getAnnotation(annotationType) != null) {
					return true;
				}
			}
			CtClass parent = cls.getSuperclass();
			if (parent != null && !Object.class.getName().equals(parent.getName())) {
				return hasAnnotation(parent, annotationType);
			}
		} catch (ClassNotFoundException | NotFoundException e) {
			log.warn("Class not found", e);
		}
		return false;
	}

	/**
	 * 通过java注解创建javassist注解
	 * 
	 * @param cp
	 *            常量池
	 * @param ann
	 *            注解
	 * @return 注解
	 */
	static javassist.bytecode.annotation.Annotation createAnnotation(ConstPool cp, Annotation ann) {
		javassist.bytecode.annotation.Annotation rs = new javassist.bytecode.annotation.Annotation(
				ann.annotationType().getName(), cp);
		try {
			for (Method m : ann.annotationType().getDeclaredMethods()) {// 循环注解的所有值
				Object value = m.invoke(ann);
				if (value != null && !isSameValue(value, m.getDefaultValue())) {// 排除空值和默认值
					rs.addMemberValue(m.getName(), getMemberValue(cp, value));
				}
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return rs;
	}

	/**
	 * 获取注解的成员值
	 * 
	 * @param cp
	 *            常量池
	 * @param value
	 *            值
	 * @return 注解的成员值
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	static MemberValue getMemberValue(ConstPool cp, Object value) {
		Class<?> type = value.getClass();
		if (type.isArray()) {
			return getMemberArray(cp, value);
		}
		Class<?> t = TYPES.get(type);
		if (t != null) {// 通过构造方法(type,ConstPool)构造
			try {
				return (MemberValue) t.getConstructor(type, ConstPool.class).newInstance(value, cp);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else if (boolean.class.equals(type) || Boolean.class.equals(type)) {
			return new BooleanMemberValue((Boolean) value, cp);
		} else if (int.class.equals(type) || Integer.class.equals(type)) {
			return new IntegerMemberValue(cp, ((Integer) value));
		} else if (Enum.class.isAssignableFrom(type)) {
			return getMemberEnum(cp, type, value);
		} else if (Class.class.isAssignableFrom(type)) {
			return new ClassMemberValue(((Class<?>) value).getName(), cp);
		} else if (Annotation.class.isAssignableFrom(type)) {
			return new AnnotationMemberValue(createAnnotation(cp, (Annotation) value), cp);
		} else if(MemberValue.class.isAssignableFrom(type)) {
			return (MemberValue) value;
		}else{
			throw new UnsupportedOperationException("Create annotation for " + type.getName() + " is unsupported");
		}
	}
	

	/**
	 * 新的注解值
	 * @param cp
	 * @param annotationType
	 * @return
	 */
	static MemberValue newAnnotation(ConstPool cp, Class<?> annotationClass) {
		return new AnnotationMemberValue(new javassist.bytecode.annotation.Annotation(annotationClass.getName(), cp), cp);
	}

	/**
	 * 获取注解的枚举值
	 * 
	 * @param cp
	 *            常量池
	 * @param type
	 *            枚举类型
	 * @param value
	 *            值
	 * @return 注解的枚举值
	 */
	private static MemberValue getMemberEnum(ConstPool cp, Class<?> type, Object value) {
		EnumMemberValue rs = new EnumMemberValue(cp);
		rs.setType(type.getName());
		rs.setValue(value.toString());
		return rs;
	}

	/**
	 * 获取注解的数组值
	 * 
	 * @param cp
	 *            常量池
	 * @param value
	 *            数组值
	 * @return 注解的数组值
	 */
	static MemberValue getMemberArray(ConstPool cp, Object value) {
		ArrayMemberValue rs = new ArrayMemberValue(cp);
		int len = Array.getLength(value);
		MemberValue[] values = new MemberValue[len];
		for (int i = 0; i < len; i++) {
			values[i] = getMemberValue(cp, Array.get(value, i));
		}
		rs.setValue(values);
		return rs;
	}

	/**
	 * 判断注解的两个值是否相同
	 * 
	 * @param value
	 *            值
	 * @param defaultValue
	 *            默认值
	 * @return 相同值返回true
	 */
	private static boolean isSameValue(Object value, Object defaultValue) {
		if (defaultValue == null && value != null) {
			return false;
		} else if (value.equals(defaultValue)) {
			return true;
		} else if (value.getClass().isArray() && defaultValue.getClass().isArray()) {
			return isSameArray(value, defaultValue);
		}
		return false;
	}

	/**
	 * 判断数组是否相同
	 * 
	 * @param value
	 *            值
	 * @param defaultValue
	 *            默认值
	 * @return 相同数组返回true
	 */
	private static boolean isSameArray(Object value, Object defaultValue) {
		int len = Array.getLength(value);
		int len2 = Array.getLength(defaultValue);
		if (len == len2) {
			for (int i = 0; i < len; i++) {
				if (!isSameValue(Array.get(value, i), Array.get(defaultValue, i))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
