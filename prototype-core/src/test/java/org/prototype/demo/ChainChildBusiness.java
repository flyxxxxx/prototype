package org.prototype.demo;

import lombok.extern.slf4j.Slf4j;

/**
 * 责任链子类
 * @author lj
 *
 */
@Slf4j
public class ChainChildBusiness extends ChainParentBusiness {

	void business(){
		if(value==0){
			throw new RuntimeException ("error");
		}else{
			value++;//这里未执行
		}
	}
	
	void executeException(RuntimeException e){
		log.info("Catch exception :"+e.getMessage());
		if(e!=null){
			value++;//这里执行了
		}
	}
}
