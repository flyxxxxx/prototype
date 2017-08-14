package org.prototype.demo;

import org.springframework.scheduling.annotation.Async;

import org.prototype.annotation.Chain;
import org.prototype.core.Prototype;

import lombok.Getter;
/**
 * 异步责任链
 * @author lj
 *
 */
@Prototype
public class AsyncChainBusiness {

	@Getter
	private int value;
	
	private String executeThread;

	public void execute(){
		executeThread=Thread.currentThread().getName();
		asyncChain();
	}

	@Chain("m1")@Async
	private void asyncChain() {
		if(value==0){
			throw new RuntimeException();
		}
	}
	
	void m1(){
		if(Thread.currentThread().getName().equals(executeThread)){
			value=2;
		}else{
			value=1;
		}
	}
}
