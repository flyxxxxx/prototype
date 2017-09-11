package org.prototype.business;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.prototype.annotation.Chain;
import org.prototype.annotation.OverloadAsync;
import org.prototype.core.AnnotationBuilder;
import org.prototype.core.ChainOrder;
import org.prototype.core.ClassAdvisor;
import org.prototype.core.ClassBuilder;
import org.prototype.core.ClassFactory;
import org.prototype.core.Errors;
import org.prototype.core.MethodBuilder;
import org.prototype.reflect.ClassUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

/**
 * 业务类适配. <br>
 * 主要功能是对业务类的业务方法进行检查，并给业务执行方法加上责任链和OverloadAsync注解.
 * 
 * <pre>
 * 业务类是指定义注解{@link BusinessDefine}的类或子类。
 * 业务类必须定义BusinessDefine中指定的入口方法（默认为public void execute(){}）.
 * 入口方法允许加Transactional注解，意味着所有的同步业务方法（{@link BusinessDefine#sync()}）在一个事务中执行，否则每个业务方法均有不同的事务处理（默认的处理方式）。
 * 业务方法如果定义了Transactional注解,将按注解的定义独立进行事务处理（忽略BusinessDefine注解中定义的读写规则等）。
 * </pre>
 * 
 * @author flyxxxxx@163.com
 *
 */
@Slf4j
@Component
@Order(ChainOrder.VERY_HIGH)
public class BusinessClassAdvisor implements ClassAdvisor {

	private Set<String> classes = new HashSet<>();

	@Override
	public void beforeLoad(ClassBuilder builder, Errors errors) {
		BusinessDefine current = builder.getAnnotation(BusinessDefine.class);//TODO 未给子类方法加Transactional等
		if (current == null) {
			return;
		}
		MethodBuilder mb = null;
		ClassBuilder cb = builder;
		while (cb != null) {
			mb = cb.findMethod(current.execute());
			if (mb != null) {
				break;
			}
			cb = cb.getSuperClassBuilder();
		}
		if (mb == null) {
			errors.add("business.method.notfound", builder.toString(), current.execute());
			return;
		} else if (mb.getClassBuilder() != builder) {
			return;
		}else if(mb.getExceptionTypes().length>0){
			errors.add("business.method.exception", builder.toString(), current.execute());
			return ;
		}
		boolean transaction = mb.getAnnotation(Transactional.class) == null;
		List<String> chainValues = checkMethods(builder, errors, current.sync(), false, transaction);
		List<String> asyncValue = checkMethods(builder, errors, current.async(), true, true);
		MethodBuilder target = (MethodBuilder) builder.findMethod(current.execute());
		buildChain(errors, target, chainValues);
		buildOverloadAsync(errors, target, asyncValue);
		target.create();
		classes.add(builder.getName());
	}

	/**
	 * 在原方法上加OverloadAsync注解与事务
	 * 
	 * @param errors
	 *            处理中的错误
	 * @param target
	 *            目标方法
	 * @param asyncValue
	 *            异步方法名
	 */
	private void buildOverloadAsync(Errors errors, MethodBuilder target, List<String> asyncValue) {
		if (asyncValue.isEmpty()) {
			return;
		}
		if (target.getAnnotation(OverloadAsync.class) != null) {
			log.warn("Annotation 'OverloadAsync' is exists in class " + target.toString());
			return;
		}
		target.getAnnotationBuilder(OverloadAsync.class).setAttribute("after", true).setAttribute("value",
				asyncValue.toArray(new String[asyncValue.size()]));
	}

	/**
	 * 在原execute方法上加Chain注解
	 * 
	 * @param errors
	 *            处理中的错误
	 * @param target
	 *            目标方法
	 * @param chainValues
	 *            责任链方法名
	 */
	private void buildChain(Errors errors, MethodBuilder target, List<String> chainValues) {
		if (chainValues.isEmpty()) {
			return;
		}
		if (target.getAnnotation(Chain.class) != null) {
			log.warn("Annotation 'Chain' is exists in class " + target.toString());
			return;
		}
		target.getAnnotationBuilder(Chain.class).setAttribute("after", false).setAttribute("dynamic", true)
				.setAttribute("value", chainValues.toArray(new String[chainValues.size()]));
	}

	/**
	 * 检查方法
	 * 
	 * @param builder
	 *            类构建器
	 * @param errors
	 *            处理中的错误
	 * @param methods
	 *            业务方法定义
	 * @param async
	 *            是否异步业务方法
	 * @param transaction
	 *            是否全局事务
	 * @return 方法链
	 */
	private List<String> checkMethods(ClassBuilder builder, Errors errors, BusinessMethod[] methods, boolean async,
			boolean transaction) {
		List<String> list = new ArrayList<>();
		for (BusinessMethod method : methods) {
			if (async && method.overload()) {// 异步方法
				for (MethodBuilder ma : builder.findMethods(method.value())) {
					checkMethod(errors, method, (MethodBuilder) ma, transaction);
				}
			} else {// 责任链方法
				MethodBuilder ma = builder.findUniqueMethod(method.value(), errors, BusinessMethod.class);
				if (ma != null) {
					checkMethod(errors, method, (MethodBuilder) ma, transaction);
				}
			}
			list.add(method.value());
		}
		return list;
	}

	/**
	 * 检查指定的方法
	 * 
	 * @param errors
	 *            处理中的错误
	 * @param method
	 *            业务方法注解
	 * @param target
	 *            业务方法构建目标
	 * @param transaction
	 *            是否需要事务
	 * @return 通过检查返回true
	 */
	private boolean checkMethod(Errors errors, BusinessMethod method, MethodBuilder target, boolean transaction) {
		if (target == null) {
			return false;
		}
		if (!target.enableInject(errors)) {
			errors.add("method.inject.some", target.toString());
			return false;
		}
		if (transaction && method.transaction() && target.isNeedTransaction(errors)
				&& target.getAnnotation(Transactional.class) == null) {
			AnnotationBuilder builder = target.getAnnotationBuilder(Transactional.class);
			if (method.readOnly()) {
				builder.setAttribute("readOnly", true);
			}
			target.create();
		}
		return true;
	}

	/**
	 * 检查业务类的成员变量中有View注解的，多于一个视为异常
	 */
	@Override
	public void onComplete(ClassFactory factory, Errors errors) {
		for (String name : classes) {
			Class<?> clazz = factory.loadClass(name);
			if (ClassUtils.findProperty(clazz, View.class).size() > 1) {
				errors.add("business.view.morethanone", clazz.getName());
			}
		}
	}

}
