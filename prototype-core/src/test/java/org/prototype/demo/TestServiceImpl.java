package org.prototype.demo;

import org.springframework.stereotype.Service;

/**
 * 测试服务实现
 * @author flyxxxxx@163.com
 *
 */
@Service
public class TestServiceImpl implements TestService{
	
	private int value;

	public TestServiceImpl(){
		//do nothing
	}
	
	public TestServiceImpl(int value){
		this.value=value;
	}

	@Override
	public int call() {
		return value;
	}

}
