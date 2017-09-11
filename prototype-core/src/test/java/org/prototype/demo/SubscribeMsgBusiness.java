package org.prototype.demo;

import java.util.concurrent.atomic.AtomicInteger;

import org.prototype.annotation.Subscribe;
import org.springframework.context.ApplicationContext;

import lombok.extern.slf4j.Slf4j;

/**
 * 测试消息处理
 * @author lj
 *
 */
@Subscribe(type = { "test" })
@Slf4j
public class SubscribeMsgBusiness {
	
	/**
	 * 测试用
	 */
	public static AtomicInteger value=new AtomicInteger();
	
	public void onMessage(int value,ApplicationContext context){
		log.info("Reveive msg : "+value+" , beans : "+context.getBeanDefinitionCount());
		SubscribeMsgBusiness.value.incrementAndGet();
	}
	
	public void onMessage(String object){
		log.info("Receive "+object);
	}

}
