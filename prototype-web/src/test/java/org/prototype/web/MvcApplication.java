package org.prototype.web;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.prototype.EnablePrototype;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;

@EnablePrototype({"org.prototype.demo"})
@SpringBootApplication
//@EnableAsync
@EntityScan(basePackages="org.prototype.demo")
public class MvcApplication {

    
    public static void main(String[] args){
    	SpringApplication.run(MvcApplication.class, args);
    }
	
	@Bean
	@Primary
	public ExecutorService executeService() {
		return new ThreadPoolExecutor(2, 5, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3),
				new ThreadPoolExecutor.DiscardPolicy());
	}

	/**
	 * 生产情况下应该是最大线程数较低（如几个），队列数量数百.
	 * @return
	 */
	@Bean
	public ExecutorService secondExecutor() {
		return new ThreadPoolExecutor(2, 20, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3),
				new ThreadPoolExecutor.DiscardPolicy());
	}
}
