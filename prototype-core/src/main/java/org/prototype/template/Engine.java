package org.prototype.template;

import java.util.Map;

/**
 * 模版引擎
 * 
 * @author 李劲
 * 
 */
public interface Engine {
	
	/**
	 * 引擎名，必须在spring配置中唯一
	 * @return 引擎名
	 */
	String getType();
	/**
	 * 渲染
	 * 
	 * @param template
	 *            模版URL
	 * @param properties
	 *            属性变量
	 * @return 未找到模版实现则返回null
	 */
	String render(String template,
			Map<String, Object> properties);
}
