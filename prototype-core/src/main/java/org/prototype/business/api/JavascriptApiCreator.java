package org.prototype.business.api;

import java.lang.annotation.Annotation;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.prototype.PrototypeConfig;
import org.prototype.PrototypeConfig.Api;
import org.prototype.business.ApiCreator;
import org.prototype.business.Input;
import org.prototype.business.IteratorBuilder;
import org.prototype.business.Prop;
import org.prototype.business.Service;
import org.prototype.business.ServiceClassAdvisor;
import org.prototype.reflect.ClassUtils;
import org.prototype.reflect.Property;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import lombok.Data;

/**
 * 生成javascript验证数据. <br>
 * 此接口只能一次生成一个服务的验证数据。默认为每个URL生成文档（WEB工程） 
 * @author lj
 *
 */
@Component
public class JavascriptApiCreator implements ApiCreator<List<JavascriptApiCreator.Validation>> {

	/**
	 * API类型: javascript
	 */
	public static final String TYPE="javascript";

	@Resource
	private PrototypeConfig config;

	@Override
	public String getType() {
		return TYPE;
	}
	
	@PostConstruct
	void init(){
		Api api=config.getApi().get(TYPE);
		if ( api== null) {
			api = new PrototypeConfig.Api();
			api.setEnable(true);
			config.getApi().put(TYPE, api);
		}
		if(api.getContentType()==null){
			api.setContentType("text/javascript");
		}
	}

	@Override
	public boolean isSupportSingle() {
		return true;
	}

	@Override
	public void createForAll(Collection<Service> services) {
		// do nothing
	}
	/**
	 * 当参数中有多个服务时，将抛出UnsupportedOperationException
	 */
	@Override
	public List<JavascriptApiCreator.Validation> create(Service service)
			throws UnsupportedOperationException {
		Assert.notNull(service);
		try {
			return new ValidationBuilder().build(service);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static class ValidationBuilder extends IteratorBuilder {

		List<Validation> build(Service service) throws Exception {
			List<Validation> list = new ArrayList<>();
			if (service.getParamType() == null) {
				return list;
			}
			for (Property property : getProperties(service.getType(), true)) {
				initInputOutput(property.getField(), true);
				Annotation annotation = property.getField().getAnnotation(Input.class);
				Validation validation = (Validation) build(property.getType(), this, property, annotation);
				list.add(validation);
			}
			return list;
		}

		protected Validation buildString(Property property, Annotation annotation) {
			return createSimple(property.getType(), property.getName(), annotation);
		}

		protected Validation buildBytes(Property property, Annotation annotation) {
			return createSimple(String.class, property.getName(), annotation);
		}

		protected Validation buildBoolean(Property property, Annotation annotation) {
			return createSimple(boolean.class, property.getName(), annotation);
		}

		protected Validation buildCharacter(Property property, Annotation annotation) {
			return createSimple(String.class, property.getName(), annotation);
		}

		protected Validation buildEnum(Property property, Annotation annotation) {
			return createSimple(property.getType(), property.getName(), annotation);
		}

		protected Validation buildPrimitive(Property property, Annotation annotation) {
			return createSimple(property.getType(), property.getName(), annotation);
		}

		protected Validation buildNumber(Property property, Annotation annotation) {
			return createSimple(ClassUtils.getBaseType(property.getType()), property.getName(), annotation);
		}

		protected Validation buildDate(Property property, Annotation annotation) {
			return createSimple(Date.class, property.getName(), annotation);
		}

		protected Validation buildArray(Property property, Annotation annotation) throws Exception {
			Class<?> componentType = property.getType().getComponentType();
			Validation validation = null;
			if (ClassUtils.POJO.equals(ClassUtils.getDataType(componentType))) {
				validation = createPojo(property.getName(), componentType, annotation);
			} else {
				validation = createSimple(componentType, property.getName(), annotation);
			}
			validation.setArray(true);
			return validation;
		}

		protected Validation buildSet(Property property, Annotation annotation) throws Exception {
			Class<?> generic = getGeneric(property.getField(), 0);
			Validation validation = null;
			if (ClassUtils.POJO.equals(ClassUtils.getDataType(generic))) {
				validation = createPojo(property.getName(), generic, annotation);
			} else {
				validation = createSimple(generic, property.getName(), annotation);
			}
			validation.setGeneric(ClassUtils.getBaseType(generic).getSimpleName());
			validation.setType(Set.class.getSimpleName());
			return validation;
		}

		protected Validation buildList(Property property, Annotation annotation) throws Exception {
			Class<?> generic = getGeneric(property.getField(), 0);
			Validation validation = null;
			if (ClassUtils.POJO.equals(ClassUtils.getDataType(generic))) {
				validation = createPojo(property.getName(), generic, annotation);
			} else {
				validation = createSimple(generic, property.getName(), annotation);
			}
			validation.setGeneric(ClassUtils.getBaseType(generic).getSimpleName());
			validation.setType(List.class.getSimpleName());
			return validation;
		}

		protected Validation buildMap(Property property, Annotation annotation) throws Exception {
			Class<?> generic = getGeneric(property.getField(), 1);
			Validation validation = null;
			if (ClassUtils.POJO.equals(ClassUtils.getDataType(generic))) {
				validation = createPojo(property.getName(), generic, annotation);
			} else {
				validation = createSimple(generic, property.getName(), annotation);
			}
			validation.setGeneric(ClassUtils.getBaseType(generic).getSimpleName());
			validation.setType(Map.class.getSimpleName());
			return validation;
		}

		protected Validation buildPojo(Property property, Annotation annotation) throws Exception {
			return createPojo(property.getName(), property.getType(), annotation);
		}

		private Validation createPojo(String name, Class<?> componentType, Annotation annotation) throws Exception {
			Validation rs = new Validation();
			rs.setType(Object.class.getSimpleName());
			Annotation anno = inputOutputs.get(componentType);
			if (anno == null) {
				anno = annotation;
			}
			Input input = (Input) anno;
			if (annotation != null && Prop.class.equals(annotation.annotationType())) {
				Prop prop = (Prop) annotation;
				rs.required = prop.required();
			} else {
				rs.required = input.required();
			}
			rs.childs = new ArrayList<>();
			Map<String, Property> map = ClassUtils.properties(componentType);
			for (Prop prop : input.value()) {
				Property p = map.get(prop.name());
				Validation validation = (Validation) build(p.getType(), this, p, annotation);
				rs.childs.add(validation);
			}
			return rs;
		}

		private Validation createSimple(Class<?> type, String name, Annotation annotation) {
			Prop prop = Prop.class.equals(annotation.annotationType()) ? (Prop) annotation
					: ((Input) annotation).value()[0];
			Validation rs = new Validation();
			rs.name = name;
			rs.type = type.getSimpleName();
			rs.required=prop.required();
			rs.maxLength = prop.maxLength();
			rs.pattern = prop.pattern().length() > 0 && !prop.pattern().startsWith(ServiceClassAdvisor.METHOD_PREFIX)
					? prop.pattern() : null;
			return rs;
		}
	}

	@Data
	public static class Validation {
		private String name;
		private boolean array;
		private String type;
		private String generic;
		private boolean required;
		private int maxLength;
		private String pattern;
		private List<Validation> childs;


		/**
		 * 按name属性组织为映射
		 * @param validations 验证列表
		 * @return name与验证的列表
		 */
		public static Map<String, Validation> getMap(List<Validation> validations){
			Map<String, Validation> rs=new HashMap<>();
			for(Validation type:validations){
				rs.put(type.name, type);
			}
			return rs;
		}
	}
}
