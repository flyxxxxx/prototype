package org.prototype.template;

/**
 * 模板服务
 * @author lj
 *
 */
public interface TemplateService {
	/**
	 * 获取模板内容
	 * @param key 关键字
	 * @return 模板内容
	 */
	String getTemplate(String key);
}
