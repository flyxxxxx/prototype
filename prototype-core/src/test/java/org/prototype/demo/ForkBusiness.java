package org.prototype.demo;

import java.util.concurrent.atomic.AtomicInteger;

import org.prototype.annotation.Fork;
import org.prototype.core.Prototype;

import lombok.Getter;

/**
 * 并发业务
 * 
 * @author lj
 *
 */
@Prototype
public class ForkBusiness {

	@Getter
	private transient AtomicInteger value = new AtomicInteger();

	@Fork(value = { "m1", "m2" })
	public void fork() {
		value.incrementAndGet();
	}

	void m1(TestService ts) throws InterruptedException {
		Thread.sleep(1000);//这是为了演示用，因此不调用RPC方法
		value.incrementAndGet();
	}

	void m2() throws InterruptedException {
		Thread.sleep(1000);//这是为了演示用，因此不调用RPC方法
		value.incrementAndGet();
	}
}
