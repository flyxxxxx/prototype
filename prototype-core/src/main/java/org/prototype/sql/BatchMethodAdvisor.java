package org.prototype.sql;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import javax.annotation.Resource;

import org.prototype.core.Errors;
import org.prototype.core.MethodAdvisor;
import org.prototype.core.MethodBuilder;
import org.prototype.core.MethodChain;
import org.prototype.core.MethodFilter;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 批处理注解的方法适配. <br>
 * @author lj
 *
 */
@Component
@Slf4j
public class BatchMethodAdvisor implements MethodAdvisor {

	@Resource
	private SqlConfiguration config;
	
	@Override
	public MethodFilter<?> matches(MethodBuilder builder, Errors errors) {
		Batch batch = builder.getAnnotation(Batch.class);
		if (batch == null) {
			return null;
		}
		if (batch.value().length == 0) {//调用的方法要求
			errors.add("batch.required", builder.toString());
			return null;
		}
		boolean rs = true;
		if (hasParameter(builder.getParameterTypes(),Connection.class)) {//调用的方法要求
			errors.add("batch.connection.required", builder.toString());
			return null;
		}
		for (String value : batch.value()) {
			MethodBuilder mi = builder.getClassBuilder().findUniqueMethod(value, errors, Batch.class);
			if (mi == null) {//调用的方法不存在
				errors.add("batch.method.notfound", builder.toString(),value);
				rs = false;
				continue;
			}
			String returnType = mi.getReturnType();//返回值类型要求
			if(!returnType.equals(CollectionIterator.class.getName())&&!returnType.equals(InsertIterator.class.getName())){
				errors.add("batch.method.return", builder.toString(),mi.toString());
				rs = false;
			}
			if(mi.getParameterTypes().length>0){
				errors.add("batch.method.parameters", builder.toString(),mi.toString());
				rs = false;
			}
			BatchSql sql = mi.getAnnotation(BatchSql.class);
			if (sql == null) {//必须有注解BatchSql
				errors.add("batch.batchsql.notfound", builder.toString(),mi.toString());
				rs = false;
			}
		}
		return rs ? new BatchMethodFilter(batch) : null;
	}

	/**
	 * 是否有指定类型参数
	 * @param parameterTypes
	 * @param type
	 * @return
	 */
	private boolean hasParameter(Class<?>[] parameterTypes, Class<?> type) {
		for(Class<?> clazz:parameterTypes){
			if(clazz==type){
				return true;
			}
		}
		return false;
	}

	/**
	 * 批处理注解方法过滤
	 * @author lj
	 *
	 */
	private class BatchMethodFilter implements MethodFilter<Batch> {

		private Batch batch;

		public BatchMethodFilter(Batch batch) {
			this.batch = batch;
		}

		@Override
		public Object doFilter(Object[] args, MethodChain chain) throws Exception {
			if (batch.after()) {
				Object rs = chain.doFilter(args);
				doBatch(chain, args);
				return rs;
			} else {
				doBatch(chain, args);
				return chain.doFilter(args);
			}
		}

		/**
		 * 执行批处理
		 * @param chain 方法链
		 * @param args 参数
		 * @throws Exception 异常
		 */
		private void doBatch(MethodChain chain, Object[] args) throws Exception {
			Connection conn = (Connection) args[0];
			String[] sql = new String[batch.value().length];
			Method[] mi = new Method[sql.length];
			int k = 0;
			for (String value : batch.value()) {
				mi[k] = chain.findUniqueMethod(value, true);
				sql[k] = mi[k].getAnnotation(BatchSql.class).value();
				k++;
			}
			int batchSize = config.getBatchSize();
			PreparedStatement[] ps = new PreparedStatement[sql.length];
			k = 0;
			try {
				for (String s : sql) {
					ps[k] = conn.prepareStatement(s, Statement.RETURN_GENERATED_KEYS);
					prepared(ps[k], (Iterator<?>) mi[k].invoke(chain.getTarget(),new Object[0]), batchSize);
					k++;
				}
			} finally {
				close(ps);
			}
		}

	}

	/**
	 * 关闭PreparedStatement
	 * 
	 * @param ps
	 *            PreparedStatement
	 */
	private static void close(PreparedStatement... ps) {
		for (PreparedStatement p : ps) {
			if (p != null) {
				try {
					p.close();
				} catch (SQLException e) {
					log.warn("Close prepared statement failed", e);
				}
			}
		}
	}

	/**
	 * 批量处理
	 * 
	 * @param ps
	 *            PreparedStatement
	 * @param it
	 *            循环
	 * @param batchSize
	 *            批处理大小
	 * @throws SQLException
	 */
	private static void prepared(PreparedStatement ps, Iterator<?> it, int batchSize) throws SQLException {
		int total = 0;
		while (it.hasNext()) {
			Object[] value = (Object[]) it.next();
			for (int i = 0, k = value.length; i < k; i++) {
				ps.setObject(i + 1, value[i]);
			}
			ps.addBatch();
			total++;
			if (total % batchSize == 0) {
				batch(ps, it, total - batchSize);
			}
		}
		int m = total % batchSize;
		if (m > 0) {
			batch(ps, it, total - m);
		}
	}

	/**
	 * 执行批处理并获取可能的结果
	 * 
	 * @param ps
	 * @param it
	 * @param start
	 * @throws SQLException
	 */
	private static void batch(PreparedStatement ps, Iterator<?> it, int start) throws SQLException {
		ps.executeBatch();
		if (it instanceof InsertIterator) {
			saveIds(ps, (InsertIterator<?>) it, start);
		}
	}

	/**
	 * 保存结果ID
	 * 
	 * @param ps
	 * @param it
	 * @param start
	 * @throws SQLException
	 */
	private static void saveIds(PreparedStatement ps, InsertIterator<?> it, int start) throws SQLException {
		ResultSet rs = ps.getGeneratedKeys();
		int k = 0;
		try {
			while (rs.next()) {
				it.setId(start + (k++), rs.getObject(1));
			}
		} catch (InvocationTargetException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}
