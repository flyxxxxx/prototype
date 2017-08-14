package org.prototype.hibernate;

import org.prototype.sql.StatementType;

public @interface HibernateQuery{
	
	/**
	 * 语句类型（用于启动前检查）
	 * @return 语句类型
	 */
	StatementType type() default StatementType.SELECT;
	/**
	 * select语句对应结果的属性名（支持a.b.c形式）
	 * @return 属性名
	 */
	String[] value () default {};
}
