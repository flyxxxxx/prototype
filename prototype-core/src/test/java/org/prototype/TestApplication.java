package org.prototype;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.prototype.business.Performance;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 测试
 * @author lj
 *
 */
@SpringBootApplication
@EnablePrototype
@Performance(1)
public class TestApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestApplication.class, args);
	}

	/**
	 * 为了模拟测试设置的值较小。
	 * 建议真实应该是最大线程数和队列数可以是300/600，根据生产需要调整。
	 * @return
	 */
	@Bean
	@Primary
	public ExecutorService executeService() {
		return new ThreadPoolExecutor(2, 2, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3),
				new ThreadPoolExecutor.DiscardPolicy());
	}

	/**
	 * 生产情况下应该是最大线程数较低（如几个），队列数量数百.
	 * @return
	 */
	@Bean
	public ExecutorService secondExecutor() {
		return Executors.newFixedThreadPool(10, new ThreadFactoryImpl(2));
	}

	private class ThreadFactoryImpl implements ThreadFactory {

		private int index;

		public ThreadFactoryImpl(int index) {
			this.index = index;
		}

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(new ThreadGroup("pool group" + index), r);
		}

	}
}
