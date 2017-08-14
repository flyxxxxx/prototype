package org.prototype.reflect;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lombok.Getter;

/**
 * 成员变量属性. <br>
 * @author lj
 *
 */
public class Property {

	@Getter
	private Field field;
	private PropertyDescriptor descriptor;
	@Getter
	private String name;

	void setField(Field field) {
		this.field = field;
		field.setAccessible(true);
		this.name=field.getName();
	}

	Property(PropertyDescriptor descriptor) {
		this.name=descriptor.getName();
		this.descriptor = descriptor;
		if(this.descriptor.getWriteMethod()!=null){
			this.descriptor.getWriteMethod().setAccessible(true);
		}
		if(this.descriptor.getReadMethod()!=null){
			this.descriptor.getReadMethod().setAccessible(true);
		}
	}

	Property(Field field) {
		setField(field);
	}
	
	public boolean isReadOnly(){
		if(field==null&&descriptor.getWriteMethod()==null){
			return true;
		}
		return false;
	}

	public Class<?> getType() {
		if (descriptor != null) {
			Method method = descriptor.getReadMethod();
			if (method != null) {
				return method.getReturnType();
			}
		}
		if (field != null) {
			return field.getType();
		}
		return descriptor.getWriteMethod().getParameterTypes()[0];
	}

	public void setValue(Object object, Object value)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (descriptor == null) {
			field.set(object, value);
			return;
		}
		Method method = descriptor.getWriteMethod();
		if (method != null) {
			method.invoke(object, value);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public Object getValue(Object object)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (descriptor == null) {
			return field.get(object);
		}
		Method method = descriptor.getReadMethod();
		if (method != null) {
			return method.invoke(object);
		}
		throw new UnsupportedOperationException();
	}

}
