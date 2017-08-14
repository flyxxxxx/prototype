package org.prototype.javassist;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.prototype.core.MethodChain;
import org.prototype.core.MethodFilter;
import org.prototype.reflect.MethodUtils;
import org.springframework.aop.support.AopUtils;

import lombok.Getter;

/**
 * 方法调用链实现. <br>
 * @author lj
 *
 */
class MethodChainImpl implements MethodChain{
	
	private List<MethodFilter<?>> filters;
	
	private int index;
	
	@Getter
	private Object target;
	
	@Getter
	private Method method;
	
	private Method implMethod;

	private int max;
	
	public MethodChainImpl(Object target,Method method,Method implMethod,List<MethodFilter<?>>  filters) {
		this.target=target;
		this.method=method;
		this.implMethod=implMethod;
		this.filters=filters;
		this.max=filters.size();
	}

	@Override
	public Object doFilter(Object[] args) throws Exception {
		if(index<max){
			return filters.get(index++).doFilter(args, this);
		}else if(implMethod!=null){
			implMethod.setAccessible(true);
			return implMethod.invoke(target, args);
		}else{
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Method findUniqueMethod(String methodName, boolean required) {
		return MethodUtils.findUniqueMethod(target.getClass(), methodName, required);
	}
	
	@Override
	public Method findOverloadMethod(String methodName,Class<?> parameterType){
		return MethodUtils.findOverloadMethod(target.getClass(), methodName, parameterType);
	}

	@Override
	public List<Method> findMethods(String methodName) {
		return MethodUtils.findMethods(target.getClass(), methodName);
	}

	@Override
	public String toString(){
		List<String> list=new ArrayList<>();
		for(MethodFilter<?> filter:filters ){
			list.add(AopUtils.getTargetClass(filter).getName());
		}
		return list.toString();
	}

	@Override
	public Type getGenericReturnType() {
		return implMethod==null?method.getGenericReturnType():implMethod.getGenericReturnType();
	}

}
