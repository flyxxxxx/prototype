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

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.prototype.PrototypeInitializer;

import lombok.Getter;

/**
 * 为加载过程中的处理错误记录
 * 
 * @author flyxxxxx@163.com
 *
 */
public class Errors {

	/**
	 * 资源
	 */
	private List<ResourceBundle> resources = new ArrayList<>();

	// TODO 扩展时，对外部消息资源的处理

	/**
	 * 错误消息
	 */
	@Getter
	private List<String> messages = new ArrayList<>();

	public Errors(PrototypeInitializer initializer) {
		resources.add(ResourceBundle.getBundle("prototype"));
		for (String name : initializer.getMessages()) {
			name = name.trim();
			if (name.length() > 0) {
				resources.add(ResourceBundle.getBundle(name));
			}
		}
	}

	/**
	 * 添加错误
	 * 
	 * @param key
	 *            消息关键字
	 * @param args
	 *            参数
	 */
	public void add(String key, String... args) {
		for (ResourceBundle bundle : resources) {
			if (!bundle.containsKey(key)) {
				continue;
			}
			String msg = bundle.getString(key);
			for (int i = 0, k = args.length; i < k; i++) {
				msg = msg.replace("{" + i + "}", args[i]);
			}
			messages.add(msg);
			return;
		}
		throw new NullPointerException("Resource key '" + key + "' not found");
	}

	/**
	 * 是否包含错误
	 * 
	 * @return 包含错误返回true
	 */
	public boolean hasError() {
		return !messages.isEmpty();
	}
}
