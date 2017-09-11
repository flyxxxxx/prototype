package org.prototype.template;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.prototype.core.Errors;
import org.prototype.core.MethodAdvisor;
import org.prototype.core.MethodBuilder;
import org.prototype.core.MethodChain;
import org.prototype.core.MethodFilter;
import org.prototype.javassist.CtMethodUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Template注解处理
 * 
 * @author lj
 *
 */
@Component
public class TemplateAdvisor implements MethodAdvisor {

	@Resource
	private EngineService es;
	@Resource
	private ApplicationContext applicationContext;

	@Override
	public MethodFilter<?> matches(MethodBuilder accessor, Errors errors) {
		Template template = accessor.getAnnotation(Template.class);
		if (template == null) {
			return null;
		}
		boolean rs = true;
		if (accessor.getParameterTypes().length == 0) {
			errors.add("template.param.required", accessor.toString());
			rs = false;
		}
		if (!"java.lang.String".equals(accessor.getReturnType())) {
			errors.add("template.returntype.required", accessor.toString());
			rs = false;
		}
		if ("".equals(template.value()) && "".equals(template.file())) {
			errors.add("template.valueorfile.required", accessor.toString());
			rs = false;
		}
		if(template.file().startsWith("classpath:")){
			
		}
		if (!es.hasEngine(template.engine())) {
			errors.add("template.engine", accessor.toString(), template.engine());
			rs= false;
		}
		return rs?new TemplateMethodFilter(template):null;
	}

	/**
	 * 获取参数名与值映射
	 * @param args  参数值
	 * @param parameterNames 参数名
	 * @return
	 */
	private static Map<String, Object> getParametersAsMap(Object[] args, String[] names) {
		Map<String, Object> map = new HashMap<>();
		for (int i = 0, k = names.length; i < k; i++) {
			map.put(names[i], args[i]);
		}
		return map;
	}
	
	/**
	 * 模板方法过滤
	 * @author lj
	 *
	 */
	private class TemplateMethodFilter implements MethodFilter<Template>{

		private Template template;

		public TemplateMethodFilter(Template template) {
			this.template=template;
		}

		@Override
		public Object doFilter(Object[] args, MethodChain chain) throws Exception {
			chain.doFilter(args);// 执行原方法，但忽略其结果
			Map<String, Object> map = getParametersAsMap(args, CtMethodUtils.getMethodParamNames(chain.getMethod()));
			if (template.value().length() > 0) {// 模板内容处理
				if (template.value().startsWith("key:")) {
					return es.render(template.engine(),
							applicationContext.getBean(TemplateService.class).getTemplate(template.value().substring(4)),
							map);
				}
				return es.render(template.engine(), template.value(), map);
			} else if (template.file().startsWith("/")) {// 文件模板
				return es.render(template.engine(), new File(template.file()),template.encoding(), map);
			} else if (template.file().startsWith("classpath:")) {// classpath模板文件
				return es.render(template.engine(),
						new ClassPathResource(template.file().substring("classpath:".length())).getURL(), template.encoding(),map);
			} else {// 相对路径模板文件
				String name = chain.getTarget().getClass().getName();
				int k = name.lastIndexOf('.');
				String path = k == -1 ? "" : name.substring(0, k).replace('.', '/');
				return es.render(template.engine(), new ClassPathResource(path + "/" + template.file()).getURL(),template.encoding(), map);
			}
		}
		
	}

}
