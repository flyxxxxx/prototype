package org.prototype.sql;

public @interface Call {
	String sql();
	String param();
	String result();
	int[] outParamIndex() default {};
	int[] outParamSqlType() default {};
}
