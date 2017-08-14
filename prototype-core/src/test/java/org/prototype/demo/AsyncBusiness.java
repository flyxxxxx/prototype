package org.prototype.demo;

import org.springframework.scheduling.annotation.Async;

import org.prototype.core.Prototype;

import lombok.Getter;

/**
 * 异步业务
 * @author lj
 *
 */
@Prototype
public class AsyncBusiness {
	
	@Getter
	private int value;
	
	private String executeThread;

	public void execute(){
		executeThread=Thread.currentThread().getName();
		async();
	}

	@Async
	private void async() {
		if(Thread.currentThread().getName().equals(executeThread)){
			value=2;
		}else{
			value=1;
		}
	}
}
