package org.prototype.business;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.prototype.reflect.ClassUtils;
import org.prototype.reflect.MethodUtils;
import org.prototype.reflect.Property;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@SuppressWarnings("rawtypes")
@Order(ExecuteFilter.RESULT)
@Component
public class GetResultExecuteFilter implements ExecuteFilter {

	@Override
	public void doFilter(ExecuteChain chain) throws Exception {
		if(chain.isValidated()){
			chain.doChain();
		}
		Class<?> type = chain.getService().getResultType();
		if (type == null) {
			return;
		}
		Object rs = type.newInstance();
		new ResultSetter(chain.getTarget(), rs).build();
		chain.setResult(rs);
	}
	
	/**
	 * 结果转换
	 * @param result 原结果
	 * @param target 目标结果
	 * @throws Exception 异常
	 */
	static <T> T processResult(Object result,Class<T> type) throws Exception{
		T rs=type.newInstance();
		new ResultSetter(result, rs).build();
		return rs;
	}

	static class ResultSetter extends IteratorBuilder {

		private Object source;

		private Object target;

		private Map<String, Property> properties;

		public ResultSetter(Object source, Object target) {
			this.source = source;
			this.target = target;
			properties = ClassUtils.properties(target.getClass());
		}

		public void build() throws Exception {
			Map<String, Property> ps = ClassUtils.properties(source.getClass());
			for (Map.Entry<String, Property> entry : properties.entrySet()) {
				Property sp = ps.get(entry.getKey());
				initInputOutput(sp.getField(), false);
				FieldSetter setter = new FieldSetter();
				setter.target = target;
				setter.property = entry.getValue();
				setter.field = sp.getField();
				setter.value = sp.getValue(source);
				setter.build();
			}
		}

		private class FieldSetter {

			private Object value;
			private Property property;
			private Field field;
			private Object target;
			private String pattern;
			private Set<Object> objects=new HashSet<>();

			public void build() throws Exception {
				if (value == null) {
					return;
				}
				Output output = field.getAnnotation(Output.class);
				if (output != null && output.value().length == 1) {
					this.pattern = output.value()[0].pattern();
					if (pattern.startsWith(ServiceClassAdvisor.METHOD_PREFIX)) {
						String name = pattern.substring(ServiceClassAdvisor.METHOD_PREFIX.length());
						Method method = MethodUtils.findMethod(ResultSetter.this.source.getClass(), name,
								field.getType());
						property.setValue(target, method.invoke(ResultSetter.this.source, value));
						return;
					}
				}
				Object rs = ResultSetter.this.build(field.getType(), this, property.getType(), value, output);
				property.setValue(target, rs);
			}

			@SuppressWarnings("unused") // 反射调用
			protected Object buildArray(Class<?> type, Object value, Output output) throws Exception {
				return getArray(type.getComponentType(), value, output);
			}

			private Object getArray(Class<?> type, Object value, Output output) throws Exception {
				int length = Array.getLength(value);
				pattern = output.value()[0].pattern();
				Object rs = Array.newInstance(pattern.length() > 0 ? String.class : type, length);
				Method method = ResultSetter.this.findMethod(this, type);
				for (int i = 0; i < length; i++) {
					Array.set(rs, i, method.invoke(this, type, Array.get(value, i), output));
				}
				return rs;
			}

			@SuppressWarnings("unused") // 反射调用
			protected Object buildList(Class<?> type, Object value, Output output) throws Exception {
				Class<?> clazz = IteratorBuilder.getGeneric(field, 0);
				Output put = output == null ? (Output) inputOutputs.get(clazz) : output;
				Object[] objects = ((Collection) value).toArray();
				Object[] array = (Object[]) getArray(clazz, objects, put);
				return Arrays.asList(array);
			}

			@SuppressWarnings("unused") // 反射调用
			protected Object buildSet(Class<?> type, Object value, Output output) throws Exception {
				Class<?> clazz = IteratorBuilder.getGeneric(field, 0);
				Output put = output == null ? (Output) inputOutputs.get(clazz) : output;
				Object[] objects = ((Collection) value).toArray();
				Object[] array = (Object[]) getArray(clazz, objects, put);
				return new HashSet<>(Arrays.asList(array));
			}

			@SuppressWarnings("unused") // 反射调用
			protected Object buildBytes(Class<?> type, Object value, Output output) {
				return new String((byte[])value);
			}

			@SuppressWarnings("unused") // 反射调用
			protected Object buildString(Class<?> type, Object value, Output output) {
				return value;
			}

			@SuppressWarnings("unused") // 反射调用
			protected Object buildEnum(Class<?> type, Object value, Output output) {
				return value;
			}

			@SuppressWarnings("unused") // 反射调用
			protected Object buildDate(Class<?> type, Object value, Output output) {
				if (pattern != null && pattern.length() > 0) {
					return ResultSetter.this.getDateFormat(pattern).format((Date) value);
				}
				return value;
			}

			@SuppressWarnings({ "unused", "unchecked" }) // 反射调用
			protected Object buildMap(Class<?> type, Object value, Output output) throws Exception {
				Map<String, Object> map = (Map<String, Object>) value;
				Class<?> clazz = IteratorBuilder.getGeneric(field, 1);
				Method method = ResultSetter.this.findMethod(this, clazz);
				Output put = output == null ? (Output) inputOutputs.get(clazz) : output;
				pattern = put.value()[0].pattern();
				for (String key : map.keySet().toArray(new String[map.size()])) {
					map.put(key, method.invoke(this, clazz, map.get(key), output));
				}
				return new LinkedHashMap<>(map);
			}

			@SuppressWarnings("unused") // 反射调用
			protected Object buildPrimitive(Class<?> type, Object value, Output output) throws Exception {
				if (pattern != null && pattern.length() > 0) {
					return ResultSetter.this.getDecimalFormat(pattern).format(value);
				}
				return value;
			}

			@SuppressWarnings("unused") // 反射调用
			protected Object buildNumber(Class<?> type, Object value, Output output) {
				if (pattern != null && pattern.length() > 0) {
					return ResultSetter.this.getDecimalFormat(pattern).format(value);
				}
				return value;
			}

			@SuppressWarnings("unused") // 反射调用
			protected Object buildBoolean(Class<?> type, Object value, Output output) {
				return value;
			}

			@SuppressWarnings("unused") // 反射调用
			protected Object buildCharacter(Class<?> type, Object value, Output output) {
				return value.toString();
			}

			@SuppressWarnings("unused") // 反射调用
			protected Object buildPojo(Class<?> type, Object value, Output output) throws Exception {
				Output put = (output == null || (output.type()!=void.class&&output.type() != type))
						? (Output) ResultSetter.this.inputOutputs.get(type) : output;
				if (put == null||objects.contains(value)) {
					return ClassUtils.findIdProperty(value.getClass()).getValue(value);
				}
				objects.add(value);//防止循环引用
				Constructor constructor=type.getDeclaredConstructor();
				constructor.setAccessible(true);
				Object rs = constructor.newInstance();
				Map<String, Property> ps = ClassUtils.properties(type);
				Map<String, Property> props = ClassUtils.properties(value.getClass());
				for (Prop prop : put.value()) {
					Property property = props.get(prop.name());
					pattern = prop.pattern();
					Object sourceValue = property.getValue(value);
					if (sourceValue != null) {
						Object v = ResultSetter.this.build(property.getType(), this, property.getType(), sourceValue,
								put);
						ps.get(prop.name()).setValue(rs, v);
					}
				}
				return rs;
			}

		}

	}

}
