package org.prototype.demo;


import org.prototype.annotation.Chain;
import org.prototype.core.Prototype;
import org.springframework.beans.factory.annotation.Value;

import lombok.Getter;

@Prototype
public class ChainBusiness {
	
	@Value("${spring.datasource.driverClassName}")
	@Getter
	private static String driverClassName;
	
	@Getter
	private int value;

	@Chain(value={"a1","a2"},after=true)
	public void execute(){
		value+=1;
	}

	void a1(){
		value+=1;
	}
	void a2(TestService ts){
		ts.call();
		value+=1;
	}
	
	@Chain(value = { "a3" })
	public void execute1(){
		value+=1;
	}
	
	boolean a3(){
		return false;
	}
	
	public void exec1(){
		a1();
		a2(null);
		value++;
	}

	@Chain(value = { "a4" })
	public void execute2(String value){}
	
	void a4(String value){
		if(value!=null){
			this.value++;
		}
	}
}
