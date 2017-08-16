package org.prototype.javassist;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.prototype.core.AnnotationsChainComparator;
import org.prototype.core.ClassAdvisor;
import org.prototype.core.ClassFactory;
import org.prototype.core.ClassScaner;
import org.prototype.core.ComponentContainer;
import org.prototype.core.Errors;
import org.prototype.core.FieldAdvisor;
import org.prototype.core.FieldInvoker;
import org.prototype.core.MethodAdvisor;
import org.prototype.core.MethodBuilder;
import org.prototype.core.MethodFilter;
import org.prototype.core.Prototype;
import org.prototype.core.PrototypeStatus;
import org.prototype.inject.InjectHelper;
import org.prototype.reflect.AnnotationUtils;
import org.prototype.reflect.MethodUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.stereotype.Component;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

@Slf4j
@Component
public class ClassScanerImpl implements ClassScaner, BeanFactoryAware {

	private static final String POSTFIX = "$Impl";

	@Getter
	private ClassFactoryImpl factory;

	@Resource
	private ComponentContainer container;

	@Getter
	private BeanFactory beanFactory;

	private static List<Object> beans = new ArrayList<>();

	static int addBean(Object bean) {
		synchronized (beans) {
			int rs = beans.indexOf(bean);
			if (rs == -1) {
				beans.add(bean);
				rs = beans.size() - 1;
			}
			return rs;
		}
	}

	@Override
	public ClassFactory getClassFactory() {
		return factory;
	}

	@PreDestroy
	void destroy() {
		beans.clear();
	}

	/**
	 * 用于执行经过方法适配的方法
	 * 
	 * @param filterIndex
	 *            方法过滤链的索引
	 * @param debug
	 *            是否开启debug
	 * @param type
	 *            对象类型
	 * @param target
	 *            目标对象
	 * @param methodName
	 *            目标对象的方法
	 * @param parameterTypes
	 *            方法的参数类型
	 * @param args
	 *            方法的参数值
	 * @return 方法执行结果
	 */
	@SuppressWarnings("unchecked")
	public static Object execute(int filterIndex, boolean debug, Class<?> type, Object target, String methodName,
			Class<?>[] parameterTypes, Object[] args) {
		Method method = MethodUtils.findMethod(type, methodName, parameterTypes);
		Method implMethod = MethodUtils.findMethod(type, methodName + POSTFIX, parameterTypes);
		MethodChainImpl chainImpl = new MethodChainImpl(target, method, implMethod,
				(List<MethodFilter<?>>) beans.get(filterIndex));
		if (debug) {
			long nano = System.nanoTime();
			try {
				return executeInner(method, implMethod, chainImpl, target, args);
			} finally {
				log.debug("Execute method '{}' , method chain is {} , user {} nanoseconds", method, chainImpl,
						System.nanoTime() - nano);
			}
		} else {
			return executeInner(method, implMethod, chainImpl, target, args);
		}
	}

	private static Object executeInner(Method method, Method implMethod, MethodChainImpl chainImpl, Object target,
			Object[] args) {
		PrototypeStatus status = PrototypeStatus.getStatus();
		boolean create = status == null;
		if (create) {
			status = new PrototypeStatus();
			PrototypeStatus.setStatus(status);
		}
		try {
			log.debug("Business {} , Execute method '{}' , method chain is {}",status.getId(), method, chainImpl);
			return chainImpl.doFilter(args);
		} catch (InvocationTargetException e) {
			Throwable throwable = e.getTargetException();
			throw RuntimeException.class.isInstance(throwable) ? (RuntimeException) throwable
					: new RuntimeException(throwable);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (create) {
				PrototypeStatus.setStatus(null);
			}
		}
	}

	/**
	 * 使用RxJava方式扫描
	 */
	@Override
	public void scan(Collection<String> classNames, Errors errors) {
		Scaner scaner = new Scaner(errors);
		Observable.from(classNames).map(scaner).filter(new AnnotationFilter(Prototype.class))
				.subscribe(scaner.subscriber());
	}

	/**
	 * 包扫描程序
	 * 
	 * @author flyxxxxx@163.com
	 *
	 */
	class Scaner implements Func1<String, CtClass> {
		private Errors errors;// 错误

		public Scaner(Errors errors) {
			this.errors = errors;
		}

		@Override
		public CtClass call(String className) {
			try {
				return factory.getClassPool().get(className);
			} catch (NotFoundException e) {
				log.warn("Class not found : " + className, e);
				return CtClass.booleanType;
			}
		}

		/**
		 * 分派给类处理程序
		 * 
		 * @return 类处理程序
		 */
		public Subscriber<CtClass> subscriber() {
			return new CtClassSubscriber();
		}

		/**
		 * 类处理程序
		 * 
		 * @author flyxxxxx@163.com
		 *
		 */
		class CtClassSubscriber extends Subscriber<CtClass> {
			// 要处理的全部类
			private Set<Class<?>> classes = new HashSet<>();

			// 每个类处理并加载完成后，注册bean并调用ClassAdvisor的完成方法（以创建如控制器类），如果有错误，则抛出异常
			@Override
			public void onCompleted() {
				for (ClassAdvisor ca : container.getComponents(ClassAdvisor.class)) {
					ca.onComplete(factory, errors);
				}
				if (errors.hasError()) {
					throw new RuntimeException(errors.getMessages().toString());
				}
			}

			@Override
			public void onError(Throwable e) {
				log.error("Modify classes error", e);
			}

			// 处理类的过程中，优先处理父类
			@Override
			public void onNext(CtClass clazz) {
				if (CtClass.booleanType == clazz) {
					return;
				}
				try {
					CtClass parent = clazz.getSuperclass();
					if (parent != null && !Object.class.getName().equals(parent.getName())) {
						onNext(parent);
					}
					if (clazz.isFrozen()) {
						return;
					}
					log.debug("Found prototype class " + clazz.getName());
					modifyClass(clazz);// 修改类及方法
					Class<?> cls = factory.getClassBuilder(clazz).create();
					modifyFields(cls);// 修改类的静态成员变量值
					classes.add(cls);
				} catch (NotFoundException | ClassNotFoundException e) {
					log.warn("Compile class error : ", e);
				}
			}

			/**
			 * 修改类的静态成员变量
			 * 
			 * @param advisors
			 *            成员变量甜酸
			 * @param clazz
			 *            类
			 */
			private void modifyFields(Class<?> clazz) {
				for (Field field : clazz.getDeclaredFields()) {
					if (!Modifier.isStatic(field.getModifiers())) {
						continue;
					}
					List<FieldAdvisor> advisors = container.getComponents(FieldAdvisor.class);
					if (advisors.isEmpty()) {
						return;
					}
					Collections.sort(advisors,
							new AnnotationsChainComparator<>(FieldAdvisor.class, field.getAnnotations()));
					FieldInvoker invoker = new StaticFieldInvoker(field);
					for (FieldAdvisor advisor : advisors) {
						advisor.afterLoad(invoker, errors);
					}
				}
			}

			/**
			 * 修改类
			 * 
			 * @param clazz
			 *            类
			 */
			@SuppressWarnings({ "unchecked", "rawtypes" })
			private void modifyClass(CtClass clazz) throws ClassNotFoundException {
				CtClassBuilder accessor = factory.getClassBuilder(clazz);
				List<ClassAdvisor> cas = container.getComponents(ClassAdvisor.class);
				Collections.sort(cas, new AnnotationsChainComparator(ClassAdvisor.class,
						AnnotationUtils.getAnnotations(clazz.getAnnotations())));
				for (ClassAdvisor ca : cas) {
					ca.beforeLoad(accessor, errors);// 类本身的修改
				}
				boolean debug = log.isDebugEnabled();
				for (CtMethod method : accessor.getDeclaredMethods()) {
					int modifier = method.getModifiers();
					if (javassist.Modifier.isStatic(modifier) || javassist.Modifier.isAbstract(modifier)) {
						continue;
					}
					List<MethodAdvisor> matchers = container.getComponents(MethodAdvisor.class);
					List<MethodFilter> filters = getFilters(method, matchers);
					if (filters.isEmpty()) {
						continue;
					}
					log.debug("Modify method : " + CtMethodUtils.getDescription(method));
					Collections.sort(filters, new AnnotationsChainComparator(MethodFilter.class,
							AnnotationUtils.getAnnotations(method.getAnnotations())));
					modifyMethod(debug, method, filters);
				}
			}

			@SuppressWarnings("rawtypes")
			private List<MethodFilter> getFilters(CtMethod method, List<MethodAdvisor> matchers) {
				List<MethodFilter> filters = new ArrayList<>();
				MethodBuilder builder = new CtMethodBuilder(factory, method);
				for (MethodAdvisor matcher : matchers) {
					MethodFilter<?> filter = matcher.matches(builder, errors);
					if (filter != null) {
						filters.add(filter);
					}
				}
				return filters;
			}

			/**
			 * 通过一个方法
			 * 
			 * @param debug
			 *            是否需要debug
			 * @param method
			 *            javassist方法
			 * @param advisor
			 *            方法适配
			 */
			@SuppressWarnings("rawtypes")
			private void modifyMethod(boolean debug, CtMethod method, List<MethodFilter> filters) {
				String body = null;
				try {
					CtMethod newMethod = CtNewMethod.copy(method, method.getDeclaringClass(), null);
					body = CtMethodUtils.buildNewMethod(addBean(filters), debug, method);
					method.setName(method.getName() + POSTFIX);
					method.setModifiers(javassist.Modifier.PRIVATE);
					newMethod.setBody(body);
					copyAnnotation(method, newMethod);
					method.getDeclaringClass().addMethod(newMethod);
				} catch (CannotCompileException | ClassNotFoundException | InvocationTargetException
						| IllegalAccessException | NotFoundException e) {
					throw new RuntimeException("Modify method '" + CtMethodUtils.getDescription(method) + "' failed"
							+ (body == null ? "" : " , javassist code : \r\n" + body), e);
				}
			}

			/**
			 * 复制方法注解
			 * 
			 * @param oldMethod
			 * @param newMethod
			 * @throws ClassNotFoundException
			 * @throws InvocationTargetException
			 * @throws IllegalAccessException
			 */
			private void copyAnnotation(CtMethod oldMethod, CtMethod newMethod)
					throws ClassNotFoundException, InvocationTargetException, IllegalAccessException {
				ConstPool cp = newMethod.getMethodInfo().getConstPool();
				AnnotationsAttribute attr = new AnnotationsAttribute(cp, AnnotationsAttribute.visibleTag);
				for (Object obj : oldMethod.getAnnotations()) {
					Annotation annotation = (Annotation) obj;
					javassist.bytecode.annotation.Annotation ann = new javassist.bytecode.annotation.Annotation(
							annotation.annotationType().getName(), cp);
					for (Method method : annotation.annotationType().getMethods()) {
						if (method.getParameterTypes().length > 0 || void.class.equals(method.getReturnType())) {
							continue;
						}
						ann.addMemberValue(method.getName(),
								CtAnnotationUtils.getMemberValue(cp, method.invoke(annotation)));
					}
					attr.addAnnotation(ann);
				}
				newMethod.getMethodInfo().addAttribute(attr);
			}
		}

	}

	/**
	 * 是否原型类的过滤
	 * 
	 * @author flyxxxxx@163.com
	 *
	 */
	static class AnnotationFilter implements Func1<CtClass, Boolean> {

		private Class<? extends Annotation> annotationClass;

		public AnnotationFilter(Class<? extends Annotation> annotationClass) {
			this.annotationClass = annotationClass;
		}

		@Override
		public Boolean call(CtClass clazz) {
			if (clazz.isInterface() || clazz.isEnum() || clazz.isAnnotation()) {
				return Boolean.FALSE;
			}
			return CtAnnotationUtils.hasAnnotation(clazz, annotationClass);
		}

	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory=beanFactory;
		factory = new ClassFactoryImpl((BeanDefinitionRegistry) beanFactory,
				container.getComponent(InjectHelper.class));
	}

}
