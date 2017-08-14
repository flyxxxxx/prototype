package org.prototype.business.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.prototype.PrototypeConfig;
import org.prototype.PrototypeConfig.Api;
import org.prototype.business.ApiCreator;
import org.prototype.business.Input;
import org.prototype.business.IteratorBuilder;
import org.prototype.business.Output;
import org.prototype.business.Prop;
import org.prototype.business.Service;
import org.prototype.business.ServiceClassAdvisor;
import org.prototype.business.ServiceDefine;
import org.prototype.business.View;
import org.prototype.reflect.ClassUtils;
import org.prototype.reflect.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 生成json接口文档. <br>
 * 此接口只能一次生成一个服务的接口文档。
 * 
 * @author lj
 *
 */
@Slf4j
@Component
public class JsonApiCreator implements ApiCreator<JsonApiCreator.JsonApi> {

	/**
	 * API类型：json
	 */
	public static String TYPE = "json";

	@Resource
	private PrototypeConfig config;

	@Resource
	private ObjectMapper mapper;

	@Autowired(required = false)
	private JsonApiBuilder builder;

	@PostConstruct
	void init() {
		Api api = config.getApi().get(TYPE);
		if (api == null) {
			api = new PrototypeConfig.Api();
			api.setEnable(true);
			config.getApi().put(TYPE, api);
		}
		if (api.getContentType() == null) {
			api.setContentType("application/json");
		}
	}

	@Data
	public static class JsonApi implements java.io.Serializable {

		private static final long serialVersionUID = 2769888494191036571L;

		private String author;
		private String description;
		private String hint;
		private String[] methods;
		private String[] products;
		private String[] consumes;
		private String version;
		private String[] url;

		private List<Type> params;
		private List<Type> results;

		/**
		 * 按name属性组织为映射
		 * 
		 * @param types
		 *            类型列表
		 * @return name与类型的列表
		 */
		public static Map<String, Type> getMap(List<Type> types) {
			Map<String, Type> rs = new HashMap<>();
			for (Type type : types) {
				rs.put(type.name, type);
			}
			return rs;
		}
	}

	@Data
	public static class Type {
		private String name;
		private String description;
		private String hint;
		private String type;
		private boolean array;
		private Boolean required;
		private Integer maxLength;
		private String pattern;
		private List<Type> childs;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean isSupportSingle() {
		return true;
	}

	@Override
	public void createForAll(Collection<Service> services) {
		if (config.getApiRepository() == null) {
			return;
		}
		List<JsonApi> list = new ArrayList<>();
		for (Service service : services) {
			list.add(create(service));
		}
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Accept", "application/json");
		requestHeaders.set("Content-Type", "application/json");

		RestTemplate restTemplate = new RestTemplate();
		try {
			HttpEntity<String> httpEntity = new HttpEntity<String>(mapper.writeValueAsString(list), requestHeaders);
			String[] urls = restTemplate.postForObject(config.getApiRepository(), httpEntity, String[].class);
			if (urls == null || urls.length == 0) {
				return;
			}
			log.warn("The following interfaces conflict with older versions : " + Arrays.asList(urls));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("JSON error", e);
		}
	}

	/**
	 * 构建API的数据
	 * 
	 * @author lj
	 *
	 */
	static interface JsonApiBuilder {
		void build(Service service,JsonApi api);
	}

	/**
	 * 当参数中有多个服务时，将抛出UnsupportedOperationException
	 */
	@Override
	public JsonApi create(Service service) throws UnsupportedOperationException {
		Assert.notNull(service);
		JsonApi api = new JsonApi();
		ServiceDefine define = service.getDefine();
		api.description = define.value();
		api.hint = define.hint();
		api.url = new String[] { define.url() };
		api.version = define.version();
		api.author = define.author();
		if (builder != null) {
			builder.build(service, api);
		}
		try {
			api.params = new JsonApiParamBuilder().build(service, true);
			List<Property> views = ClassUtils.findProperty(service.getType(), View.class);
			if (views.isEmpty()) {
				api.results = new JsonApiParamBuilder().build(service, false);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return api;
	}

	static class JsonApiParamBuilder extends IteratorBuilder {

		private boolean isInput;

		List<Type> build(Service service, boolean isInput) throws Exception {
			this.isInput = isInput;
			List<Type> list = new ArrayList<>();
			Class<?> type = isInput ? service.getParamType() : service.getResultType();
			if (type == null) {
				return list;
			}
			Map<String, Property> map = ClassUtils.properties(type);
			for (Property property : getProperties(service.getType(), isInput)) {
				initInputOutput(property.getField(), isInput);
				Property prop = map.get(property.getField().getName());
				Annotation annotation = isInput ? property.getField().getAnnotation(Input.class)
						: property.getField().getAnnotation(Output.class);
				Type pt = (Type) build(prop.getType(), this, prop, property.getField(), annotation);
				list.add(pt);
			}
			return list;
		}

		protected Type buildString(Property property, Field field, Annotation annotation) {
			return createSimple(property.getType(), field, annotation);
		}

		protected Type buildBytes(Property property, Field field, Annotation annotation) {
			return createSimple(String.class, field, annotation);
		}

		protected Type buildBoolean(Property property, Field field, Annotation annotation) {
			return createSimple(ClassUtils.getBaseType(property.getType()), field, annotation);
		}

		protected Type buildCharacter(Property property, Field field, Annotation annotation) {
			return createSimple(String.class, field, annotation);
		}

		protected Type buildEnum(Property property, Field field, Annotation annotation) {
			return createSimple(property.getType(), field, annotation);
		}

		protected Type buildPrimitive(Property property, Field field, Annotation annotation) {
			return createSimple(property.getType(), field, annotation);
		}

		protected Type buildNumber(Property property, Field field, Annotation annotation) {
			return createSimple(ClassUtils.getBaseType(property.getType()), field, annotation);
		}

		protected Type buildDate(Property property, Field field, Annotation annotation) {
			return createSimple(property.getType(), field, annotation);
		}

		private Type createSimple(Class<?> clazz, Field field, Annotation annotation) {
			Type type = new Type();
			type.setName(field.getName());
			Prop prop = null;
			if (Prop.class.equals(annotation.annotationType())) {
				prop = (Prop) annotation;
			} else {
				prop = isInput ? ((Input) annotation).value()[0] : ((Output) annotation).value()[0];
			}
			type.description = prop.desc();
			type.type = clazz.getSimpleName();
			type.hint = prop.hint();
			if (isInput) {
				type.required = prop.required();
			}
			type.maxLength = prop.maxLength() > 0 ? prop.maxLength() : null;
			type.pattern = prop.pattern().length() > 0 && !prop.pattern().startsWith(ServiceClassAdvisor.METHOD_PREFIX)
					? prop.pattern() : null;
			return type;
		}

		protected Type buildArray(Property property, Field field, Annotation annotation) throws Exception {
			Class<?> componentType = property.getType().getComponentType();
			Type type = null;
			if (ClassUtils.POJO.equals(ClassUtils.getDataType(componentType))) {
				type = createPojo(field.getName(), componentType, field.getType().getComponentType(), annotation);
			} else {
				type = createSimple(componentType, field, annotation);
			}
			type.setArray(true);
			return type;
		}

		protected Type buildSet(Property property, Field field, Annotation annotation) throws Exception {
			Class<?> generic = getGeneric(property.getField(), 0);
			Type type = null;
			if (ClassUtils.POJO.equals(ClassUtils.getDataType(generic))) {
				type = createPojo(field.getName(), generic, getGeneric(field, 0), annotation);
			} else {
				type = createSimple(generic, field, annotation);
			}
			type.setType("Set<" + generic.getName() + ">");
			return type;
		}

		protected Type buildList(Property property, Field field, Annotation annotation) throws Exception {
			Class<?> generic = getGeneric(property.getField(), 0);
			Type type = null;
			if (ClassUtils.POJO.equals(ClassUtils.getDataType(generic))) {
				type = createPojo(field.getName(), generic, getGeneric(field, 0), annotation);
			} else {
				type = createSimple(generic, field, annotation);
			}
			type.setType("List<" + generic.getName() + ">");
			return type;
		}

		protected Type buildMap(Property property, Field field, Annotation annotation) throws Exception {
			Class<?> generic = getGeneric(property.getField(), 1);
			Type type = null;
			if (ClassUtils.POJO.equals(ClassUtils.getDataType(generic))) {
				type = createPojo(field.getName(), generic, getGeneric(field, 1), annotation);
			} else {
				type = createSimple(generic, field, annotation);
			}
			type.setType("Map<String," + generic.getName() + ">");
			return type;
		}

		protected Type buildPojo(Property property, Field field, Annotation annotation) throws Exception {
			return createPojo(field.getName(), property.getType(), field.getType(), annotation);
		}

		private Type createPojo(String name, Class<?> targetType, Class<?> sourceType, Annotation annotation)
				throws Exception {
			Type rs = new Type();
			rs.name = name;
			rs.type = targetType.getSimpleName();
			Annotation anno = inputOutputs.get(sourceType);
			if (anno == null) {
				anno = annotation;
			}
			Input input = isInput ? (Input) anno : null;
			Output output = isInput ? null : (Output) anno;
			if (annotation != null && Prop.class.equals(annotation.annotationType())) {
				Prop prop = (Prop) annotation;
				rs.required = prop.required();
				rs.description = prop.desc();
				rs.hint = prop.hint();
			} else {
				rs.required = input == null ? output.required() : input.required();
				rs.description = input == null ? output.desc() : input.desc();
				rs.hint = input == null ? output.hint() : input.hint();
			}
			rs.childs = new ArrayList<>();
			Map<String, Property> map = ClassUtils.properties(targetType);
			Map<String, Property> ps = ClassUtils.properties(sourceType);
			for (Prop prop : (isInput ? input.value() : output.value())) {
				Property p = map.get(prop.name());
				Type t = (Type) build(p.getType(), this, p, ps.get(prop.name()).getField(), prop);
				rs.childs.add(t);
			}
			return rs;
		}
	}
}
