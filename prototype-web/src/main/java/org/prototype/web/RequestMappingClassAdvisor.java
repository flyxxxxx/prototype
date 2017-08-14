package org.prototype.web;

import java.util.Arrays;
import java.util.List;

import org.prototype.business.ServiceDefine;
import org.prototype.core.AnnotationBuilder;
import org.prototype.core.ChainOrder;
import org.prototype.core.ClassAdvisor;
import org.prototype.core.ClassBuilder;
import org.prototype.core.ClassFactory;
import org.prototype.core.ConstructorBuilder;
import org.prototype.core.Errors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 根据POST/GET方式确写事务读写
 * 
 * @author lj
 *
 */
@Component
@Order(ChainOrder.VERY_HIGH - 1)
public class RequestMappingClassAdvisor implements ClassAdvisor {

	private static final List<RequestMethod> GET_METHOD = Arrays.asList(RequestMethod.GET, RequestMethod.HEAD,
			RequestMethod.OPTIONS, RequestMethod.TRACE);

	@Value("${spring.cloud.config.name:}${server.contextPath:/}")
	private String contextPath;

	@Override
	public void beforeLoad(ClassBuilder builder, Errors errors) {
		RequestMapping mapping = findRequestMapping(builder, errors);
		if (mapping == null) {
			ServiceDefine define = builder.getAnnotation(ServiceDefine.class);
			if (define == null) {
				return;
			}
			AnnotationBuilder mb = builder.getAnnotationBuilder(RequestMapping.class);
			mb.setAttribute("value", new String[] { define.url() });
			mb.setAttribute("method", new RequestMethod[] { RequestMethod.GET });
			return;
		}
		AnnotationBuilder ab = builder.getAnnotationBuilder(ServiceDefine.class);
		if (mapping.method().length == 0 || !isGet(mapping)) {
			ab.setAttribute("readOnly", false);
		}
		String[] urls = mapping.value();
		if (urls.length == 0) {
			urls = mapping.path();
		}
		if (urls.length == 0) {
			urls = new String[] { "/" };
		}
		ab.setAttribute("url", urls[0]);
	}

	private RequestMapping findRequestMapping(ClassBuilder builder, Errors errors) {
		RequestMapping mapping = builder.getAnnotation(RequestMapping.class);
		if (mapping == null) {
			ConstructorBuilder[] list = builder.listConstructors(true);
			if (list.length > 1) {
				return null;
			}
			mapping = list[0].getAnnotation(RequestMapping.class);
			if (mapping == null) {
				return null;
			}
			if (mapping.method().length == 0) {
				list[0].getAnnotationBuilder(RequestMapping.class).setAttribute("method",
						new RequestMethod[] { RequestMethod.GET });
				list[0].create();
			}
		} else if (mapping.method().length == 0) {
			builder.getAnnotationBuilder(RequestMapping.class).setAttribute("method",
					new RequestMethod[] { RequestMethod.GET });
		}
		return mapping;
	}

	private boolean isGet(RequestMapping mapping) {
		for (RequestMethod method : mapping.method()) {
			if (GET_METHOD.indexOf(method) != -1) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onComplete(ClassFactory factory, Errors errors) {
		// do nothing
	}

}
