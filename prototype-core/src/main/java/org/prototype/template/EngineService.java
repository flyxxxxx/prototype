package org.prototype.template;

import java.io.File;
import java.net.URL;
import java.util.Map;

/**
 * 引擎服务
 * 
 * @author 李劲
 * 
 */

public interface EngineService {

	/**
	 * 是否有指定类型的引擎
	 * @param type 引擎类型
	 * @return 有指定引擎类型返回true
	 */
	boolean hasEngine(String type);

	/**
	 * 渲染
	 * @param type 模板类型（引擎名称）
	 * @param template
	 *            模板输入流
	 * @param encoding 文件编码
	 * @param properties
	 *            属性变量
	 * @return 未找到模版实现则返回null
	 */
	String render(String type,URL template,String encoding, Map<String, Object> properties);

	/**
	 * 
	 * 渲染
	 * @param type 模板类型（引擎名称）
	 * @param template
	 *            模版名称（由文件管理模块提供的模版）
	 * @param properties
	 *            属性变量
	 * @return 渲染后的内容
	 */
	String render(String type,String template, Map<String, Object> properties);

	/**
	 * 渲染
	 * @param type 模板类型（引擎名称）
	 * @param template
	 *            模版文件(UTF-8格式)
	 * @param encoding 文件编码
	 * @param properties
	 *            属性变量
	 * @return 渲染后的内容
	 */
	String render(String type,File template,String encoding, Map<String, Object> properties);

}
