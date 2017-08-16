package org.prototype.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * sql构建器. <br>
 * 
 * @author lj
 *
 */
public class SQLBuilder {

	/**
	 * SQL语句
	 */
	private StringBuilder builder = new StringBuilder();

	/**
	 * SQL参数
	 */
	private List<Object> params = new ArrayList<>();

	/**
	 * 构造
	 */
	public SQLBuilder() {
		// do nothing;
	}

	/**
	 * 构造
	 * 
	 * @param snippet
	 *            SQL片断
	 */
	public SQLBuilder(String snippet) {
		append(snippet);
	}

	/**
	 * 添加SQL片断
	 * 
	 * @param snippet
	 *            SQL片断
	 * @return 当前对象
	 */
	public SQLBuilder append(String snippet) {
		Assert.notNull(snippet);
		builder.append(snippet);
		if (snippet.charAt(snippet.length() - 1) != ' ') {
			builder.append(' ');
		}
		return this;
	}

	/**
	 * 当参数非null（包括非空字符串及非空集合）时将SQL片断及参数加入
	 * 
	 * @param param
	 *            参数
	 * @param snippet
	 *            SQL片断
	 * @return 当前对象
	 */
	public SQLBuilder appendWhenNotEmpty(Object param, String snippet) {
		return isEmpty(param) ? this : append(param, snippet);
	}

	/**
	 * 添加参数和SQL片断
	 * 
	 * @param param
	 *            参数
	 * @param snippet
	 *            SQL片断
	 * @return 当前对象
	 */
	public SQLBuilder append(Object param, String snippet) {
		Assert.notNull(param);
		params.add(param);
		return append(snippet);
	}

	/**
	 * 当对象为非null（非空字符串，非0长度数组，非空集合时添加SQL片断（不添加object作为参数）
	 * 
	 * @param object
	 *            要判断的参数
	 * @param snippet
	 *            SQL片断
	 * @return 当前对象
	 */
	public SQLBuilder appendSnippetWhenNotEmpty(Object object, String snippet) {
		return isEmpty(object) ? this : append(snippet);
	}

	/**
	 * 当对象为非null（非空字符串，非0长度数组，非空集合时添加SQL片断（不添加object作为参数）
	 * 
	 * @param object
	 *            要判断的参数
	 * @param param
	 *            参数
	 * @param snippet
	 *            SQL片断
	 * @return 当前对象
	 */
	public SQLBuilder appendWhenNotEmpty(Object object, Object param, String snippet) {
		return isEmpty(object) ? this : append(param, snippet);
	}

	/**
	 * 当对象为null（或空字符串，0长度数组，空集合时添加SQL片断（不添加object作为参数）
	 * 
	 * @param object
	 *            要判断的参数
	 * @param snippet
	 *            SQL片断
	 * @return 当前对象
	 */
	public SQLBuilder appendSnippetWhenEmpty(Object object, String snippet) {
		return isEmpty(object) ? this : append(snippet);
	}

	/**
	 * 判断对象是否为null，空字符串、空集合、空数组
	 * 
	 * @param object
	 *            需要判断的对象
	 * @return 为null，空字符串、空集合、空数组时返回true
	 */
	private boolean isEmpty(Object object) {
		if (object == null) {
			return true;
		}
		if (String.class.isInstance(object)) {
			return ((String) object).length() == 0;
		} else if (Collection.class.isInstance(object)) {
			Collection<?> coll = (Collection<?>) object;
			return coll.isEmpty();
		} else if (object.getClass().isArray()) {
			return ((Object[]) object).length == 0;
		} else if (Map.class.isInstance(object)) {
			Map<?, ?> map = (Map<?, ?>) object;
			return map.isEmpty();
		}
		return false;
	}

	/**
	 * 当对象为null（或空字符串，0长度数组，空集合时添加指定参数和SQL片断
	 * 
	 * @param object
	 *            要判断的参数
	 * @param param
	 *            参数
	 * @param snippet
	 *            SQL片断
	 * @return 当前对象
	 */
	public SQLBuilder appendWhenEmpty(Object object, Object param, String snippet) {
		return isEmpty(object) ? this : append(param, snippet);
	}

	/**
	 * 添加SQL执行参数
	 * 
	 * @param param
	 *            执行参数
	 * @return 当前对象
	 */
	public SQLBuilder appendParam(Object... param) {
		params.addAll(Arrays.asList(param));
		return this;
	}

	/**
	 * 获取SQL语句
	 * 
	 * @return SQL语句
	 */
	public String getSql() {
		return builder.toString();
	}

	/**
	 * 获取SQL执行参数
	 * 
	 * @return 执行参数
	 */
	public Object[] getParams() {
		return params.toArray();
	}

}
