package org.prototype;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 无Spring Boot环境的测试
 * @author lj
 *
 */
public class NoSpringBootTest {
	
	private ClassPathXmlApplicationContext context;

	//@Before
	public void init(){
		context=new ClassPathXmlApplicationContext("context.xml");
	}
	
	//@After
	public void destroy(){
		if(context!=null){
			context.destroy();
		}
	}
	
	//@Test
	public void test(){
		
	}
	
}
