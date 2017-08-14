/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.prototype.springframework;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.prototype.annotation.Message;
import org.prototype.core.ChainOrder;
import org.prototype.core.ComponentContainer;
import org.prototype.core.Errors;
import org.prototype.core.MethodAdvisor;
import org.prototype.core.MethodBuilder;
import org.prototype.core.MethodChain;
import org.prototype.core.MethodFilter;
import org.prototype.core.PrototypeStatus;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.NoRollbackRuleAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring {@link org.springframework.transaction.annotation.Transactional}注解适配. <br>
 * <pre>
 * 此类实现完整的spring事务处理注解支持。
 * 在一个方法上使用注解Transactional将开启一个独立的事务处理，即使这个方法被另一个加了注解Transactional的方法调用。
 * </pre>
 * @author lj
 *
 */
@Slf4j
@Component
public class TransactionalMethodAdvisor implements MethodAdvisor {

	@Resource
	private ApplicationContext context;
	@Resource
	private ComponentContainer container;

	private Map<Transactional, TransactionTemplate> templates = new ConcurrentHashMap<>();

	@Override
	public MethodFilter<?> matches(MethodBuilder accessor, Errors errors) {
		Transactional transactional = accessor.getAnnotation(Transactional.class);
		if (transactional == null) {
			return null;
		}
		if (!existTransactionManager(
				transactional.value().length() > 0 ? transactional.value() : transactional.transactionManager())) {

			return null;
		}
		return new TransactionalMethodFilter(transactional);
	}

	/**
	 * 检查指定的Excecutor bean是否存在
	 * 
	 * @param executor
	 *            bean名称
	 * @return 存在返回true
	 */
	private boolean existTransactionManager(String name) {
		if (!"".equals(name)) {
			return context.containsBean(name) && PlatformTransactionManager.class.isInstance(context.getBean(name));
		}
		return container.containsBean(PlatformTransactionManager.class);
	}

	/**
	 * 事务方法过滤
	 * @author lj
	 *
	 */
	@Order(ChainOrder.MIDDLE)
	private class TransactionalMethodFilter implements MethodFilter<Transactional> {

		private Transactional transactional;
		private TransactionTemplate template;
		private Set<Class<?>> noRollbackFor=new HashSet<>();
		private Set<Class<?>> rollbackFor=new HashSet<>();

		public TransactionalMethodFilter(Transactional transactional) {
			this.transactional = transactional;
			template = createTransactionTemplate();
			initIgnoreExceptions();
		}

		@Override
		public Object doFilter(Object[] args, MethodChain chain) throws Exception {
			PrototypeStatus status = PrototypeStatus.getStatus();
			final boolean create = status == null;
			if (create) {// 未开启原型状态则开启
				log.debug("Create new transaction in {}", chain.getMethod());
				status = new PrototypeStatus();
				PrototypeStatus.setStatus(status);
			}
			final PrototypeStatus ps = status;
			final PrototypeStatus.TransactionStatus old = status.getTransaction();
			final PrototypeStatus.TransactionStatus current = new PrototypeStatus.TransactionStatus();
			ps.setTransaction(current);// 相当于压栈处理
			return template.execute(new TransactionCallback<Object>() {

				@Override
				public Object doInTransaction(TransactionStatus status) {
					ps.getTransaction().setReadOnly(transactional.readOnly());
					return transaction(status, chain, args, ps, old, create);// 此方法中执行事务并将状态出栈
				}
			});
		}

		/**
		 * 获取所有需要忽略的异常(包括默认配置的属性)
		 */
		private void initIgnoreExceptions() {
			for (Class<?> clazz : transactional.noRollbackFor()) {
				noRollbackFor.add(clazz);
			}
			for (Class<?> clazz : transactional.rollbackFor()) {
				rollbackFor.add(clazz);
			}
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			for (String name : transactional.noRollbackForClassName()) {
				try {
					noRollbackFor.add(loader.loadClass(name));
				} catch (ClassNotFoundException e) {
					// do nothing
				}
			}
			for (String name : transactional.rollbackForClassName()) {
				try {
					rollbackFor.add(loader.loadClass(name));
				} catch (ClassNotFoundException e) {
					// do nothing
				}
			}
		}

		/**
		 * 如果可能，就从缓存获取事务模板，否则创建之
		 * 
		 * @return 事务模板
		 */
		private TransactionTemplate createTransactionTemplate() {
			TransactionTemplate template = templates.get(transactional);
			if (template != null) {// 返回缓存的
				return template;
			}
			PlatformTransactionManager manager = getPlatformTransactionManager(
					transactional.value().length() == 0 ? transactional.transactionManager() : transactional.value());
			RuleBasedTransactionAttribute transactionDefinition = new RuleBasedTransactionAttribute();
			transactionDefinition.setRollbackRules(createRollbackRules(transactional));
			transactionDefinition.setIsolationLevel(transactional.isolation().value());
			transactionDefinition.setPropagationBehavior(transactional.propagation().value());
			transactionDefinition.setTimeout(transactional.timeout());
			template = new TransactionTemplate(manager, transactionDefinition);
			templates.put(transactional, template);// 缓存
			return template;
		}

		/**
		 * 获取事务管理器
		 * @param name 名称
		 * @return 事务管理器
		 */
		private PlatformTransactionManager getPlatformTransactionManager(String name) {
			if (!"".equals(name)) {
				return (PlatformTransactionManager) context.getBean(name);
			}
			return context.getBean(PlatformTransactionManager.class);
		}

		/**
		 * 执行事务
		 * 
		 * @param status
		 *            事务状态
		 * @param chain
		 *            调用者
		 * @param args
		 *            参数
		 * @param ps
		 *            原型状态
		 * @param old
		 *            之前的事务状态
		 * @param create
		 *            是否创建了原型状态
		 * @return 调用方法的结果
		 */
		private Object transaction(TransactionStatus status, MethodChain chain, Object[] args, PrototypeStatus ps,
				PrototypeStatus.TransactionStatus old, boolean create) {
			log.debug("Begining transaction in {}", chain.getMethod());
			try {
				return chain.doFilter(args);
			} catch (InvocationTargetException e) {
				Throwable t = e.getTargetException();
				if (!isIgonre(t)) {
					Message.getSubject().onNext(new Message(Message.ROLLBACK, chain.getTarget().getClass().getName(),
							new Message.ExceptionMessage(chain, e)));
				}
				if (t instanceof RuntimeException) {
					throw (RuntimeException) t;
				} else {
					throw new RuntimeException(t);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				log.debug("End transaction in {}", chain.getMethod());
				ps.setTransaction(old);// 相当于出栈处理
				if (create) {
					PrototypeStatus.setStatus(null);
				}
			}
		}

		/**
		 * 检查回滚是否需要忽略
		 * 
		 * @param throwable
		 *            抛出的异常
		 * @return 忽略则返回true
		 */
		private boolean isIgonre(Throwable throwable) {
			for (Class<?> cls : rollbackFor) {
				if (cls.isInstance(throwable)) {
					return false;
				}
			}
			for (Class<?> cls : noRollbackFor) {
				if (cls.isInstance(throwable)) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * 创建事务回滚规则
	 * 
	 * @param transactional
	 *            事务注解
	 * @return 事务回滚规则
	 */
	private List<RollbackRuleAttribute> createRollbackRules(Transactional transactional) {
		List<RollbackRuleAttribute> rs = new ArrayList<>();
		for (Class<?> cls : transactional.noRollbackFor()) {
			rs.add(new NoRollbackRuleAttribute(cls));
		}
		for (String name : transactional.noRollbackForClassName()) {
			rs.add(new NoRollbackRuleAttribute(name));
		}
		for (Class<?> cls : transactional.rollbackFor()) {
			rs.add(new RollbackRuleAttribute(cls));
		}
		for (String name : transactional.rollbackForClassName()) {
			rs.add(new RollbackRuleAttribute(name));
		}
		return rs;
	}

}
