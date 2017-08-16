package org.prototype.business;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.prototype.core.ParameterInject;
import org.prototype.core.PrototypeStatus;
import org.prototype.inject.InjectHelper;
import org.prototype.reflect.AnnotationUtils;
import org.prototype.reflect.ClassUtils;
import org.prototype.reflect.MethodUtils;
import org.prototype.reflect.Property;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 初始化目标对象实例. <br>
 * 
 * @author lj
 *
 */
@SuppressWarnings("rawtypes")
@Order(ExecuteFilter.INIT)
@Component
@Slf4j
public class InitTargetExecuteFilter implements ExecuteFilter {

	private static final List<Class<?>> TYPES = Arrays.asList(int.class, Integer.class, byte.class, Byte.class,
			short.class, Short.class, long.class, Long.class, float.class, Float.class, double.class, Double.class);

	@Resource
	private InjectHelper helper;
	
	@Resource
	private GetResultExecuteFilter getResult;

	@Override
	public void doFilter(ExecuteChain chain) throws Exception {
		Constructor<?> constructor = chain.getType().getConstructors()[0];
		Object[] params = chain.getParams();
		List<?>[] lists = groupParameters(constructor.getParameters(), params);
		Object target = constructor.newInstance(lists[0].toArray());
		chain.setTarget(target);
		copyProperties(chain, chain.getService().getParamType(), lists[1].toArray(), target);
		if (!chain.isValidated()) {
			log.debug("Business {} validate {} failed",PrototypeStatus.getStatus().getId(),chain.getService().getType());
		}
		chain.doChain();
	}

	/**
	 * 复制属性
	 * 
	 * @param parameType
	 *            参数类型
	 * @param parameter
	 *            参数
	 * @param target
	 *            目标对象
	 * @throws Exception
	 *             异常
	 */
	private void copyProperties(ExecuteChain chain, Class<?> parameType, Object[] parameter, Object target)
			throws Exception {
		if (parameType == null) {
			return ;
		}
		Map<String, Property> props = new TreeMap<>(ClassUtils.properties(parameType));
		Map<String, Object> values = new HashMap<>();
		if (parameter.length == 1 && parameType.isInstance(parameter[0])) {
			for (Map.Entry<String, Property> obj : props.entrySet()) {
				Object value = obj.getValue().getValue(parameter[0]);
				if (value != null) {
					values.put(obj.getKey(), value);
				}
			}
		} else {
			int k = 0;
			for (Map.Entry<String, Property> obj : props.entrySet()) {
				if (parameter[k] != null) {
					values.put(obj.getKey(), parameter[k]);
				}
				k++;
			}
		}
		new ParamSetter(chain, target).build(values);
	}

	/**
	 * 业务类的构造方法支持ParameterInject注解
	 * 
	 * @param parameters
	 *            方法参数
	 * @param params
	 *            参数
	 * @return 构建参数及输入参数分组
	 */
	private List<?>[] groupParameters(Parameter[] parameters, Object[] params) {
		ArrayList<Object> list = new ArrayList<>();//构造方法参数
		int k = 0;
		for (Parameter parameter : parameters) {
			Annotation[] annotations=AnnotationUtils.getAnnotationByMeta(parameter.getAnnotations(), ParameterInject.class);
			if (annotations.length>0) {
				list.add(helper.getInjectParameter(annotations[0]));
			} else {
				list.add(params[k++]);
			}
		}
		ArrayList<Object> list2 = new ArrayList<>();//Input注解的属性
		for (int i = k, m = params.length; i < m; i++) {
			list2.add(params[i]);
		}
		return new List<?>[] { list, list2 };
	}

	static class ParamSetter extends IteratorBuilder {

		private ExecuteChain chain;

		/**
		 * 目标对象
		 */
		private Object target;

		/**
		 * 目标对象属性
		 */
		private Map<String, Property> properties;

		/**
		 * 构造
		 * 
		 * @param target
		 *            目标对象
		 */
		public ParamSetter(ExecuteChain chain, Object target) {
			this.chain = chain;
			this.target = target;
			this.properties = ClassUtils.properties(target.getClass());
		}

		/**
		 * 构建目标对象
		 * 
		 * @param params
		 *            输入参数（成员变量名与值的映射）
		 * @throws Exception
		 *             异常
		 */
		public void build(Map<String, Object> params) throws Exception {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				Property property = properties.get(entry.getKey());
				Field field = property.getField();
				initInputOutput(field, true);
				Object value = build(field.getType(), this, property, target, field.getType(), entry.getValue(),
						field.getAnnotation(Input.class));
				if (value != null) {
					property.setValue(target, value);
				}
			}
		}

		protected Object buildPrimitive(Property property, Object target, Class<?> type, Object value,
				Annotation annotation) throws Exception {
			return buildNumber(property, target, type, value, annotation);
		}

		protected Object buildBoolean(Property property, Object target, Class<?> type, Object value,
				Annotation annotation) throws Exception {
			if (validate(type, Input.class.equals(annotation.annotationType()) ? ((Input) annotation).value()[0]
					: (Prop) annotation, value)) {
				return value;
			}
			return null;
		}

		protected Object buildBytes(Property property, Object target, Class<?> type, Object value,
				Annotation annotation) throws Exception {
			if (validate(type, Input.class.equals(annotation.annotationType()) ? ((Input) annotation).value()[0]
					: (Prop) annotation, value)) {
				return ((String)value).getBytes();
			}
			return null;
		}

		protected Object buildCharacter(Property property, Object target, Class<?> type, Object value,
				Annotation annotation) throws Exception {
			if (validate(type, Input.class.equals(annotation.annotationType()) ? ((Input) annotation).value()[0]
					: (Prop) annotation, value)) {
				return ((String)value).charAt(0);
			}
			return null;
		}

		protected Object buildDate(Property property, Object target, Class<?> type, Object value, Annotation annotation)
				throws Exception {
			Prop prop = Input.class.equals(annotation.annotationType()) ? ((Input) annotation).value()[0]
					: (Prop) annotation;
			if (!validate(type, prop, value)) {
				return null;
			}
			if (prop.pattern().length() > 0) {
				return getDateFormat(prop.pattern()).parse((String) value);
			} else if (type.isInstance(value)) {
				return value;
			}
			return type.getConstructor(long.class).newInstance(((Date) value).getTime());
		}

		@SuppressWarnings("unchecked")
		protected Object buildEnum(Property property, Object target, Class<?> type, Object value, Annotation annotation)
				throws Exception {
			Prop prop = Input.class.equals(annotation.annotationType()) ? ((Input) annotation).value()[0]
					: (Prop) annotation;
			if (validate(type, prop, value)) {
				return Enum.valueOf((Class<Enum>) type, (String) value);
			}
			return null;
		}

		protected Object buildString(Property property, Object target, Class<?> type, Object value,
				Annotation annotation) throws Exception {
			Prop prop = Input.class.equals(annotation.annotationType()) ? ((Input) annotation).value()[0]
					: (Prop) annotation;
			if (!validate(type, prop, value)) {
				return null;
			}
			if (prop.pattern().length() > 0) {
				Method method = MethodUtils.findMethod(ParamSetter.this.target.getClass(),
						prop.pattern().substring(ServiceClassAdvisor.METHOD_PREFIX.length()), String.class);
				return method.invoke(ParamSetter.this.target, (String) value);
			}
			return value;
		}

		protected Object buildArray(Property property, Object target, Class<?> type, Object value,
				Annotation annotation) throws Exception {
			Class<?> clazz = type.getComponentType();
			Method method = findMethod(this, clazz);
			int length = Array.getLength(value);
			Object array = Array.newInstance(clazz, length);
			for (int i = 0; i < length; i++) {
				Array.set(array, i, method.invoke(this, property, target, clazz, Array.get(value, i), annotation));
			}
			return array;
		}

		@SuppressWarnings({ "unchecked" })
		protected Object buildList(Property property, Object target, Class<?> type, Object value, Annotation annotation)
				throws Exception {
			Class<?> generic = getGeneric(property.getField(), 0);
			Class<?> arrayType = Array.newInstance(generic, 0).getClass();
			Object[] array = (Object[]) buildArray(property, target, arrayType, ((Collection) value).toArray(),
					annotation);
			Collection collection = (Collection) property.getValue(target);
			if (collection == null) {
				return IteratorBuilder.initCollection(type, array);
			} else {
				collection.addAll(Arrays.asList(array));
				return null;
			}
		}

		protected Object buildSet(Property property, Object target, Class<?> type, Object value, Annotation annotation)
				throws Exception {
			return buildList(property, target, type, value, annotation);
		}

		@SuppressWarnings({"unchecked" })
		protected Object buildMap(Property property, Object target, Class<?> type, Object value, Annotation annotation)
				throws Exception {
			Class<?> generic = getGeneric(property.getField(), 1);
			Map<String, Object> map = new HashMap<String, Object>((Map<String, Object>) value);
			Method method = findMethod(this, generic);
			for (String key : map.keySet().toArray(new String[map.size()])) {
				Object v = map.get(key);
				map.put(key, method.invoke(this, property, target, generic, v, annotation));
			}
			Map values = (Map) property.getValue(target);
			if (values == null) {
				return IteratorBuilder.initMap(type, map);
			} else {
				values.putAll(map);
			}
			return null;
		}

		protected Object buildPojo(Property property, Object target, Class<?> type, Object value, Annotation annotation)
				throws Exception {
			Annotation anno = annotation;
			if (annotation == null) {
				anno = inputOutputs.get(type);
			}
			Object rs = type.newInstance();
			if (anno.annotationType().equals(Prop.class)) {
				Annotation annotation2 = inputOutputs.get(type);
				if (annotation2 == null) {
					ClassUtils.findIdProperty(type).setValue(rs, value);
					return rs;
				} else {
					anno = annotation2;
				}
			}
			Input input = (Input) anno;
			Map<String, Property> ps = ClassUtils.properties(value.getClass());
			Map<String, Property> props = ClassUtils.properties(type);
			for (Prop prop : input.value()) {
				Object v = ps.get(prop.name()).getValue(value);
				Property p = props.get(prop.name());
				Object object = build(p.getField().getType(), this, p, rs, p.getField().getType(), v, prop);
				props.get(prop.name()).setValue(rs, object);
			}
			return rs;
		}

		private boolean validate(Class<?> targetType, Prop prop, Object value) {
			if (value == null) {
				if (prop.required()) {
					chain.addValidateError(prop.name() + ".required");
					return false;
				}
			} else if (!String.class.isInstance(value)) {
				return true;
			} 
			String str=(String) value;
			if(str.length()==0){
				if (prop.required()) {
					chain.addValidateError(prop.name() + ".required");
					return false;
				}
				return true;
			}
			if (prop.maxLength() > 0 && str.length() > prop.maxLength()) {
				chain.addValidateError(prop.name() + ".maxLength");
				return false;
			} else if (prop.pattern().length() > 0 && !prop.pattern().startsWith(ServiceClassAdvisor.METHOD_PREFIX)) {
				if (!targetType.isPrimitive()&& !Number.class.isAssignableFrom(targetType) && !Date.class.isAssignableFrom(targetType)
						&& !Pattern.matches(prop.pattern(), (String) value)) {
					chain.addValidateError(prop.name() + ".pattern");
					return false;
				}
			}
			return true;
		}

		protected Object buildNumber(Property property, Object target, Class<?> type, Object value,
				Annotation annotation) throws Exception {
			Prop prop = Input.class.equals(annotation.annotationType()) ? ((Input) annotation).value()[0]
					: (Prop) annotation;
			if (!validate(type, prop, value)) {
				return null;
			}
			if (Number.class.isInstance(value)) {
				return value;
			}
			if (prop.pattern().startsWith(ServiceClassAdvisor.METHOD_PREFIX)) {
				Method method = MethodUtils.findMethod(ParamSetter.this.target.getClass(),
						prop.pattern().substring(ServiceClassAdvisor.METHOD_PREFIX.length()), String.class);
				return method.invoke(ParamSetter.this.target, (String) value);
			} else {
				Number number = getDecimalFormat(prop.pattern()).parse((String) value);
				return getNumber(type, number);
			}
		}

		private Object getNumber(Class<?> type, Number number) throws Exception {
			switch (TYPES.indexOf(type)) {
			case 0:
			case 1:
				return new Integer(number.intValue());
			case 2:
			case 3:
				return new Byte(number.byteValue());
			case 4:
			case 5:
				return new Short(number.shortValue());
			case 6:
			case 7:
				return new Long(number.longValue());
			case 8:
			case 9:
				return new Float(number.floatValue());
			case 10:
			case 11:
				return new Double(number.doubleValue());
			default:
				return type.getConstructor(String.class).newInstance(number.toString());
			}
		}
	}

}
