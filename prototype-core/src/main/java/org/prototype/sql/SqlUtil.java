package org.prototype.sql;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.prototype.core.PrototypeStatus;
import org.prototype.core.PrototypeStatus.TransactionStatus;
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

/**
 * SQL处理工具
 * @author flyxxxxx@163.com
 *
 */
@Slf4j
class SqlUtil {
	
	/**
	 * 获取分区SQL表达式
	 * @param partition 分区注解
	 * @return SQL表达式
	 */
	static Pattern getPartitionPattern(Partition partition){
		return partition == null ? null : Pattern.compile(" " + partition.value() + "\\s*(=|(in)).*\\s*");		
	}

	/**
	 * 设定分区值
	 * @param pattern 表达式
	 * @param sql SQL
	 * @param parameters 参数
	 */
	static void setPartition(Pattern pattern,String sql, Object[] parameters) {
		if (pattern == null) {
			return;
		}
		TransactionStatus trans = PrototypeStatus.getStatus().getTransaction();
		Assert.notNull(trans);
		if (trans.getPartion() != null) {
			return;
		}
		Matcher matcher = pattern.matcher(sql);
		if (!matcher.find()) {
			return;
		}
		String str = sql.substring(matcher.start(), matcher.end()).trim();
		int m = str.indexOf('=');
		int n = m == -1 ? str.indexOf(" in") : -1;
		boolean in = m == -1;
		String value = in?str.substring(n+3):str.substring(m+1);
		if (value.indexOf('?') == -1) {
			trans.setPartion(value);
		} else if(parameters!=null){
			int index = countParameters(sql.substring(0, matcher.start()));
			Object parameter = parameters[index];
			if (parameter == null) {
				return;
			}
			if (!in) {
				trans.setPartion(parameter.toString());
			} else if (Collection.class.isInstance(parameter)) {// where in
				trans.setPartion(((Collection<?>) parameter).iterator().next().toString());
			} else {// where in
				String v = parameter.toString().split("[,]")[0];
				trans.setPartion(v.charAt(0) == '\'' ? (v.substring(1, v.length() - 1)) : v);
			}
		}
		log.debug("Prepared sql : {} , use partition {}", sql, trans.getPartion());
	}


	private static int countParameters(String sql) {
		int count = 0;
		int k = 0;
		while ((k = sql.indexOf('?', k)) != -1) {
			count++;
			k++;
		}
		return count;
	}

}
