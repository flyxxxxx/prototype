package org.prototype.business;

import java.lang.annotation.Annotation;

import lombok.Getter;

/**
 * ExecuteChain测试实现
 * @author lj
 *
 */
public class ExecuteChainImpl implements ExecuteChain{

	private Business target;
	
	@Getter
	private boolean async;

	public ExecuteChainImpl(Business target) {
		this.target=target;
	}

	@Override
	public void doChain() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Service getService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTarget(Object target) {
		this.target=(Business) target;
	}

	@Override
	public Object getTarget() {
		return target;
	}

	@Override
	public Object[] getParams() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setResult(Object result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setResultType(int resultType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addValidateError(String error) {
		target.addValidateError(error);
	}

	@Override
	public boolean isValidated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
		// TODO Auto-generated method stub
		return null;
	}

}
