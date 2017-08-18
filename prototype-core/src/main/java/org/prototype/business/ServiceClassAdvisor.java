package org.prototype.business;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.prototype.PrototypeConfig;
import org.prototype.core.AnnotationBuilder;
import org.prototype.core.ChainOrder;
import org.prototype.core.ClassAdvisor;
import org.prototype.core.ClassBuilder;
import org.prototype.core.ClassFactory;
import org.prototype.core.ComponentContainer;
import org.prototype.core.ConstructorBuilder;
import org.prototype.core.Errors;
import org.prototype.core.FieldBuilder;
import org.prototype.reflect.ClassUtils;
import org.prototype.reflect.MethodUtils;
import org.prototype.reflect.Property;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * 创建服务输入参数及输出结果类. <br>
 * 
 * <pre>
 * Enum类型、char、Character、byte[]类型输入参数或输出结果映射为String
 * 不支持char和Character的数组、集合或映射，需要可用String代替.
 * 除boolean和char之外其它6种基本类型及其包装类型，Number的子类型、java.util.Date及子类，如果指定pattern属性值，则映射为String，也就是作为输入时，需要提交字符串参数；为输出时以字符串形式输出，
 * java.util.Date及子类均映射为java.util.Date，
 * java.util.Set及其子类(包括EnumSet)均映射为java.util.Set，必须指定明确的泛型类型，不能是Set&lt;?&gt;或Set&lt;T&gt;这些形式.
 * java.util.Collection及其子类（不包括Set及其子类）均映射为java.util.List，必须指定明确的泛型类型，不能是List&lt;?&gt;或List&lt;T&gt;这些形式.
 * java.util.Map及其子类均映射为java.util.Map(不支持EnumMap)，要求key的泛型必须是String，value的泛型必须明确指定.
 * 不支持多维数组、数组与集合类的各种组合（如Collection[]、List&lt;String[]&gt;、List&lt;List&gt;等形式）。
 * </pre>
 * 
 * @author lj
 *
 */
@Slf4j
@Order(ChainOrder.VERY_LOWER)
@Component
public class ServiceClassAdvisor implements ClassAdvisor {
	/**
	 * 通过方法转换数据
	 */
	public static final String METHOD_PREFIX = "method:";

	@Resource
	private PrototypeConfig config;// 配置

	@Resource
	private BusinessExecutor executor;// 业务执行接口
	@Resource
	private ComponentContainer container;// 组件窗口

	@Resource
	private ServiceNameGenerator generator;// 服务名生成器

	private Set<String> classes = new HashSet<>();// 需要生成参数和结果类的业务类

	@Override
	public void beforeLoad(ClassBuilder builder, Errors errors) {
		ServiceDefine define = builder.getAnnotation(ServiceDefine.class);
		if (define == null) {
			return;
		}
		ConstructorBuilder[] cbs = builder.findConstructors();
		if (cbs.length != 1) {
			errors.add("service.constructor", builder.toString());
			return;
		}
		classes.add(builder.getName());
	}

	@Override
	public void onComplete(ClassFactory factory, Errors errors) {
		List<Service> services = new ArrayList<>();
		Map<Class<?>, Service> baseTypes = new HashMap<>();
		for (String name : classes) {
			Service service = registerService(baseTypes, factory, name, errors);
			if (service.getDefine().open()) {
				services.add(service);
			}
		}
		for (ApiCreator<?> creator : container.getComponents(ApiCreator.class)) {
			List<Service> list = filterServices(creator.getType(), services);
			if (!list.isEmpty()) {
				creator.createForAll(list);
			}
		}
		classes.clear();
	}

	private List<Service> filterServices(String type, List<Service> services) {
		PrototypeConfig.Api api = config.getApi().get(type);
		if (api == null) {
			return services;
		}
		List<Service> result = new ArrayList<>();
		if (!api.getEnable()) {
			return result;
		}
		Observable.from(services.toArray(new Service[services.size()])).filter(new ServiceFilter(api))
				.subscribe(new Action1<Service>() {

					@Override
					public void call(Service service) {
						result.add(service);
					}
				});
		return result;
	}

	/**
	 * 服务过滤器. <br>
	 * 如果服务对应业务类，被配置排除，则忽略之.
	 * @author lj
	 *
	 */
	private class ServiceFilter implements Func1<Service, Boolean> {

		private Set<String> includePackages;
		private Set<String> excludePackages;
		private Set<String> includeClasses;
		private Set<String> excludeClasses;

		/**
		 * 构造
		 * @param api API配置
		 */
		public ServiceFilter(PrototypeConfig.Api api) {
			includePackages=getSet(api.getIncludePackages().trim());
			excludePackages=getSet(api.getExcludePackages().trim());
			includeClasses=getSet(api.getIncludeClasses().trim());
			excludeClasses=getSet(api.getExcludeClasses().trim());
		}
		
		/**
		 * 分析字符串为集合
		 * @param str 字符串
		 * @return 拆分后的字符串集合
		 */
		private Set<String> getSet(String str){
			Set<String> set=new HashSet<>();
			if(str.length()==0){
				return set;
			}
			for(String s:str.split("[,;]")){
				s=s.trim();
				if(s.length()>0){
					set.add(s);
				}
			}
			return set;
		}

		@Override
		public Boolean call(Service service) {
			String name=service.getType().getName();
			int k=name.lastIndexOf('.');
			String packageName=k==-1?"":name.substring(0, k);
			if(excludeClasses.contains(name)){//排除的类
				return false;
			}
			if(includeClasses.contains(name)){//包含的类
				return true;
			}
			if(!excludePackages.isEmpty()&&isInPackage(excludePackages, packageName)){//排除的包
				return false;
			}
			return includePackages.isEmpty()||isInPackage(includePackages, packageName);//包含的包
		}
		
		/**
		 * 判断指定的包保是否在集合中
		 * @param packages 集合
		 * @param packageName 包名
		 * @return 是否包含指定的包
		 */
		private boolean isInPackage(Set<String> packages,String packageName){
			for(String pkg:packages){
				if(packageName.equals(pkg)||packageName.startsWith(pkg+".")){
					return true;
				}
			}
			return false;
		}

	}

	private Service registerService(Map<Class<?>, Service> baseTypes, ClassFactory factory, String name,
			Errors errors) {
		Class<?> clazz = factory.loadClass(name);
		Service service = new Service();
		service.setDefine(clazz.getAnnotation(ServiceDefine.class));
		service.setType(clazz);
		setBaseType(service, baseTypes, clazz, errors);
		service.setParamType(createClass(factory, clazz, IteratorBuilder.getProperties(clazz, true), true, errors));
		service.setResultType(createClass(factory, clazz, IteratorBuilder.getProperties(clazz, false), false, errors));
		executor.registerService(service);
		return service;
	}

	private void setBaseType(Service service, Map<Class<?>, Service> baseTypes, Class<?> clazz, Errors errors) {
		Class<?> type = clazz;
		BusinessDefine define = null;
		while ((define = type.getDeclaredAnnotation(BusinessDefine.class)) == null) {
			type = type.getSuperclass();
		}
		Service firstService = baseTypes.get(type);
		if (firstService == null) {
			baseTypes.put(type, service);
			service.setBaseType(type);
			if (Modifier.isAbstract(type.getModifiers())) {
				errors.add("service.basetype.modifier", type.getName());
			}
			try {
				service.setConstructor(type.getConstructor());
				if (!Modifier.isPublic(service.getConstructor().getModifiers())) {
					errors.add("service.basetype.constructor", type.getName());
				}
			} catch (NoSuchMethodException | SecurityException e) {
				errors.add("service.basetype.constructor", type.getName());
			}
			Method method = MethodUtils.findMethod(type, define.setResult(), int.class);
			if (method == null) {
				errors.add("service.basetype.setresult", type.getName());
			} else {
				method.setAccessible(true);
				service.setSetResult(method);
			}
			method = MethodUtils.findMethod(type, define.addValidateError(), String.class);
			if (method == null) {
				errors.add("service.basetype.addvalidateerror", type.getName());
			} else {
				method.setAccessible(true);
				service.setAddValidateError(method);
			}
		} else {
			service.setBaseType(type);
			service.setAddValidateError(firstService.getAddValidateError());
			service.setConstructor(firstService.getConstructor());
			service.setSetResult(firstService.getSetResult());
		}
	}

	private Class<?> createClass(ClassFactory factory, Class<?> currentType, List<Property> properties, boolean isInput,
			Errors errors) {
		if (properties.size() == 0) {
			return null;
		} else {
			ServiceClassBuilder builder = new ServiceClassBuilder();
			builder.generator = generator;
			builder.factory = factory;
			builder.businessType = currentType;
			builder.errors = errors;
			builder.isInput = isInput;
			builder.properties = IteratorBuilder.getProperties(currentType, isInput);
			try {
				return builder.build();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Component
	public static class ServiceNameGenerator {

		private Map<String, Integer> sequence = new HashMap<>();

		public String getParameterClassName(Class<?> businessType) {
			return businessType.getName() + "Param";
		}

		public String getResultClassName(Class<?> businessType) {
			return businessType.getName() + "Result";
		}

		public String newClassName(Class<?> businessType, Class<?> clazz) {
			String name = clazz.getSimpleName();
			Integer value = sequence.get(name);
			if (value == null) {
				sequence.put(name, 1);
				return businessType.getName() + name;
			}
			sequence.put(name, value + 1);
			return businessType.getName() + name + value;
		}
	}

	/**
	 * 服务参数或输出类的构建器.
	 * 
	 * @author lj
	 *
	 */
	static class ServiceClassBuilder extends IteratorBuilder {

		ServiceNameGenerator generator;// 服务名生成器
		Class<?> businessType;// 业务类
		ClassFactory factory;// 类工厂
		List<Property> properties; // 输入输出属性
		Errors errors;// 错误
		boolean isInput;
		private ClassBuilder builder;// 类构建器

		private Property property;// 当前处理的属性
		private Set<Class<?>> exists = new HashSet<>();// 避免类型循环
		private boolean pojo;// 是否在生成pojo类

		/**
		 * 构建参数或输出类
		 * 
		 * @return 参数或输出类
		 * @throws Exception
		 *             异常
		 */
		public Class<?> build() throws Exception {
			String className = isInput ? generator.getParameterClassName(businessType)
					: generator.getResultClassName(businessType);
			log.debug("Create class {} for business {}", className, businessType);
			builder = factory.newClass(className,Object.class, Serializable.class);
			if (isInput) {// @JsonInclude(Include.NON_NULL)
				builder.getAnnotationBuilder(JsonInclude.class).setAttribute("value", Include.NON_NULL);
			}
			for (Property property : properties) {
				this.property = property;
				exists.clear();
				initInputOutput(property.getField(), isInput);
				Field field = property.getField();
				Object inputOutput = isInput ? field.getAnnotation(Input.class) : field.getAnnotation(Output.class);
				ServiceClassBuilder.this.build(field.getType(), this, field.getType(), builder, field, inputOutput);// 此方法反射时，最后一个参数可能是Input/Output/Prop三种
			}
			return builder.create();
		}

		/**
		 * 添加属性的输入输出注解映射数据时，加入类型判断
		 */
		@Override
		protected void addIntputOutput(InputOutput io, boolean isInput) {
			if (isInput) {
				for (Input input : io.input()) {
					if (void.class == input.type()) {// 必须指定类型
						addError("service.inputoutput");
					} else if (inputOutputs.containsKey(input.type())) {
						addError("service.input.repeat", input.type().getName());
					} else {
						inputOutputs.put(input.type(), input);
					}
				}
			} else {
				for (Output output : io.output()) {
					if (void.class == output.type()) {// 必须指定类型
						addError("service.inputoutput");
					} else if (inputOutputs.containsKey(output.type())) {
						addError("service.output.repeat", output.type().getName());
					} else {
						inputOutputs.put(output.type(), output);
					}
				}
			}
		}

		/**
		 * 构建除boolean和char之外的6种基本类型的成员变量
		 * 
		 * @param type
		 *            类型
		 * @param builder
		 *            类构建器
		 * @param field
		 *            成员变量
		 * @param inputOutput
		 *            输入输出注解
		 * @return 是否构建完成
		 * @throws Exception
		 *             异常
		 */
		protected boolean buildPrimitive(Class<?> type, ClassBuilder builder, Field field, Object inputOutput)
				throws Exception {
			return createSimpleProperty(type, builder, field, inputOutput, 2, type);
		}

		/**
		 * 构建字节数组类型的成员变量
		 * 
		 * @param type
		 *            类型
		 * @param builder
		 *            类构建器
		 * @param field
		 *            成员变量
		 * @param inputOutput
		 *            输入输出注解
		 * @return 是否构建完成
		 * @throws Exception
		 *             异常
		 */
		protected boolean buildBytes(Class<?> type, ClassBuilder builder, Field field, Object inputOutput)
				throws Exception {
			return createSimpleProperty(type, builder, field, inputOutput, 2, String.class);
		}

		/**
		 * 构建boolean和Boolean类型的成员变量
		 * 
		 * @param type
		 *            类型
		 * @param builder
		 *            类构建器
		 * @param field
		 *            成员变量
		 * @param inputOutput
		 *            输入输出注解
		 * @return 是否构建完成
		 * @throws Exception
		 *             异常
		 */
		protected boolean buildBoolean(Class<?> type, ClassBuilder builder, Field field, Object inputOutput)
				throws Exception {
			return createSimpleProperty(type, builder, field, inputOutput, 0, type);
		}

		/**
		 * 构建char和Character类型的成员变量
		 * 
		 * @param type
		 *            类型
		 * @param builder
		 *            类构建器
		 * @param field
		 *            成员变量
		 * @param inputOutput
		 *            输入输出注解
		 * @return 是否构建完成
		 * @throws Exception
		 *             异常
		 */
		protected boolean buildCharacter(Class<?> type, ClassBuilder builder, Field field, Object inputOutput)
				throws Exception {
			return createSimpleProperty(type, builder, field, inputOutput, 0, String.class);
		}

		/**
		 * 构建java.util.Date及其子类型的成员变量
		 * 
		 * @param type
		 *            类型
		 * @param builder
		 *            类构建器
		 * @param field
		 *            成员变量
		 * @param inputOutput
		 *            输入输出注解
		 * @return 是否构建完成
		 * @throws Exception
		 *             异常
		 */
		protected boolean buildDate(Class<?> type, ClassBuilder builder, Field field, Object inputOutput)
				throws Exception {
			return createSimpleProperty(type, builder, field, inputOutput, 1, Date.class);
		}

		/**
		 * 构建枚举类型的成员变量
		 * 
		 * @param type
		 *            类型
		 * @param builder
		 *            类构建器
		 * @param field
		 *            成员变量
		 * @param inputOutput
		 *            输入输出注解
		 * @return 是否构建完成
		 * @throws Exception
		 *             异常
		 */
		protected boolean buildEnum(Class<?> type, ClassBuilder builder, Field field, Object inputOutput)
				throws Exception {
			return createSimpleProperty(type, builder, field, inputOutput, 0, String.class);
		}

		/**
		 * 构建Number及其子类型的成员变量
		 * 
		 * @param type
		 *            类型
		 * @param builder
		 *            类构建器
		 * @param field
		 *            成员变量
		 * @param inputOutput
		 *            输入输出注解
		 * @return 是否构建完成
		 * @throws Exception
		 *             异常
		 */
		protected boolean buildNumber(Class<?> type, ClassBuilder builder, Field field, Object inputOutput)
				throws Exception {
			return createSimpleProperty(type, builder, field, inputOutput, 2, type);
		}

		/**
		 * 构建字符串类型的成员变量
		 * 
		 * @param type
		 *            类型
		 * @param builder
		 *            类构建器
		 * @param field
		 *            成员变量
		 * @param inputOutput
		 *            输入输出注解
		 * @return 是否构建完成
		 * @throws Exception
		 *             异常
		 */
		protected boolean buildString(Class<?> type, ClassBuilder builder, Field field, Object inputOutput)
				throws Exception {
			return createSimpleProperty(type, builder, field, inputOutput, 0, type);
		}

		/**
		 * 构建数组类型的成员变量
		 * 
		 * @param type
		 *            类型
		 * @param builder
		 *            类构建器
		 * @param field
		 *            成员变量
		 * @param inputOutput
		 *            输入输出注解
		 * @return 是否构建完成
		 * @throws Exception
		 *             异常
		 */
		protected boolean buildArray(Class<?> type, ClassBuilder builder, Field field, Object inputOutput)
				throws Exception {
			Class<?> clazz = type.getComponentType();
			clazz = createComponent(field, clazz, inputOutput);// 为数组创建组件类型
			if (clazz != null) {
				log.debug("Build {} to type : {}", field, clazz.getName() + "[]");
				Object array = Array.newInstance(clazz, 0);
				FieldBuilder fb = builder.newField(Modifier.PRIVATE, field.getName(), array.getClass(), true);
				addAnnotation(fb, clazz, true, inputOutput);
				fb.create();
				return true;
			}
			return false;
		}

		/**
		 * 添加成员变量注解（swagger说明）
		 * 
		 * @param fb
		 *            成员变量构建器
		 * @param clazz
		 *            变量类型或范型
		 * @param allowMultiple
		 *            是否允许多组数据
		 * @param inputOutput
		 *            输入或输出
		 * @throws Exception
		 *             异常
		 */
		private void addAnnotation(FieldBuilder fb, Class<?> clazz, boolean allowMultiple, Object inputOutput)
				throws Exception {
			Prop prop = inputOutput != null && Prop.class.equals(((Annotation) inputOutput).annotationType())
					? (Prop) inputOutput : null;
			Object io = pojo ? inputOutputs.get(clazz) : inputOutput;
			Input input = prop == null && isInput ? (Input) io : null;
			Output output = prop != null || isInput ? null : (Output) io;
			AnnotationBuilder ab = fb.getAnnotationBuilder(pojo ? ApiModelProperty.class : ApiParam.class);
			ab.setAttribute("value", prop != null ? prop.desc()
					: (input != null ? input.desc() : (output != null ? output.desc() : "")));
			if (prop == null) {
				prop = input != null ? input.value()[0] : (output != null ? output.value()[0] : null);
			}
			ab.setAttribute("required", prop.required());
			ab.setAttribute("allowMultiple", allowMultiple);
			ab.setAttribute("notes", getNotes(clazz, prop));
			ab.setAttribute("allowableValues", getAllowableValues(clazz, prop));
			ab.setAttribute("example", prop.hint());
		}

		/**
		 * 获取swagger注解值notes的内容
		 * 
		 * @param type
		 *            数据类型
		 * @param prop
		 *            属性注解
		 * @return 注解值allowableValues的内容
		 * @throws Exception
		 *             异常
		 */
		private String getNotes(Class<?> type, Prop prop) throws Exception {
			StringBuilder allowableValues = new StringBuilder();
			if (prop.maxLength() > 0) {// 最大长度
				allowableValues.append("length<");
				allowableValues.append(prop.maxLength());
			}
			if (prop.pattern().length() > 0 && !prop.pattern().startsWith(METHOD_PREFIX)) {// 正则表达式
				allowableValues.append(",pattern[");
				allowableValues.append(prop.pattern());
				allowableValues.append("]");
			}
			if (allowableValues.length() > 0 && allowableValues.charAt(0) == ',') {
				return allowableValues.substring(1);
			}
			return allowableValues.toString();
		}
		/**
		 * 获取swagger注解值allowableValues的内容
		 * 
		 * @param type
		 *            数据类型
		 * @param prop
		 *            属性注解
		 * @return 注解值allowableValues的内容
		 * @throws Exception
		 *             异常
		 */
		private String getAllowableValues(Class<?> type, Prop prop) throws Exception {
			StringBuilder allowableValues = new StringBuilder();
			if (Enum.class.isAssignableFrom(type)) {// 枚举
				allowableValues.append("range");
				Method method = type.getMethod("values");
				allowableValues.append(Arrays.asList((Object[]) method.invoke(null)));
			}
			return allowableValues.toString();
		}

		/**
		 * 创建组件（包括数组、集合、映射）
		 * 
		 * @param field
		 *            成员变量
		 * @param componentType
		 *            组件类型
		 * @param inputOutput
		 *            输入输出注解
		 * @return 组件类型（范型）
		 * @throws Exception
		 *             异常
		 */
		private Class<?> createComponent(Field field, Class<?> componentType, Object inputOutput) throws Exception {
			if (!isSupported(field, componentType)) {
				return null;
			}
			Class<?> clazz = componentType;
			if (ClassUtils.POJO.equals(ClassUtils.getDataType(clazz))) {
				Object io = inputOutput == null || Prop.class.isInstance(inputOutput) ? inputOutputs.get(clazz)
						: inputOutput;
				clazz = createPojo(componentType, field, inputOutput, io);
				if (!checkDesc(io)) {
					return null;
				}
			} else if (char.class == componentType || Character.class.equals(componentType)) {
				addError("service.booleanchar.type.unsupported", field.toString());
				return null;
			} else {
				Prop prop = checkProperty(clazz, inputOutput, getDataType(clazz));
				if (prop != null) {
					clazz = prop.pattern().length() > 0 ? String.class : clazz;
					if (!checkMaxLength(clazz, prop)) {
						return null;
					}
				}
				if (clazz.isEnum() || char.class == clazz || Character.class.equals(clazz)) {
					return String.class;
				} else if (Date.class.isAssignableFrom(clazz)) {
					return Date.class;
				}
			}
			return clazz;
		}

		/**
		 * 构建Collection及其子类型(不包括Set及其子类)的成员变量
		 * 
		 * @param type
		 *            类型
		 * @param builder
		 *            类构建器
		 * @param field
		 *            成员变量
		 * @param inputOutput
		 *            输入输出注解
		 * @return 是否构建完成
		 * @throws Exception
		 *             异常
		 */
		protected boolean buildList(Class<?> type, ClassBuilder builder, Field field, Object inputOutput)
				throws Exception {
			Class<?> generic = getGeneric(field, 0);
			if (generic == null) {// 必须有范型
				addError("service.collection.generic.required", field.toString());
				return false;
			}
			Class<?> clazz = createComponent(field, generic, inputOutput);
			if (clazz != null) {// 构建新类的成员变量
				log.debug("Build {} to type : java.util.List<{}>", field, clazz.getName());
				FieldBuilder fb = builder.newField(Modifier.PRIVATE, field.getName(), List.class, true);
				fb.setSignature(clazz);
				addAnnotation(fb, clazz, true, inputOutput == null ? inputOutputs.get(generic) : inputOutput);
				fb.create();
				return true;
			}
			return false;
		}

		/**
		 * 构建Set及其子类型的成员变量
		 * 
		 * @param type
		 *            类型
		 * @param builder
		 *            类构建器
		 * @param field
		 *            成员变量
		 * @param inputOutput
		 *            输入输出注解
		 * @return 是否构建完成
		 * @throws Exception
		 *             异常
		 */
		protected boolean buildSet(Class<?> type, ClassBuilder builder, Field field, Object inputOutput)
				throws Exception {
			Class<?> generic = getGeneric(field, 0);
			if (generic == null) {
				addError("service.collection.generic.required", field.toString());
				return false;
			}
			Class<?> clazz = createComponent(field, generic, inputOutput);
			if (clazz != null) {
				FieldBuilder fb = builder.newField(Modifier.PRIVATE, field.getName(), Set.class, true);
				fb.setSignature(clazz);
				addAnnotation(fb, clazz, true, inputOutput == null ? inputOutputs.get(generic) : inputOutput);
				fb.create();
				log.debug("Build {} to type : java.util.Set<{}>", field, clazz.getName());
				return true;
			}
			return false;
		}

		/**
		 * 构建Map及其子类型的成员变量
		 * 
		 * @param type
		 *            类型
		 * @param builder
		 *            类构建器
		 * @param field
		 *            成员变量
		 * @param inputOutput
		 *            输入输出注解
		 * @return 是否构建完成
		 * @throws Exception
		 *             异常
		 */
		protected boolean buildMap(Class<?> type, ClassBuilder builder, Field field, Object inputOutput)
				throws Exception {
			Class<?> first = getGeneric(field, 0);
			boolean rs = true;
			if (!String.class.equals(first)) {
				addError("service.map.generickey.required", field.toString());
				rs = false;
			}
			Class<?> generic = getGeneric(field, 1);
			if (generic == null) {
				addError("service.map.genericvalue.required", field.toString());
				rs = false;
			}
			if (rs == false) {
				return false;
			}
			Class<?> clazz = createComponent(field, generic, inputOutput);
			if (clazz != null) {
				FieldBuilder fb = builder.newField(Modifier.PRIVATE, field.getName(), Map.class, true);
				fb.setSignature(String.class, clazz);
				addAnnotation(fb, clazz, true, inputOutput == null ? inputOutputs.get(generic) : inputOutput);
				fb.create();
				log.debug("Build {} to type : java.util.Map<String,{}>", field, clazz.getName());
				return true;
			}
			return false;
		}

		/**
		 * 添加pojo类的注解
		 * 
		 * @param builder
		 *            POJO类的构建器
		 * @param inputOutput
		 *            输入或输出注解
		 */
		private void addPojoAnnotation(ClassBuilder builder, Class<?> clazz, Object inputOutput) {
			String desc = "";
			if (Prop.class.equals(((Annotation) inputOutput).annotationType())) {
				desc = ((Prop) inputOutput).desc();
			} else {
				Input input = isInput ? (Input) inputOutput : null;
				Output output = isInput ? null : (Output) inputOutput;
				desc = isInput ? input.desc() : output.desc();
			}
			if (desc.length() == 0) {
				addError("service.pojo.desc", clazz.getName(), inputOutput.toString());
				return;
			}
			AnnotationBuilder ab = builder.getAnnotationBuilder(ApiModel.class);
			ab.setAttribute("description", desc);
		}

		/**
		 * 创建pojo类
		 * 
		 * @param type
		 *            原类型
		 * @param fieldProp
		 *            原成员变量对应的注解(Input/Output/Prop)
		 * @param inputOutput
		 *            输入或输出注解
		 * @return pojo类
		 * @throws Exception
		 *             异常
		 */
		private Class<?> createPojo(Class<?> type, Field field, Object fieldProp, Object inputOutput) throws Exception {
			if (exists.contains(type)) {
				Property idProperty = ClassUtils.findIdProperty(type);
				if (idProperty == null) {
					addError("service.pojo.id.notfound", type.getName());
				} else {
					Class<?> clazz = idProperty.getType();
					if (!Number.class.isAssignableFrom(ClassUtils.getWrapperType(clazz))) {
						addError("service.pojo.id.nonnumber", type.getName(), idProperty.getName());
					} else {
						return idProperty.getType();
					}
				}
				return null;
			}
			exists.add(type);
			if (inputOutput == null) {
				addError("service.inputoutput.notfound", type.getName());
				return null;
			}
			pojo = true;
			ClassBuilder builder = factory.newClass(generator.newClassName(businessType, type));
			log.debug("Build {} to type : {}", type, builder.getName());
			addPojoAnnotation(builder, type, fieldProp == null ? inputOutput : fieldProp);
			Map<String, Property> properties = ClassUtils.properties(type);
			Prop[] props = isInput ? ((Input) inputOutput).value() : ((Output) inputOutput).value();
			for (Prop prop : props) {
				if (prop.name().length() == 0) {
					addError("service.prop.name.notfound", type.getName());
					continue;
				}
				Property property = properties.get(prop.name());
				if (property == null) {
					addError("service.property.notfound", type.getName(), prop.name());
					continue;
				}
				Field f = property.getField();
				build(f.getType(), this, f.getType(), builder, f, prop);
			}
			pojo = false;
			return builder.create();
		}

		/**
		 * 构建POJO类型的成员变量
		 * 
		 * @param type
		 *            类型
		 * @param builder
		 *            类构建器
		 * @param field
		 *            成员变量
		 * @param inputOutput
		 *            输入输出注解
		 * @return 是否构建完成
		 * @throws Exception
		 *             异常
		 */
		protected boolean buildPojo(Class<?> type, ClassBuilder builder, Field field, Object inputOutput)
				throws Exception {
			Object io = inputOutput == null || Prop.class.isInstance(inputOutput) ? inputOutputs.get(type)
					: inputOutput;
			Class<?> pojo = createPojo(type, field, inputOutput, io);
			if (pojo == null) {
				return false;
			}
			FieldBuilder fb = builder.newField(Modifier.PRIVATE, field.getName(), pojo, true);
			fb.create();
			return true;
		}

		/**
		 * 数据类型：日期、数字、其它
		 * 
		 * @param clazz
		 *            原类型
		 * @return 数据类型
		 */
		private int getDataType(Class<?> clazz) {
			String type = ClassUtils.getDataType(clazz);
			switch (type) {
			case ClassUtils.DATE:
				return 1;
			case ClassUtils.NUMBER:
			case ClassUtils.PRIMITIVE:
				return 2;
			default:
				return 0;
			}
		}

		/**
		 * 是否支持的数据类型
		 * 
		 * @param field
		 *            成员变量
		 * @param componentType
		 *            组件类型
		 * @return 不支持组件为数组、集合或映射
		 */
		private boolean isSupported(Field field, Class<?> componentType) {
			if (componentType.isArray() || Collection.class.isAssignableFrom(componentType)
					|| Map.class.isAssignableFrom(componentType)) {
				addError("service.field.type", field.toString());
				return false;
			}
			return true;
		}

		/**
		 * 检查属性（Prop注解的数量、pattern属性及desc属性）
		 * 
		 * @param type
		 *            对象类型
		 * @param inputOutput
		 *            输入或输出注解或Prop注解
		 * @param dataType
		 *            数据类型：日期、数字、其它
		 * @return 唯一 的Prop注解
		 */
		private Prop checkProperty(Class<?> type, Object inputOutput, int dataType) {
			Prop prop = null;
			if (Prop.class.isInstance(inputOutput)) {
				prop = (Prop) inputOutput;
			} else {
				Prop[] props = isInput ? ((Input) inputOutput).value() : ((Output) inputOutput).value();
				if (props.length != 1) {
					addError("service.inputoutput.error", inputOutput.toString());
					return null;
				}
				prop = props[0];
			}
			return checkPattern(type, prop.pattern(), dataType) & checkDesc(prop) ? prop : null;
		}

		/**
		 * 创建简单属性
		 * 
		 * @param type
		 *            属性类型或泛型
		 * @param builder
		 *            类构建器
		 * @param field
		 *            原成员变量
		 * @param inputOutput
		 *            输入或输出注解或Prop注解
		 * @param dataType
		 *            数据类型：日期、数字、其它
		 * @param fieldType
		 *            成员变量类型
		 * @return 是否构建成功
		 * @throws Exception
		 *             异常
		 */
		private boolean createSimpleProperty(Class<?> type, ClassBuilder builder, Field field, Object inputOutput,
				int dataType, Class<?> fieldType) throws Exception {
			Prop prop = null;
			if (Prop.class.isInstance(inputOutput)) {
				prop = (Prop) inputOutput;
			} else {
				Prop[] props = isInput ? ((Input) inputOutput).value() : ((Output) inputOutput).value();
				if (props.length != 1) {
					addError("service.inputoutput.error", inputOutput.toString());
					return false;
				}
				prop = props[0];
				prop = checkPattern(type, prop.pattern(), dataType) & checkDesc(prop) ? prop : null;
			}
			return prop == null ? false : createProperty(type, builder, field, prop, fieldType);
		}

		/**
		 * 创建属性对应的成员变量
		 * 
		 * @param type
		 *            属性类型或泛型
		 * @param builder
		 *            类构建器
		 * @param field
		 *            原成员变量
		 * @param prop
		 *            对应的属性注解
		 * @param fieldType
		 *            成员变量类型
		 * @return 是否创建成功
		 * @throws Exception
		 *             异常
		 */
		private boolean createProperty(Class<?> type, ClassBuilder builder, Field field, Prop prop, Class<?> fieldType)
				throws Exception {
			Class<?> clazz = prop.pattern().length() > 0 ? String.class : fieldType;
			if (clazz.isPrimitive() && !prop.required()) {
				addError("service.primitive.required", field.toString());
			}
			if (checkMaxLength(clazz, prop)) {
				FieldBuilder fb = builder.newField(Modifier.PRIVATE, field.getName(), clazz, true);
				addAnnotation(fb, clazz, false, prop);
				fb.create();
				log.debug("Build {} to type : {}", field, clazz.getName());
				return true;
			}
			return false;
		}

		/**
		 * 检查注解的desc值
		 * 
		 * @param inputOutput
		 *            输入或输出注解
		 * @return 符合要求返回true
		 */
		private boolean checkDesc(Object inputOutput) {
			if (isInput) {
				Input input = (Input) inputOutput;
				if (input.desc().length() == 0) {
					addError("service.prop.desc", input.toString());
					return false;
				}
			} else {
				Output output = (Output) inputOutput;
				if (output.desc().length() == 0) {
					addError("service.prop.desc", output.toString());
					return false;
				}
			}
			return true;
		}

		/**
		 * 检查注解的desc属性
		 * 
		 * @param prop
		 *            属性注解
		 * @return 符合要求返回true
		 */
		private boolean checkDesc(Prop prop) {
			if (prop.desc().length() == 0) {
				addError("service.prop.desc", prop.toString());
				return false;
			}
			return true;
		}

		/**
		 * 检查属性注解的最大长度值
		 * 
		 * @param type
		 *            数据类型
		 * @param prop
		 *            属性注解
		 * @return 符合要求返回true
		 */
		private boolean checkMaxLength(Class<?> type, Prop prop) {
			if (isInput && String.class.equals(type) && prop.maxLength() == -1) {
				addError("service.prop.maxLength", prop.toString());
				return false;
			}
			return true;
		}

		/**
		 * 检查注解上的pattern属性值
		 * 
		 * @param type
		 *            数据类型或范型
		 * @param pattern
		 *            注解上的pattern属性值
		 * @param dataType
		 *            数据类型：日期、数字、其它
		 * @return 符合要求返回true
		 */
		private boolean checkPattern(Class<?> type, String pattern, int dateType) {
			if (pattern.length() == 0) {
				return true;
			}
			if (pattern.startsWith(METHOD_PREFIX)) {
				return existsMethod(pattern.substring(METHOD_PREFIX.length()), type);
			}
			try {
				switch (dateType) {
				case 1:
					getDateFormat(pattern);
					return true;
				case 2:
					getDecimalFormat(pattern);
					return true;
				default:
					addError("service.pattern.unsupported", pattern);
					return false;
				}
			} catch (Exception e) {
				addError("service.pattern.error", pattern);
				return false;
			}
		}

		/**
		 * 检查pattern属性注解对应的方法是否存在
		 * 
		 * @param methodName
		 *            方法名
		 * @param type
		 *            数据类型
		 * @return 存在方法则返回true
		 */
		private boolean existsMethod(String methodName, Class<?> type) {
			Method method = MethodUtils.findMethod(businessType, methodName, isInput ? String.class : type);
			if (method == null || method.getReturnType() != (isInput ? type : String.class)) {
				String key = isInput ? "service.prop.pattern.input.method" : "service.prop.pattern.output.method";
				addError(key, (isInput ? type : String.class).getName(), methodName,
						(isInput ? String.class : type).getName());
				return false;
			}
			return true;
		}

		/**
		 * 添加错误
		 * 
		 * @param key
		 *            错误关键字
		 * @param msgs
		 *            消息内容参数
		 */
		private void addError(String key, String... msgs) {
			String[] args = new String[msgs.length + 2];
			args[0] = businessType.getName();
			args[1] = property.getField().getName();
			System.arraycopy(msgs, 0, args, 2, msgs.length);
			errors.add(key, args);
		}

	}

}
