package org.prototype.web;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.prototype.PrototypeConfig;
import org.prototype.PrototypeConfig.Api;
import org.prototype.PrototypeInitializer;
import org.prototype.business.ApiCreator;
import org.prototype.business.BusinessExecutor;
import org.prototype.business.Service;
import org.prototype.business.ServiceDefine;
import org.prototype.business.View;
import org.prototype.core.ClassBuilder;
import org.prototype.core.ClassScaner;
import org.prototype.core.ComponentContainer;
import org.prototype.core.MethodBuilder;
import org.prototype.core.MethodChain;
import org.prototype.core.MethodFilter;
import org.prototype.core.ParameterAnnotationsBuilder;
import org.prototype.core.ParameterInject;
import org.prototype.core.PrototypeStatus;
import org.prototype.reflect.AnnotationUtils;
import org.prototype.reflect.CacheUtils;
import org.prototype.reflect.CacheUtils.Cache;
import org.prototype.reflect.ClassUtils;
import org.prototype.reflect.Property;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 控制器服务创建实现. <br>
 * 
 * @author lj
 *
 */
@Slf4j
@Component
public class ControllerServiceCreator implements ApiCreator<Class<?>>, BeanFactoryAware {

	/**
	 * 接口类型值
	 */
	public static final String TYPE = "springmvc";

	@Resource
	private PrototypeConfig config;

	@Resource
	private PrototypeInitializer initializer;

	@Resource
	private BusinessExecutor executor;

	private DefaultListableBeanFactory registry;

	@Resource
	private ClassScaner scaner;

	@Resource
	private ControllerNameGenerator namgeGenerator;

	@Resource
	private ComponentContainer container;

	@Resource
	private ObjectMapper mapper;

	private boolean async;

	private List<String> apiTypes = new ArrayList<>();

	private Class<? extends Annotation> swaggerApi;

	@SuppressWarnings("unchecked")
	@PostConstruct
	void init() {
		async = initializer.getBootClass().getAnnotation(EnableAsync.class) != null;
		if (async) {
			log.info("Open Servlet 3 async request");
		}
		if (config.getApi().get(TYPE) == null) {
			PrototypeConfig.Api api = new PrototypeConfig.Api();
			api.setEnable(true);
			config.getApi().put(TYPE, api);
		}
		for (ApiCreator<?> creator : container.getComponents(ApiCreator.class)) {
			Api api = config.getApi().get(creator.getType());
			if (api != null && Boolean.TRUE.equals(api.getEnable())) {
				apiTypes.add(creator.getType());
			}
		}
		try {
			swaggerApi = (Class<? extends Annotation>) Thread.currentThread().getContextClassLoader()
					.loadClass("io.swagger.annotations.ApiOperation");
		} catch (ClassNotFoundException e) {
			log.info("Swagger UI disabled");
		}
	}

	/**
	 * 返回{@link #TYPE}作为接口类型值
	 */
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void createForAll(Collection<Service> services) {
		Map<String, List<Service>> map = Service.group(services);
		for (Map.Entry<String, List<Service>> entry : map.entrySet()) {
			Class<?> controller = create(entry.getKey(), entry.getValue());
			BeanDefinitionBuilder define = BeanDefinitionBuilder.genericBeanDefinition(controller);
			registry.registerBeanDefinition(controller.getName(), define.getRawBeanDefinition());
		}
	}

	private Class<?> create(String packageName, Collection<Service> services) {
		ClassBuilder builder = scaner.getClassFactory().newClass(namgeGenerator.newControllerName(packageName));
		builder.getAnnotationBuilder(Controller.class);
		Map<String, List<Service>> map = executor.groupByUrl(services);
		for (Map.Entry<String, List<Service>> entry : map.entrySet()) {
			ControllerMethodBuilder mb = new ControllerMethodBuilder();
			mb.builder = builder;
			mb.url = entry.getKey();
			mb.services = entry.getValue();
			mb.build();
		}
		return builder.create();
	}

	/**
	 * 方法参数
	 * 
	 * @author lj
	 *
	 */
	class MethodParameter {
		private Class<?> type;
		private Annotation[] annotations;
		private String name;
	}

	/**
	 * api生成
	 * 
	 * @author lj
	 *
	 */
	@SuppressWarnings("rawtypes")
	class ApiMethodAdvisor implements MethodFilter {
		private String businessName;

		public ApiMethodAdvisor(String businessName) {
			this.businessName = businessName;
		}

		@Override
		public Object doFilter(Object[] args, MethodChain chain) throws Exception {
			String type = (String) args[0];
			if (!apiTypes.contains(type)) {
				return "UnsupportedOperation";
			}
			HttpServletResponse response = (HttpServletResponse) args[1];
			String contentType = config.getApi().get(type).getContentType();
			if (contentType != null) {
				response.setContentType(contentType + "; charset=UTF-8");
			}
			String key = businessName + "_" + type;
			Cache cache = CacheUtils.getCache(key);
			String value = cache.get(key, String.class);
			if (value == null) {
				Object object = executor.getApi(type, businessName);
				value = mapper.writeValueAsString(object);
				cache.put(key, value);
			}
			response.getWriter().write(value);
			return null;
		}

	}

	/**
	 * 版本路由
	 * 
	 * @author lj
	 *
	 */
	@SuppressWarnings("rawtypes")
	class RouteMethodAdvisor implements MethodFilter {

		@Override
		public Object doFilter(Object[] args, MethodChain chain) throws Exception {
			HttpServletRequest request = (HttpServletRequest) args[0];
			String version = request.getHeader(config.getVersionParameterName());
			if (version == null) {// 获取版本号
				version = request.getParameter(config.getVersionParameterName());
			}
			RequestMapping route = chain.getMethod().getAnnotation(RequestMapping.class);
			String lastVersion = executor.findService(route.value()[0], version).getDefine().version().replace('.',
					'_');// 获取API最近的版本号
			String uri = request.getRequestURI();
			int k = uri.indexOf('.');
			uri = k == -1 ? (uri + '/' + lastVersion) : (uri.substring(0, k) + '/' + lastVersion + uri.substring(k));
			request.getRequestDispatcher(uri).forward(request, (ServletResponse) args[1]);// 重定向
			return null;
		}

	}

	/**
	 * 控制器方法执行
	 * 
	 * @author lj
	 *
	 */
	@SuppressWarnings("rawtypes")
	class ControllerMethodAdvisor implements MethodFilter {

		private Service service;
		private Property view;
		private boolean eventSource;

		public ControllerMethodAdvisor(Service service, Property view, boolean eventSource) {
			this.service = service;
			this.view = view;
			this.eventSource = eventSource;
		}

		@Override
		public Object doFilter(Object[] args, MethodChain chain) throws Exception {
			if (async) {
				DeferredResult<Object> deferredResult = new DeferredResult<>();
				executor.submit(service.getType(), args).whenCompleteAsync((result, throwable) -> {
					deferredResult.setResult(execute(args));
				});
				return deferredResult;
			}
			return execute(args);
		}

		private Object execute(Object[] args) {
			HttpPrototypeStatus status = new HttpPrototypeStatus();
			HttpPrototypeStatus.setStatus(status);
			try {
				Object rs = executor.execute(service.getType(), args);
				if (view != null) {
					return getView(status, rs);
				} else if (eventSource) {
					return getEventSourceResult(rs);
				}
				return rs;
			} finally {
				if (log.isDebugEnabled()) {
					try {
						String json = mapper
								.writeValueAsString(new RequestResponse(PrototypeStatus.getStatus().getResult()));
						log.debug("Http request and response : \r\n{}", json);
					} catch (JsonProcessingException e) {
						throw new RuntimeException("JSON error", e);
					}
				}
				HttpPrototypeStatus.setStatus(null);
			}
		}

		/**
		 * 获取事件源结果
		 * 
		 * @param result
		 *            结果
		 * @return EventSource的返回结果
		 */
		private String getEventSourceResult(Object result) {
			try {
				return "data:" + mapper.writeValueAsString(result) + "\n\n";
			} catch (JsonProcessingException e) {
				throw new RuntimeException("Json error", e);
			}
		}

		/**
		 * 获取视图
		 * 
		 * @param request
		 * @param chain
		 * @param result
		 * @return
		 */
		private ModelAndView getView(HttpPrototypeStatus status, Object result) {
			try {
				Map<String, Object> map = new HashMap<>();
				for (Map.Entry<String, Property> entry : ClassUtils.properties(result.getClass()).entrySet()) {
					Property property = entry.getValue();
					Object value = property.getValue(result);
					if (value != null) {
						map.put(entry.getKey(), value);
					}
				}
				return new ModelAndView((String) view.getValue(status.getTarget()), map);
			} catch (Exception e) {
				throw new RuntimeException("Prototype error", e);
			}
		}
	}

	/**
	 * 控制器类名及方法名生成
	 * 
	 * @author lj
	 *
	 */
	@Component
	public static class ControllerNameGenerator {

		private Map<String, Integer> values = new HashMap<>();

		public String newControllerName(String packageName) {
			int k = packageName.lastIndexOf('.');
			String prefix = k == -1 ? ""
					: (packageName.substring(k + 1, k + 2).toUpperCase() + packageName.substring(k + 2));
			String className = packageName + "." + prefix + "Controller";
			int seq = 0;
			while (true) {
				try {
					Thread.currentThread().getContextClassLoader().loadClass(className);
					className = packageName + (seq++);
				} catch (ClassNotFoundException e) {
					return className;
				}
			}
		}

		public String newMethodName(Service service) {
			String methodName = service.getType().getSimpleName();
			methodName = methodName.substring(0, 1).toLowerCase() + methodName.substring(1);
			Integer value = values.get(methodName);
			if (value == null) {
				values.put(methodName, 1);
			} else {
				values.put(methodName, value + 1);
				methodName += value;
			}
			return methodName;
		}
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		registry = (DefaultListableBeanFactory) beanFactory;
	}

	@Override
	public Class<?> create(Service service) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSupportSingle() {
		return false;
	}

	class ControllerMethodBuilder {
		private String url;
		private List<Service> services;
		private ClassBuilder builder;

		private String name;

		public void build() {
			Service service = services.get(0);
			name = namgeGenerator.newMethodName(service);
			if (services.size() == 1) {
				buildApi(service, null);
				buildMethod(service, null);
			} else {
				buildRoute(service);
				for (Service ser : services) {
					buildApi(ser, ser.getDefine().version());
					buildMethod(ser, ser.getDefine().version());
				}
			}
		}

		@SuppressWarnings("unchecked")
		private void buildRoute(Service service) {
			Set<RequestMethod> methods = new HashSet<>();
			for (Service ser : services) {
				RequestMapping mapping = findRequestMapping(ser.getType());
				methods.addAll(Arrays.asList(mapping.method()));
			}
			Class<? extends Throwable>[] throwableTypes = (Class<? extends Throwable>[]) new Class<?>[] {};
			Class<?>[] parameterTypes = new Class<?>[] { HttpServletRequest.class, HttpServletResponse.class };
			MethodBuilder mb = builder.newMethod(Modifier.PUBLIC, void.class, name, parameterTypes, throwableTypes,
					new RouteMethodAdvisor());
			mb.getAnnotationBuilder(RequestMapping.class).setAttribute("value", new String[] { url })
					.setAttribute("method", methods.toArray(new RequestMethod[methods.size()]));
			ServiceDefine define = service.getDefine();
			if (swaggerApi != null) {
				mb.getAnnotationBuilder(swaggerApi).setAttribute("value", "Version router : " + define.value())
						.setAttribute("notes", define.hint());
			}
			mb.create();
		}

		@SuppressWarnings("unchecked")
		private void buildApi(Service service, String version) {
			Class<? extends Throwable>[] throwableTypes = (Class<? extends Throwable>[]) new Class<?>[] {};
			Class<?>[] parameterTypes = new Class<?>[] { String.class, HttpServletResponse.class };
			String url = service.getDefine().url();
			String methodName = name;
			if (version != null) {
				url = url + "/" + version.replace('.', '_');
				methodName = name + version.replace('.', '_');
			}
			MethodBuilder mb = builder.newMethod(Modifier.PUBLIC, void.class, methodName, parameterTypes,
					throwableTypes, new ApiMethodAdvisor(service.getType().getName()));
			mb.getAnnotationBuilder(RequestMapping.class).setAttribute("value", new String[] { url })
					.setAttribute("method", new RequestMethod[] { RequestMethod.GET })
					.setAttribute("params", new String[] { config.getApiParameterName() });
			mb.getParameterAnnotationsBuilder().getAnnotationBuilder(0, RequestParam.class)[0].setAttribute("name",
					config.getApiParameterName());
			ServiceDefine define = service.getDefine();
			if (swaggerApi != null) {
				mb.getAnnotationBuilder(swaggerApi).setAttribute("value", "API : " + define.value())
						.setAttribute("hidden", true);
			}
			mb.create();
		}

		@SuppressWarnings("unchecked")
		private void buildMethod(Service service, String version) {
			Class<? extends Throwable>[] throwableTypes = (Class<? extends Throwable>[]) new Class<?>[] {};
			boolean requestBody = hasPojoProperty(service.getParamType(), true);
			MethodParameter[] mps = getParameters(service, requestBody);
			Class<?>[] parameterTypes = getParameterTypes(mps);
			List<Property> views = ClassUtils.findProperty(service.getType(), View.class);
			Property view = views.isEmpty() ? null : views.get(0);
			RequestMapping requestMapping = findRequestMapping(service.getType());
			boolean eventSource = Arrays.asList(requestMapping.produces()).indexOf("text/event-stream") != -1;
			MethodBuilder mb = null;
			String methodName = name + (version == null ? "" : version.replace('.', '_'));
			if (async) {
				mb = builder.newMethod(Modifier.PUBLIC, DeferredResult.class, methodName, parameterTypes,
						throwableTypes, new ControllerMethodAdvisor(service, view, eventSource));
			} else {
				Class<?> type = view == null ? service.getResultType() : ModelAndView.class;
				if (eventSource) {
					type = String.class;
				}
				mb = builder.newMethod(Modifier.PUBLIC, type, methodName, parameterTypes, throwableTypes,
						new ControllerMethodAdvisor(service, view, eventSource));
			}
			buildMethodAnnotations(mb, requestMapping, service, view != null, eventSource, version);
			buildParametersAnnotations(mb, mps, requestBody);
			mb.create();
		}

		@SuppressWarnings("unchecked")
		private void buildParametersAnnotations(MethodBuilder mb, MethodParameter[] mps, boolean requestBody) {
			ParameterAnnotationsBuilder builder = mb.getParameterAnnotationsBuilder();
			for (int i = 0, k = mps.length; i < k; i++) {
				builder.copyAnnotations(i, mps[i].annotations);
				if (mps[i].name != null) {
					builder.getAnnotationBuilder(i, RequestParam.class)[0].setAttribute("name", mps[i].name)
							.setAttribute("required", false);
				}
			}
			if (requestBody) {
				mb.getParameterAnnotationsBuilder().getAnnotationBuilder(mps.length - 1, RequestBody.class);
			}
		}

		private Class<?>[] getParameterTypes(MethodParameter[] mps) {
			Class<?>[] classes = new Class<?>[mps.length];
			for (int i = 0, k = mps.length; i < k; i++) {
				classes[i] = mps[i].type;
			}
			return classes;
		}

		private void buildMethodAnnotations(MethodBuilder mb, RequestMapping mapping, Service service, boolean hasView,
				boolean eventSource, String version) {
			mb.copyAnnotations(getAnnotationsExceptMapping(service));
			mb.getAnnotationBuilder(RequestMapping.class).setAttribute("value", getUrl(mapping, version))
					.setAttribute("name", mapping.name()).setAttribute("method", mapping.method())
					.setAttribute("params", mapping.params()).setAttribute("headers", mapping.headers())
					.setAttribute("consumes", mapping.consumes()).setAttribute("produces", mapping.produces());
			ServiceDefine define = service.getDefine();
			if (swaggerApi != null) {
				mb.getAnnotationBuilder(swaggerApi).setAttribute("value", define.value())
						.setAttribute("notes", define.hint()).setAttribute("httpMethod", mapping.method()[0].name())
						.setAttribute("response", (hasView || eventSource) ? String.class : service.getResultType())
						.setAttribute("produces", getArrayValue(mapping.produces()))
						.setAttribute("consumes", getArrayValue(mapping.consumes()));
			}
			if (!hasView) {
				mb.getAnnotationBuilder(ResponseBody.class);
			}
		}

		private Object getUrl(RequestMapping mapping, String version) {
			String[] rs = new String[mapping.value().length + mapping.path().length];
			System.arraycopy(mapping.value(), 0, rs, 0, mapping.value().length);
			System.arraycopy(mapping.path(), 0, rs, mapping.value().length, mapping.path().length);
			if (version != null) {
				String ver = version.replace('.', '_');
				rs[0] = rs[0] + '/' + ver;
			}
			return rs;
		}

		private String getArrayValue(String[] values) {
			return values.length == 0 ? "" : values[0];
		}

		/**
		 * 获取控制器方法参数
		 * 
		 * @param service
		 *            服务
		 * @return 控制器方法参数
		 */
		private MethodParameter[] getParameters(Service service, boolean requestBody) {
			List<MethodParameter> params = new ArrayList<>();
			for (Parameter parameter : service.getType().getConstructors()[0].getParameters()) {
				Annotation[] annotations = AnnotationUtils.getAnnotationByMeta(parameter.getAnnotations(),
						ParameterInject.class);
				if (annotations.length == 0) {
					MethodParameter mp = new MethodParameter();
					mp.type = parameter.getType();
					mp.annotations = parameter.getAnnotations();
					params.add(mp);
				}
			}
			if (service.getParamType() != null) {
				if (requestBody) {
					MethodParameter mp = new MethodParameter();
					mp.type = service.getParamType();
					params.add(mp);
				} else if (service.getParamType() != null) {
					addParameters(params, service.getParamType(), "");
				}
			}
			return params.toArray(new MethodParameter[params.size()]);
		}

		private void addParameters(List<MethodParameter> params, Class<?> type, String prefix) {
			Collection<Property> properties = ClassUtils.properties(type).values();
			if (properties.size() == 1 && prefix.length() == 0) {
				Property property = properties.iterator().next();
				if (hasPojoProperty(property.getType(), true)) {// TODO
					addParameters(params, property.getType(), property.getName());
					return;
				}
			}
			for (Property property : properties) {
				MethodParameter mp = new MethodParameter();
				mp.type = property.getType();
				mp.annotations = property.getField().getAnnotations();
				mp.name = (prefix.length() == 0 ? "" : (prefix + ".")) + property.getName();
				params.add(mp);
			}
		}

		/**
		 * 是否数组、集合、映射与POJO
		 * 
		 * @param type
		 *            数据类型
		 * @return 是则返回true
		 */
		private boolean isComponent(Property property) {
			String type = ClassUtils.getDataType(property.getType());
			switch (type) {
			case ClassUtils.POJO:
			case ClassUtils.LIST:
			case ClassUtils.ARRAY:
			case ClassUtils.SET:
			case ClassUtils.MAP:
				return true;
			default:
				return false;
			}
		}

		private boolean hasPojoProperty(Class<?> type, boolean paramType) {
			if (type == null) {
				return false;
			}
			Collection<Property> properties = ClassUtils.properties(type).values();
			if (properties.size() == 1 && paramType) {
				return hasPojoProperty(type, false);
			}
			boolean component = false;
			for (Property property : properties) {
				if (isComponent(property)) {

				}
			}
			return false;
		}

		private RequestMapping findRequestMapping(Class<?> type) {
			RequestMapping mapping = type.getAnnotation(RequestMapping.class);
			if (mapping == null) {
				return type.getConstructors()[0].getAnnotation(RequestMapping.class);
			}
			return mapping;
		}

		private Annotation[] getAnnotationsExceptMapping(Service service) {
			List<Annotation> annotations = new ArrayList<>();
			for (Annotation annotation : service.getType().getAnnotations()) {
				if (isMethodAnnotation(annotation)) {
					annotations.add(annotation);
				}
			}
			for (Annotation annotation : service.getConstructor().getAnnotations()) {
				if (isMethodAnnotation(annotation)) {
					annotations.add(annotation);
				}
			}
			return annotations.toArray(new Annotation[annotations.size()]);
		}

		/**
		 * 是否方法注解(除了RequestMapping注解)
		 * 
		 * @param annotation
		 *            注解
		 * @return 是则返回true
		 */
		private boolean isMethodAnnotation(Annotation annotation) {
			Class<?> type = annotation.annotationType();
			if (!RequestMapping.class.equals(type)) {
				Target target = type.getAnnotation(Target.class);
				return Arrays.asList(target.value()).indexOf(ElementType.METHOD) != -1;
			}
			return false;
		}
	}
}
