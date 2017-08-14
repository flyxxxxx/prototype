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
package org.prototype.core;

/**
 * 类的适配器. <br>
 * 
 * <pre>
如果实现了一个子类并注入为spring bean，则当框架扫描到一个类时，按以下顺序执行相应方法：
1、调用{@link #beforeLoad(ClassBuilder, Errors)}方法对类进行一些预处理（如果父类不是Object类，则按此顺序优先处理父类），
2、调用{@link MethodAdvisor#matches(MethodBuilder, Errors)}对类的所有方法进行检查，如果匹配则对原方法进行修改.
3、使用ClassLoader加载扫描到的所有类（有指定注解的）.
4、调用{@link FieldInvoker#setValue(Object)}对类的静态成员变量进行处理。
5、调用{@link #onComplete(ClassFactory, Errors)}以完成新类的创建或bean的注册.
 * </pre>
 * 
 * @author flyxxxxx@163.com
 *
 */
public interface ClassAdvisor {

	/**
	 * 修改类的方法之前的处理. <br>
	 * 在此方法中可以给类加注解或添加方法、方法注解等。如果需要修改类，可通过类构建器完成修改，否则可以跳过此类. 对于需要后处理的类，需要将此类缓存起来，然后在{@link #onComplete(ClassFactory, Errors)}方法中处理。
	 * @param builder 类构建接口
	 * @param errors 处理中的错误
	 */
	void beforeLoad(ClassBuilder builder, Errors errors);

	/**
	 * 所有类处理并加载完成后调用. 可以此方法中创建新类并注入bean到Spring上下文<br>
	 * @param factory 用于创建新类的工厂
	 * @param errors 处理中的错误
	 */
	void onComplete(ClassFactory factory, Errors errors);
}
