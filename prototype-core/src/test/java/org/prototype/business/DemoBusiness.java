package org.prototype.business;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicInteger;

import org.prototype.demo.TestService;

import lombok.Getter;

public class DemoBusiness extends Business{
	@Getter
	private AtomicInteger value=new AtomicInteger();

	public void validate(){
		value.incrementAndGet();
	}
	public void business(TestService ts,Connection connection){
		ts.call(); 
		value.incrementAndGet();
	}
	public void after(){
		value.incrementAndGet();
	}

	void async(Connection connection){
		value.incrementAndGet();
	}
	void async(TestService ts){
		ts.call();
		value.incrementAndGet();
	}
}
