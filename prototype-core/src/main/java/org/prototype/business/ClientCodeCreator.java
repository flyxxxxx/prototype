package org.prototype.business;

/**
 * 客户端代码创建器
 * @author lj
 *
 */

interface ClientCodeCreator {
	
	/**
	 * 创建代码的路径环境变量
	 */
	final String PROTOTYPE_CODE_PATH="prototype.code.path";
	
	/**
	 * 创建客户端代码
	 * @param interfaceType 客户端接口
	 * @param codePath 保存路径
	 */
	void create(Class<?> interfaceType,String codePath);

}
