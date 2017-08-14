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
package org.prototype.inject;

import javax.annotation.Resource;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.prototype.core.ConditionalHasClass;
import org.quartz.SchedulerContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.weaving.LoadTimeWeaverAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.jmx.export.notification.NotificationPublisherAware;
import org.springframework.scheduling.quartz.SchedulerContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.portlet.context.PortletConfigAware;
import org.springframework.web.portlet.context.PortletContextAware;

import lombok.Getter;

/**
 * ApplicationContext作为方法参数注入
 * 
 * @author flyxxxxx@163.com
 *
 */
public class SpringAwareInjecter<T> extends AwareBeanInjecter<T> {
	// ServletContext和Environment是不需要的，已经注册在applicationContext

	@Resource
	private ApplicationContext applicationContext;

	@SuppressWarnings("unchecked")
	@Override
	public T getBean() {
		return (T) applicationContext;
	}

	@Component
	public static class ApplicationContextInjector extends SpringAwareInjecter<ApplicationContext> {
	}

	@Component
	public static class BeanFactoryInjecter extends AwareBeanInjecter<BeanFactory> implements BeanFactoryAware {

		@Getter
		private BeanFactory bean;

		@Override
		public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
			bean = beanFactory;
		}

	}

	@Component
	public static class MessageSourceInjector extends SpringAwareInjecter<MessageSource> {
	}

	@Component
	public static class ResourceLoaderInjector extends SpringAwareInjecter<ResourceLoader> {
	}

	@Component
	public static class ApplicationEventPublisherInjecter extends SpringAwareInjecter<ApplicationEventPublisher> {
	}

	@Component
	@ConditionalHasClass({ ServletContext.class, ServletContextAware.class })
	public static class WebApplicationContextInjector extends SpringAwareInjecter<WebApplicationContext> {
	}

	@Component
	@ConditionalHasClass({ ServletConfig.class, ServletConfigAware.class })
	public static class NotificationPublisherInjecter extends AwareBeanInjecter<NotificationPublisher>
			implements NotificationPublisherAware {

		@Getter
		private NotificationPublisher bean;

		@Override
		public void setNotificationPublisher(NotificationPublisher notificationPublisher) {
			bean = notificationPublisher;
		}

	}

	@Component
	@ConditionalHasClass({ LoadTimeWeaver.class })
	public static class LoadTimeWeaverInjecter extends AwareBeanInjecter<LoadTimeWeaver>
			implements LoadTimeWeaverAware {

		@Getter
		private LoadTimeWeaver bean;

		@Override
		public void setLoadTimeWeaver(LoadTimeWeaver loadTimeWeaver) {
			bean = loadTimeWeaver;
		}

	}

	@Component
	public static class AnnotationMetadataInjecter extends AwareBeanInjecter<AnnotationMetadata>
			implements ImportAware {

		@Getter
		private AnnotationMetadata bean;

		@Override
		public void setImportMetadata(AnnotationMetadata importMetadata) {
			bean = importMetadata;
		}

	}

	@Component
	@ConditionalHasClass({ ServletConfig.class, ServletConfigAware.class })
	public static class ServletConfigInjecter extends AwareBeanInjecter<ServletConfig> implements ServletConfigAware {

		@Getter
		private ServletConfig bean;

		@Override
		public void setServletConfig(ServletConfig servletConfig) {
			bean = servletConfig;
		}

	}

	@Component
	@ConditionalHasClass({ PortletContext.class, PortletContextAware.class })
	public static class PortletContextInjecter extends AwareBeanInjecter<PortletContext>
			implements PortletContextAware {

		@Getter
		private PortletContext bean;

		@Override
		public void setPortletContext(PortletContext portletContext) {
			bean = portletContext;
		}

	}

	@Component
	@ConditionalHasClass({ PortletConfig.class, PortletConfigAware.class })
	public static class PortletConfigInjecter extends AwareBeanInjecter<PortletConfig> implements PortletConfigAware {

		@Getter
		private PortletConfig bean;

		@Override
		public void setPortletConfig(PortletConfig portletConfig) {
			bean = portletConfig;
		}

	}

	@Component
	@ConditionalHasClass({ SchedulerContext.class, SchedulerContextAware.class })
	public static class SchedulerContextInjecter extends AwareBeanInjecter<SchedulerContext>
			implements SchedulerContextAware {

		@Getter
		private SchedulerContext bean;

		@Override
		public void setSchedulerContext(SchedulerContext schedulerContext) {
			this.bean = schedulerContext;
		}

	}
}
