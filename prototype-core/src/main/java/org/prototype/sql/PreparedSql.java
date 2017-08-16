package org.prototype.sql;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 豫处理的SQL执行. <br>
 * 
 * <pre>
 * 例1：
 * void method1(Connection conn , ...){
 *   String sql="select id,name from users where flag=?";
 *   Object[] parameters=new Object[]{1};
 *   List&lt;User&gt; users=getUsers(sql,parameters};
 *   ...
 * }
 * &#064;PreparedSql({"id","name"})
 * List&lt;User&gt; getUsers(String sql,Object[] parameters){return null;};
 * 例2：
 * void method2(Connection conn , ...){
 *   String sql="select flag,count (id), from users group by flag";
 *   List&lt;Object&gt; users=getUsers(sql};
 *   ...
 * }
 * &#064;PreparedSql
 * List&lt;Object&gt; group(String sql,){ return null;}//如果只是简单的统计，返回值也可以是int/long/Integer/Long
 * </pre>
 * @author lj
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PreparedSql {
	
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
