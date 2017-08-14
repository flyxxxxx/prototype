package org.prototype.demo;

import org.prototype.annotation.Decision;
import org.prototype.core.Prototype;

import lombok.Getter;

/**
 * 决策
 * @author lj
 *
 */

@Prototype
public class DecisionBusiness {
	
	@Getter
	private transient int value=0;

	@Decision({"a1","a2"})
	public boolean execute(){
		//do nothing;
		return true;
	}
	
	void a1(){
		value=1;
	}
	
	void a2(){
		value=2;
	}

	@Decision({"a1","a2"})
	public int execute1(){
		return 1;
	}

	@Decision({"a1","a2"})
	public String execute2(){
		return "a1";
	}
	
}
