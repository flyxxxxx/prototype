package org.prototype.demo;

import java.util.concurrent.atomic.AtomicInteger;

import org.prototype.annotation.OverloadAsync;
import org.prototype.core.Prototype;

import lombok.Getter;

/**
 * 
 * @author lj
 *
 */
@Prototype
public class OverloadAsyncBusiness {
	
	@Getter
	private AtomicInteger value=new AtomicInteger();
	
	private String threadName;

	@OverloadAsync(value = { "async" },after=true)
	public void execute(){
		threadName=Thread.currentThread().getName();
	}
	
	void async(){
		if(!Thread.currentThread().getName().equals(threadName)){
			value.incrementAndGet();
		}
	}
	void async(TestService ts){
		ts.call();
		if(!Thread.currentThread().getName().equals(threadName)){
			value.incrementAndGet();
		}
	}
}
