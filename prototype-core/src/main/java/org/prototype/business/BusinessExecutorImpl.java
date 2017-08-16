package org.prototype.business;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.prototype.PrototypeInitializer;
import org.prototype.annotation.Message;
import org.prototype.core.AnnotationsChainComparator;
import org.prototype.core.ClassFactory;
import org.prototype.core.ClassScaner;
import org.prototype.core.ComponentContainer;
import org.prototype.core.PrototypeStatus;
import org.prototype.reflect.AnnotationUtils;
import org.prototype.reflect.MethodUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 业务执行接口实现类. <br>
 * 
 * 提供每个ExecuteFilter的性能日志：需要指定Spring环境变量"logging.level."+业务类名=debug。如业务类为org.prototype.Foo，则可在application.properties中指定logging.level.org.prototype.Foo=deubg
 * 
 * @author lj
 *
 */
@Slf4j
@Component
public class BusinessExecutorImpl implements BusinessExecutor {

	@Resource
	private PrototypeInitializer initializer;// 初始化

	@Autowired(required = false)
	private java.util.concurrent.Executor executor;// 线程池

	@Resource
	private MessageSource messageSource;// 消息资源

	@Resource
	private ClassScaner scaner;// 类执行

	@Resource
	private ComponentContainer container;// 组件容器

	@Resource
	private GetResultExecuteFilter getResultExecuteFilter;// 获取结果过滤器

	private Map<Class<?>, Annotation> globalAnnotations = new HashMap<>();// 全局注解（启动类）被Executor注解过的注解类型与注解的映射

	private Map<Class<?>, Service> services = new HashMap<>();// 所有的服务

	private Map<Class<Annotation>, ExecuteFilter<?>> filters = new HashMap<>();// 注解与对应执行过滤器

	private List<ExecuteFilter<?>> defaultFilters = new ArrayList<>();// 默认的执行过滤器（不需要注解的）

	private Map<String, ApiCreator<?>> creators = new HashMap<>();// API创建接口（类型与实现的映射）

	@PostConstruct
	void init() {
		initFilters();
		Class<?> clazz = initializer.getBootClass();// 获取启动类
		if (clazz != null) {
			Annotation[] annotations = AnnotationUtils.getAnnotationByMeta(clazz.getAnnotations(), Executor.class);
			for (Annotation annotation : annotations) {
				globalAnnotations.put(annotation.annotationType(), annotation);// 全局注解
			}
		}
		if (executor == null) {
			executor = Executors.newCachedThreadPool();// 线程池
			log.warn("No bean '{}' found , use {}", java.util.concurrent.Executor.class.getName(),
					"Executors.newCachedThreadPool()");
		}
		for (ApiCreator<?> creator : container.getComponents(ApiCreator.class)) {
			creators.put(creator.getType(), creator);// API创建接口
		}
	}

	/**
	 * 初始化默认的执行过滤器及注解处理的执行过滤器
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initFilters() {
		List<ExecuteFilter> list = container.getComponents(ExecuteFilter.class);
		for (ExecuteFilter filter : list) {
			Class<?> clazz = ResolvableType.forClass(AopUtils.getTargetClass(filter)).as(ExecuteFilter.class)
					.getGeneric(0).resolve();
			if (clazz == null || Annotation.class.equals(clazz)) {
				defaultFilters.add(filter);
			} else {
				filters.put((Class<Annotation>) clazz, filter);
			}
		}
	}

	/**
	 * 异步提交数据
	 */
	@Override
	public CompletableFuture<Object> submit(Class<?> type, Object[] params) {
		return CompletableFuture.supplyAsync(new Supplier<Object>() {

			@Override
			public Object get() {
				return executeInner(true, type, params);
			}

		}, executor);
	}

	@Override
	public Object execute(Class<?> type, Object[] params) {
		return executeInner(false, type, params);
	}

	/**
	 * 执行业务
	 * 
	 * @param async
	 * @param type
	 * @param params
	 * @return
	 */
	private Object executeInner(boolean async, Class<?> type, Object[] params) {
		ExecuteChain chain = new ExecuteChainImpl(async, type, params, findFilters(type));
		boolean create = initStatus();
		try {
			chain.doChain();
			Object rs = chain.getResult();
			return rs == null ? getResult(chain, ExecuteChain.ERROR) : rs;
		} catch (Exception e) {
			Message.getSubject().onNext(new Message(Message.EXCEPTION, getClass().getName(), e));
			log.warn("Execute " + type + " error", e);
			return getResult(chain, ExecuteChain.EXCEPTION);
		} finally {
			PrototypeStatus ps = PrototypeStatus.getStatus();
			if (create) {
				ps.end();
			}
			Message.getSubject().onNext(new Message(Message.EXECUTE, type.getName(), ps));
		}
	}

	private Object getResult(ExecuteChain chain, int resultType) {
		Service service = chain.getService();
		Object rs = chain.getResult();
		try {
			if (rs == null || !service.getBaseType().isInstance(rs)) {
				if (service.getConstructor().getParameterCount() == 0) {
					rs = service.getType().newInstance();
				} else {
					rs = service.getBaseType().newInstance();
				}
				chain.setResult(rs);
			}
			service.getSetResult().invoke(rs, resultType);
			return GetResultExecuteFilter.processResult(rs, service.getResultType());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private boolean initStatus() {
		PrototypeStatus status = PrototypeStatus.getStatus();
		if (status == null) {
			status = new PrototypeStatus();
			PrototypeStatus.setStatus(status);
			return true;
		}
		return false;
	}

	private List<ExecuteFilter<?>> findFilters(Class<?> type) {
		Annotation[] annotations = AnnotationUtils.getAnnotationByMeta(type.getAnnotations(), Executor.class);
		List<ExecuteFilter<?>> list = new ArrayList<>(defaultFilters);
		if (annotations.length > 0 || !globalAnnotations.isEmpty()) {
			for (Annotation annotation : merge(annotations)) {
				list.add(filters.get(annotation.annotationType()));
			}
		}
		Collections.sort(list, new AnnotationsChainComparator<>(ExecuteFilter.class, annotations));
		return list;
	}

	/**
	 * 全局定义的被类中的覆盖
	 * 
	 * @param annotations
	 * @return
	 */
	private Collection<Annotation> merge(Annotation[] annotations) {
		Map<Class<?>, Annotation> map = new LinkedHashMap<>(globalAnnotations);
		for (Annotation annotation : annotations) {
			map.put(annotation.annotationType(), annotation);
		}
		return map.values();
	}

	private class ExecuteChainImpl implements ExecuteChain {
		private List<ExecuteFilter<?>> filters;

		private int index;

		private int max;

		@Getter
		private boolean async;
		@Getter
		private Class<?> type;

		@Getter
		private Object target;

		@Getter
		private Object[] params;

		@Getter
		private Object result;

		@Getter
		private Service service;

		private BusinessDefine define;

		@Getter
		private boolean validated = true;

		public void setTarget(Object target) {
			this.target = target;
			PrototypeStatus.getStatus().setTarget(target);
		}

		public void setResult(Object result) {
			if (result == null
					|| (!service.getResultType().isInstance(result) && !service.getBaseType().isInstance(result))) {
				throw new IllegalArgumentException(
						"Result must be instance of " + service.getResultType() + " or " + service.getBaseType());
			}
			this.result = result;
			PrototypeStatus.getStatus().setResult(result);
		}

		public ExecuteChainImpl(boolean async, Class<?> type, Object[] params, List<ExecuteFilter<?>> filters) {
			this.async = async;
			this.type = type;
			this.params = params;
			this.filters = filters;
			this.max = filters.size();
			service = services.get(type);
			define = type.getAnnotation(BusinessDefine.class);
			if (define == null) {
				throw new IllegalArgumentException(
						"Class " + type.getName() + " is not a service , it's need '@ServiceDefine'");
			}
		}

		@Override
		public void doChain() throws Exception {
			boolean debug = log.isDebugEnabled();
			PrototypeStatus status = PrototypeStatus.getStatus();
			if (debug) {
				long nano = System.nanoTime();
				int filterIndex = index;
				try {
					doChainInner(status);
				} finally {
					if (filterIndex < max) {
						log.debug("Businiss {} , {} execute filter : {} use time {} nanoseconds", status.getId(), type,
								filters.get(filterIndex), System.nanoTime() - nano);
					} else {
						log.debug("Businiss {} , {} execute method : {} use time {} nanoseconds", status.getId(), type,
								define.execute(), System.nanoTime() - nano);
					}
				}
			} else {
				doChainInner(status);
			}
		}

		private void doChainInner(PrototypeStatus status) throws Exception {
			if (index < max) {
				log.debug("Businiss {} , {} execute filter {}", status.getId(), type, filters.get(index));
				filters.get(index++).doFilter(this);
			} else {
				log.debug("Businiss {} , {} execute method : {}", status.getId(), type, define.execute());
				MethodUtils.findMethod(type, define.execute()).invoke(target);
			}
		}

		@Override
		public void setResultType(int resultType) {
			result = BusinessExecutorImpl.this.getResult(this, resultType);
			log.debug("Business {} set result {} to {}", PrototypeStatus.getStatus().getId(), resultType, type);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			T rs = type.getAnnotation(annotationClass);
			return rs == null ? (T) globalAnnotations.get(annotationClass) : rs;
		}

		@Override
		public void addValidateError(String error) {
			validated = false;
			setResultType(VALIDATE);
			try {
				service.getAddValidateError().invoke(result, error);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Service getService(Class<?> type) {
		return services.get(type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerService(Service service) {
		services.put(service.getType(), service);
	}

	@Override
	public Object getApi(String type, String businessName) throws UnsupportedOperationException {
		Assert.notNull(type);
		Assert.notNull(businessName);
		ApiCreator<?> creator = creators.get(type);
		if (creator == null) {
			throw new UnsupportedOperationException("Api type '" + type + "' is not supported");
		}
		ClassFactory factory = scaner.getClassFactory();
		Service service = this.services.get(factory.loadClass(businessName));
		if (service == null || !service.getType().getAnnotation(ServiceDefine.class).open()) {
			throw new UnsupportedOperationException("Business class " + businessName + " not found or not public");
		}
		return creator.create(service);
	}

	@Override
	public Collection<Service> listServices(boolean onlyPublic) {
		if (onlyPublic) {
			List<Service> list = new ArrayList<>();
			for (Service service : services.values()) {
				if (service.getType().getAnnotation(ServiceDefine.class).open()) {
					list.add(service);
				}
			}
			return list;
		}
		return services.values();
	}

	private Map<String, List<Service>> url_service_map;

	@Override
	public Map<String, List<Service>> groupByUrl(Collection<Service> services) {
		Map<String, List<Service>> map = new HashMap<>();
		for (Service service : services) {
			String url = service.getDefine().url();
			List<Service> list = map.get(url);
			if (list == null) {
				list = new ArrayList<Service>();
				map.put(url, list);
			}
			list.add(service);
		}
		ServiceComparator comparator = new ServiceComparator();
		for (List<Service> list : map.values()) {
			Collections.sort(list, comparator);
		}
		return map;
	}

	@Override
	public Service findService(String url, String version) {
		if (url_service_map == null) {
			synchronized (this) {
				url_service_map = groupByUrl(services.values());
			}
		}
		List<Service> services = url_service_map.get(url);
		if (services == null) {
			return null;
		}
		ServiceComparator comparator = new ServiceComparator();
		if (version == null) {
			return services.get(0);
		}
		int m=0;
		for (Service service : services) {
			int k = comparator.compareVersion(version, service.getDefine().version());
			if(k==0){
				return service;
			}else if (k < 0) {
				return m==0?service:services.get(m);
			}
			m++;
		}
		return services.get(services.size() - 1);
	}

	/**
	 * 服务版本比较
	 * 
	 * @author lj
	 *
	 */
	private static class ServiceComparator implements Comparator<Service> {

		private static final Pattern NUM = Pattern.compile("^\\d+$");

		@Override
		public int compare(Service o1, Service o2) {
			int k = o1.getDefine().url().compareTo(o2.getDefine().url());
			if (k == 0) {
				k = compareVersion(o1.getDefine().version(), o2.getDefine().version());
			}
			return k;
		}

		private int compareVersion(String version1, String version2) {
			String[] v1 = version1.split("[.]");
			String[] v2 = version2.split("[.]");
			int k = v1.length, m = v2.length;
			int t = 0;
			for (int i = 0; i < k && i < m; i++) {
				if (NUM.matcher(v1[i]).matches() && NUM.matcher(v2[i]).matches()) {
					t = Integer.valueOf(v2[i]) - Integer.valueOf(v1[i]);
				} else {
					t = v2[i].compareTo(v1[i]);
				}
				if (t != 0) {
					return t;
				}
			}
			return m - k;
		}

	}
}
