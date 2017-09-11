package org.prototype.sql;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 批处理的SQL. <br>
 * 与{@link Batch}注解配合使用。一个方法加此注解，要求方法返回值类型为{@link CollectionIterator}或{@link InsertIterator}. <br>
 * 与{@link Partition}注解在同一方法中使用，可以对批处理进行数据分区处理（需要多数据源支持）；一个线程中只能有一个数据分区.
 * @see Batch
 * @author lj
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BatchSql {

	/**
	 * 批处理的SQL语句
	 * 
	 * @return SQL语句
	 */
	String value();
}
