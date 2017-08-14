package org.prototype.demo;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.prototype.core.Prototype;

import lombok.Getter;

@Prototype
public class HystrixBusiness {
	
	@Getter
	private int value;

	@HystrixCommand
	public void execute(){
		throw new RuntimeException();
	}

	@HystrixCommand(ignoreExceptions=RuntimeException.class)
	public void execute1(){
		throw new RuntimeException();
	}
	
	//fallback
	void execute1Fallback(){
		value++;
	}
	
	//fallback
	void executeFallback(){
		value++;
	}
}
