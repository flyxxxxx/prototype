package org.prototype.javassist;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

public class CtMethodUtils {

	private CtMethodUtils() {
		// do nothing;
	}

	private static Map<String, List<CtMethod>> getAllMethods(CtClass clazz) {
		Map<String, List<CtMethod>> methods = new HashMap<>();
		addMethods(methods, clazz, 0);
		return methods;
	}

	private static void addMethods(Map<String, List<CtMethod>> map, CtClass clazz, int range) {
		for (CtMethod method : clazz.getDeclaredMethods()) {
			String name = method.getName();
			if (isInRange(method, range)) {
				List<CtMethod> list = map.get(name);
				if (list == null) {
					list = new ArrayList<>();
				} else if (exists(list, method)) {
					continue;
				}
				list.add(method);
				map.put(name, list);
			}
		}
	}

	private static boolean exists(List<CtMethod> list, CtMethod method) {
		for (CtMethod cm : list) {
			try {
				CtClass[] p1 = cm.getParameterTypes();
				CtClass[] p2 = method.getParameterTypes();
				if (isSame(p1, p2)) {
					return true;
				}
			} catch (NotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return false;
	}

	private static boolean isSame(CtClass[] p1, CtClass[] p2) {
		int k = p1.length;
		if (k != p2.length) {
			return false;
		}
		for (int i = 0; i < k; i++) {
			if (!p1[i].getName().equals(p2[i].getName())) {
				return false;
			}
		}
		return true;
	}

	private static boolean isInRange(CtMethod method, int range) {
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

	static List<CtMethod> findMethod(CtClass clazz, String methodName) {
		List<CtMethod> rs = getAllMethods(clazz).get(methodName);
		return rs == null ? new ArrayList<CtMethod>() : rs;
	}

	static CtMethod findMethod(CtClass clazz, String methodName, Class<?>... parameterType) {
		List<CtMethod> rs = getAllMethods(clazz).get(methodName);
		if (rs == null) {
			return null;
		}
		try {
			for (CtMethod method : rs) {
				if (matches(method, parameterType)) {
					return method;
				}
			}
			return null;
		} catch (NotFoundException e) {
			throw new RuntimeException("Class not found", e);
		}
	}

	private static boolean matches(CtMethod method, Class<?>[] parameterType) throws NotFoundException {
		CtClass[] ps = method.getParameterTypes();
		if (ps.length != parameterType.length) {
			return false;
		}
		for (int i = 0, k = ps.length; i < k; i++) {
			if (!ps[i].getName().equals(parameterType[i].getName())) {
				return false;
			}
		}
		return true;
	}

	static String buildNewMethod(int advisorIndex, boolean debug,String fromClass, String returnType, String methodName,
			Class<?>[] parameterTypes) {
		return buildNewMethod(advisorIndex,debug, fromClass, returnType, methodName, buildParametersCode(parameterTypes));

	}

	private static String buildNewMethod(int advisorIndex,boolean debug, String fromClass, String returnType, String methodName,
			String paramsCode) {
		StringBuilder rs = new StringBuilder("{");
		String typeName = getWrappedType(returnType);
		if (!"void".equals(returnType)) {
			rs.append("return (");
			rs.append(typeName == null ? returnType : ("(" + typeName));
			rs.append(")");
		}
		rs.append(ClassScanerImpl.class.getName());
		rs.append(".execute(");
		rs.append(advisorIndex);
		rs.append(",");
		rs.append(debug);
		rs.append(",");
		rs.append(fromClass);
		rs.append(".class,$0,\"");
		rs.append(methodName);
		rs.append("\",");
		rs.append(paramsCode);
		rs.append(",$args)");
		rs.append(typeName == null ? "" : (")." + returnType + "Value()"));
		rs.append(";}");
		return rs.toString();
	}

	/**
	 * 构造一个方法的代理方法代码
	 * 
	 * @param contextIndex
	 *            SpringContext上下文索引
	 * @param method
	 *            被调用的方法
	 * @param advisor
	 *            方法代理
	 * @return 新方法
	 * @throws NotFoundException
	 */
	static String buildNewMethod(int advisorIndex,boolean debug, CtMethod method) throws NotFoundException {
		return buildNewMethod(advisorIndex,debug, method.getDeclaringClass().getName(), method.getReturnType().getName(),
				method.getName(), buildParametersCode(method));
	}

	/**
	 * 获得方法描述
	 * 
	 * @param method
	 *            方法
	 * @return
	 * @throws NotFoundException
	 */
	static String getDescription(CtMethod method) {
		try {
			StringBuilder rs = new StringBuilder();
			appendModifier(rs, method);
			rs.append(buildTypeNameCode(method.getReturnType()));
			rs.append(" ");
			rs.append(method.getDeclaringClass().getName());
			rs.append(".");
			rs.append(method.getName());
			rs.append("(");
			buildParametersCode(method);
			for (CtClass cls : method.getParameterTypes()) {
				rs.append("");
				rs.append(buildTypeNameCode(cls));
				rs.append(".class,");
			}
			if (rs.charAt(rs.length() - 1) == ',') {
				rs.deleteCharAt(rs.length() - 1);
			}
			rs.append(")");
			return rs.toString();
		} catch (NotFoundException e) {
			throw new RuntimeException("Class not found", e);
		}
	}

	private static void appendModifier(StringBuilder rs, CtMethod method) {
		int mod = method.getModifiers();
		if (Modifier.isPublic(mod)) {
			rs.append("public ");
		} else if (Modifier.isProtected(mod)) {
			rs.append("protected ");
		} else if (Modifier.isPrivate(mod)) {
			rs.append("private ");
		}
		if (Modifier.isFinal(mod)) {
			rs.append("final ");
		} else if (Modifier.isAbstract(mod)) {
			rs.append("abstract ");
		}
		if (Modifier.isStatic(mod)) {
			rs.append("static ");
		}
		if (Modifier.isNative(mod)) {
			rs.append("native ");
		}
		if (Modifier.isSynchronized(mod)) {
			rs.append("synchronized ");
		}
		if (Modifier.isStrict(mod)) {
			rs.append("strictfp ");
		}
	}

	private static String buildParametersCode(CtMethod method) throws NotFoundException {
		CtClass[] types = method.getParameterTypes();
		if (types.length == 0) {
			return "new Class[0]";
		}
		StringBuilder rs = new StringBuilder("new Class[]{");
		for (CtClass cls : method.getParameterTypes()) {
			rs.append(buildTypeNameCode(cls));
			rs.append(".class,");
		}
		rs.deleteCharAt(rs.length() - 1);
		rs.append("}");
		return rs.toString();
	}

	private static String buildParametersCode(Class<?>[] parameterTypes) {
		if (parameterTypes.length == 0) {
			return "new Class[0]";
		}
		StringBuilder rs = new StringBuilder("new Class[]{");
		for (Class<?> cls : parameterTypes) {
			rs.append(buildTypeNameCode(cls));
			rs.append(".class,");
		}
		rs.deleteCharAt(rs.length() - 1);
		rs.append("}");
		return rs.toString();
	}

	/**
	 * 构造类型名代码
	 * 
	 * @param type
	 * @return
	 * @throws NotFoundException
	 */
	private static String buildTypeNameCode(Class<?> type) {
		StringBuilder rs = new StringBuilder();
		Class<?> ctClass = type;
		while (ctClass.isArray()) {
			rs.append("[]");
			ctClass = ctClass.getComponentType();
		}
		rs.insert(0, ctClass.getName());
		return rs.toString();
	}

	/**
	 * 构造类型名代码
	 * 
	 * @param type
	 * @return
	 * @throws NotFoundException
	 */
	private static String buildTypeNameCode(CtClass type) throws NotFoundException {
		StringBuilder rs = new StringBuilder();
		CtClass ctClass = type;
		while (ctClass.isArray()) {
			rs.append("[]");
			ctClass = ctClass.getComponentType();
		}
		rs.insert(0, ctClass.getName());
		return rs.toString();
	}

	/**
	 * 获取包装类型，没有包装返回null
	 * 
	 * @param typeName
	 *            java类型名
	 * @return 包装类型
	 */
	private static String getWrappedType(String typeName) {
		switch (typeName) {
		case "int":
			return Integer.class.getName();
		case "boolean":
			return Boolean.class.getName();
		case "byte":
			return Byte.class.getName();
		case "char":
			return Character.class.getName();
		case "short":
			return Short.class.getName();
		case "long":
			return Long.class.getName();
		case "float":
			return Float.class.getName();
		case "double":
			return Float.class.getName();
		default:
			return null;
		}
	}

	public static String[] getMethodParamNames(Method method) {
		CtMethod ctmethod = getMethod(method);
		return getMethodParamNames(ctmethod, ctmethod.getDeclaringClass().isFrozen());
	}

	public static String[] getMethodParamNames(CtMethod method) {
		return getMethodParamNames(method, method.getDeclaringClass().isFrozen());
	}

	/**
	 * 获取对应javassist方法
	 * 
	 * @param method
	 *            JAVA方法
	 * @return javassist方法
	 */
	private static CtMethod getMethod(Method method) {
		ClassPool pool = new ClassPool(true);
		try {
			ClassClassPath classPath = new ClassClassPath(method.getDeclaringClass());
			pool.insertClassPath(classPath);
			CtClass cc = pool.get(method.getDeclaringClass().getName());
			return getMethod(cc, method);
		} catch (NotFoundException e) {
			throw new java.lang.IllegalStateException(e);
		}
	}

	/**
	 * 获取方法
	 * 
	 * @param cc
	 *            javassist类
	 * @param method
	 *            java方法
	 * @return javassist方法
	 */
	static CtMethod getMethod(CtClass cc, java.lang.reflect.Method method) {
		Class<?>[] paramTypes = method.getParameterTypes();
		String[] paramTypeNames = new String[paramTypes.length];
		for (int i = 0; i < paramTypes.length; i++) {
			paramTypeNames[i] = paramTypes[i].getName();
		}
		String name = method.getName();
		int k = name.lastIndexOf('$');
		if (k != -1) {
			name = name.substring(0, k);
		}
		try {
			return cc.getDeclaredMethod(name, cc.getClassPool().get(paramTypeNames));
		} catch (NotFoundException e) {
			try {
				return cc.getDeclaredMethod(method.getName(), cc.getClassPool().get(paramTypeNames));
			} catch (NotFoundException e1) {
				throw new java.lang.IllegalStateException(e);
			}
		}
	}

	private static String[] getMethodParamNames(CtMethod cm, boolean frozen) {
		MethodInfo methodInfo = frozen ? cm.getMethodInfo2() : cm.getMethodInfo();
		CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
		LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
		if (attr == null) {
			return new String[0];
		}
		String[] paramNames = null;
		try {
			paramNames = new String[cm.getParameterTypes().length];
		} catch (NotFoundException e) {
			throw new java.lang.IllegalStateException(e);
		}
		int pos = 0;
		for (int i = 0;; i++) {
			if ("this".equals(attr.variableName(i))) {
				pos = i + 1;
				break;
			}
		}
		for (int i = 0; i < paramNames.length; i++) {
			try {
				paramNames[i] = attr.variableName(i + pos);
			} catch (Exception e) {
			}
		}
		return paramNames;
	}

	/**
	 * 构建GET方法代码
	 * 
	 * @param name
	 * @param type
	 * @return
	 */
	static String buildGetMethod(String name, Class<?> type) {
		StringBuilder builder = new StringBuilder("public ");
		builder.append(getTypeString(type));
		builder.append(" ");
		builder.append(boolean.class.equals(type) ? "is" : "get");
		builder.append(name.substring(0, 1).toUpperCase() + name.substring(1));
		builder.append("(){ return this.");
		builder.append(name);
		builder.append(";}");
		return builder.toString();
	}

	private static String getTypeString(Class<?> type) {
		StringBuilder builder = new StringBuilder();
		Class<?> clazz = type;
		while (clazz.isArray()) {
			builder.append("[]");
			clazz = clazz.getComponentType();
		}
		builder.insert(0, clazz.getName());
		return builder.toString();
	}

	/**
	 * 构建SET方法代码
	 * 
	 * @param name
	 * @param type
	 * @return
	 */
	static String buildSetMethod(String name, Class<?> type) {
		StringBuilder builder = new StringBuilder("public void set");
		builder.append(name.substring(0, 1).toUpperCase() + name.substring(1));
		builder.append("(");
		builder.append(getTypeString(type));
		builder.append(" ");
		builder.append(name);
		builder.append("){this.");
		builder.append(name);
		builder.append("=");
		builder.append(name);
		builder.append(";}");
		return builder.toString();
	}
}
