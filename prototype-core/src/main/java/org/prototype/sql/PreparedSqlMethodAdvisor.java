package org.prototype.sql;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.prototype.core.ChainOrder;
import org.prototype.core.Errors;
import org.prototype.core.MethodBuilder;
import org.prototype.core.MethodChain;
import org.prototype.core.MethodFilter;
import org.prototype.reflect.ClassUtils;
import org.prototype.reflect.MethodUtils;
import org.prototype.reflect.Property;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 预处理SQL. <br>
 * 支持数组或集合作为SQL中的一个参数（用‘,’字符连接）
 * 
 * @author lj
 *
 */
@Component
public class PreparedSqlMethodAdvisor extends AbstractSqlMethodAdvisor {

	@Override
	public MethodFilter<?> matches(MethodBuilder builder, Errors errors) {
		PreparedSql sql = builder.getAnnotation(PreparedSql.class);
		if (sql == null) {
			return null;
		}
		Class<?>[] types = builder.getParameterTypes();
		if (types.length == 0) {
			errors.add("preparedsql.method.sql", builder.toString());
			return null;
		}
		boolean rs = true;
		if (types.length > 3) {
			errors.add("preparedsql.method.paramtoomore", builder.toString());
			rs = false;
		}
		boolean conn = Connection.class.equals(types[0]);
		if (SQLBuilder.class.equals(types[conn ? 1 : 0])) {
			// TODO
		} else {
			if (!String.class.equals(types[conn ? 1 : 0])) {
				errors.add("preparedsql.method.paramtype", builder.toString(), Integer.toString(conn ? 1 : 0),
						"String");
				rs = false;
			}
			if (types.length == (conn ? 3 : 2) && !Object[].class.equals(types[conn ? 2 : 1])) {
				errors.add("preparedsql.method.paramtype", builder.toString(), Integer.toString(conn ? 2 : 1),
						"Object[]");
				rs = false;
			}
		}
		return rs ? new PreparedSqlMethodFilter(sql) : null;
	}

	//TODO 未解决关联ID的绑定问题
	private class PreparedSqlMethodFilter implements MethodFilter<PreparedSql> {

		private PreparedSql preparedSql;

		public PreparedSqlMethodFilter(PreparedSql preparedSql) {
			this.preparedSql = preparedSql;
		}

		@Override
		public Object doFilter(Object[] args, MethodChain chain) throws Exception {
			Connection connection = getConnection(args);
			boolean conn = Connection.class.equals(args[0]);
			Object object = (Object) args[conn ? 1 : 0];
			String sql = null;
			Object[] parameters = null;
			if (String.class.isInstance(object)) {
				sql = (String) object;
				parameters = (args.length > (conn ? 1 : 0) + 1) ? (Object[]) args[conn ? 2 : 1] : new Object[0];
			} else {
				SQLBuilder builder = (SQLBuilder) object;
				sql = builder.getSql();
				parameters = builder.getParams();
			}
			return execute(chain, connection, sql, parameters);
		}

		private Object execute(MethodChain chain, Connection connection, String sql, Object[] parameters)
				throws Exception {
			try (PreparedStatement ps = connection.prepareStatement(sql)) {
				if (parameters.length > 0) {
					int k = 1;
					for (Object parameter : parameters) {
						setParameter(ps, k++, parameter);
					}
				}
				switch (preparedSql.type()) {
				case SELECT:
					return getSelectResult(ps, chain);
				case INSERT:
					return getInsertResult(ps, chain.getMethod().getReturnType());
				default:
					return getUpdateResult(ps, chain.getMethod().getReturnType());
				}
			}
		}

		private void setParameter(PreparedStatement ps, int index, Object value) throws SQLException {
			if (value == null) {
				ps.setObject(index, null);
				return;
			}
			if (value.getClass().isArray()) {
				ps.setString(index, join((Object[]) value));
			} else if (Collection.class.isInstance(value)) {
				ps.setString(index, join(((Collection<?>) value).toArray()));
			} else {
				ps.setObject(index, value);
			}
		}

		private String join(Object[] value) {
			StringBuilder rs = new StringBuilder();
			for (Object o : value) {
				if (String.class.isInstance(o)) {
					rs.append(",\"" + o + "\"");
				} else if (Number.class.isInstance(o) || o.getClass().isPrimitive()) {
					rs.append("," + o);
				}
			}
			return rs.length() > 0 ? rs.substring(1) : "";
		}

		/**
		 * 插入的结果
		 * 
		 * @param ps
		 *            PreparedStatement
		 * @param returnType
		 *            返回类型
		 * @return ID值
		 * @throws Exception
		 *             异常
		 */
		private Object getInsertResult(PreparedStatement ps, Class<?> returnType) throws Exception {
			if (ps.executeUpdate() > 0 && !void.class.equals(returnType)) {
				ResultSet set = ps.getGeneratedKeys();
				if (set.next()) {
					return getResult(set.getObject(1), returnType);
				}
			}
			return null;
		}

		/**
		 * 结果类型转换
		 * 
		 * @param object
		 *            结果
		 * @param returnType
		 *            方法返回类型
		 * @return 转换后的结果
		 * @throws Exception
		 *             异常
		 */
		private Object getResult(Object object, Class<?> returnType) throws Exception {
			if (returnType.isInstance(object)) {
				return object;
			}
			if (returnType.isPrimitive()) {
				return MethodUtils.getWrapperClass(returnType).getConstructor(String.class)
						.newInstance(object.toString());
			}
			return returnType.getConstructor(String.class).newInstance(object.toString());
		}

		/**
		 * 更新的结果
		 * 
		 * @param ps
		 * @param returnType
		 * @return
		 * @throws Exception
		 */
		private Object getUpdateResult(PreparedStatement ps, Class<?> returnType) throws Exception {
			int rs = ps.executeUpdate();
			if (void.class.equals(returnType)) {
				return null;
			} else if (boolean.class.equals(returnType)) {
				return rs > 0;
			} else if (returnType.isPrimitive()) {
				return getResult(rs, returnType);
			}
			throw new SQLException("Return type must be void/boolean/int/long ");
		}

		private Object getSelectResult(PreparedStatement ps, MethodChain chain) throws Exception {
			Class<?> returnType = chain.getMethod().getReturnType();
			try (ResultSet set = ps.executeQuery()) {
				if (Collection.class.isAssignableFrom(returnType)) {
					return getCollectionResult(set, returnType, chain.getGenericReturnType());
				} else if (Map.class.isAssignableFrom(returnType)) {
					return getMapResult(set, returnType);
				} else if (returnType.isArray()) {
					Collection<?> list = getCollectionResult(set, returnType, chain.getGenericReturnType());
					list.toArray((Object[]) Array.newInstance(returnType.getComponentType(), list.size()));
				} else {
					return getSingleResult(set, returnType);
				}
				return null;
			}
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		private Object getMapResult(ResultSet set, Class<?> returnType) throws Exception {
			ResolvableType type = ResolvableType.forClass(returnType).as(Map.class);
			Class<?> keyType = type.getGeneric(0).resolve();
			Class<?> valueType = type.getGeneric(1).resolve();
			Map map = createMap(returnType);
			while (set.next()) {
				map.put(wrapResult(set.getObject(1), keyType), getNextResult(set, valueType));
			}
			return map;
		}

		private Object wrapResult(Object object, Class<?> resultType) throws NoSuchMethodException,
				InvocationTargetException, IllegalAccessException, InstantiationException {
			if (resultType.isInstance(object)) {
				return object;
			}
			if (resultType.isPrimitive()) {
				resultType = MethodUtils.getWrapperClass(resultType);
			}
			return resultType.getConstructor(String.class).newInstance(object.toString());
		}

		private Map<?, ?> createMap(Class<?> returnType) throws Exception {
			if (returnType.isInterface()) {
				return new LinkedHashMap<>();
			}
			return (Map<?, ?>) returnType.newInstance();
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		private Collection getCollectionResult(ResultSet set, Class<?> returnType, Type type) throws Exception {
			Collection rs = createCollection(returnType);
			ParameterizedType pType = (ParameterizedType) type;
			Class<?> clazz = (Class<?>) pType.getActualTypeArguments()[0];
			while (set.next()) {
				rs.add(getNextResult(set, clazz));
			}
			return rs;
		}

		private Collection<?> createCollection(Class<?> returnType) throws Exception {
			if (Set.class.equals(returnType)) {
				return new HashSet<>();
			} else if (!returnType.isInterface()) {
				return (Collection<?>) returnType.newInstance();
			} else {
				return new ArrayList<>();
			}
		}

		private Object getSingleResult(ResultSet set, Class<?> returnType) throws Exception {
			if (set.next()) {
				return getNextResult(set, returnType);
			}
			return null;
		}

		private Object getNextResult(ResultSet set, Class<?> objectType) throws Exception {
			if (objectType.isPrimitive() || Number.class.isAssignableFrom(objectType)) {
				return getResult(set.getObject(1), objectType);
			}
			Object rs = objectType.newInstance();
			int k = 1;
			for (String name : preparedSql.value()) {
				setProperty(rs, name, set.getObject(k++));
			}
			return rs;
		}

		private void setProperty(Object object, String name, Object value) throws Exception {
			int k = name.indexOf('.');
			if (k == -1) {
				Property property = ClassUtils.properties(object.getClass()).get(name);
				property.setValue(object, value);
			} else {
				Property property = ClassUtils.properties(object.getClass()).get(name.substring(0, k));
				Object v = property.getValue(object);
				if (v == null) {
					v = property.getType().newInstance();
				}
				setProperty(v, name.substring(k + 1), value);
			}
		}

	}

}
