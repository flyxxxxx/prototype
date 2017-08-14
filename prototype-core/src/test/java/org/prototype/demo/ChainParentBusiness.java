package org.prototype.demo;

import org.prototype.annotation.Catch;
import org.prototype.annotation.Chain;
import org.prototype.core.Prototype;

import lombok.Getter;

/**
 * 责任链父类
 * @author lj
 *
 */
@Prototype
public class ChainParentBusiness {
	
	@Getter
	protected int value;

	@Catch
	@Chain(value={"business","after"},dynamic=true)
	public void execute(){
		value++;
	}
	
	protected void after(){
		value++;
	}

	protected void executeException(Exception e){
		e.printStackTrace();
	}
}
