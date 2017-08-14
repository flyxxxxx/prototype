package org.prototype.web;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.ModelAndView;

import lombok.Data;

@RestController
public class AsyncController {
	@RequestMapping("/async/test")
	@ResponseBody
	public Callable<String> callable() {
		// 这么做的好处避免web server的连接池被长期占用而引起性能问题，
		// 调用后生成一个非web的服务线程来处理，增加web服务器的吞吐量。
		return new Callable<String>() {
			@Override
			public String call() throws Exception {
				Thread.sleep(3 * 1000L);
				return "小单 - " + System.currentTimeMillis();
			}
		};
	}
	   @RequestMapping(value="/push",produces="text/event-stream") //①
	    public @ResponseBody String push(){
	         Random r = new Random();
	        try {
	                Thread.sleep(5000);
	        } catch (InterruptedException e) {
	                e.printStackTrace();
	        }   
	        return "data:Testing 1,2,3" + r.nextInt() +"\n\n";
	    }

	@RequestMapping("/async/test1")
	@ResponseBody
	public DeferredResult<Object> callable1() {
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		CompletableFuture.supplyAsync(new Supplier<Object>() {

			@Override
			public String get() {
				// TODO Auto-generated method stub
				return "fdsa";
			}

		}).whenCompleteAsync((result, throwable) -> deferredResult.setResult(result));
		return deferredResult;
	}

	@RequestMapping("/async/test2")
	@ResponseBody
	public DeferredResult<ModelAndView> callable2() {
		DeferredResult<ModelAndView> deferredResult = new DeferredResult<>();
		CompletableFuture.supplyAsync(new Supplier<ModelAndView>() {

			@Override
			public ModelAndView get() {
				Map<String, Object> map=new HashMap<>();
				map.put("user", new User(5,"321a"));
				return new ModelAndView("first",map);
			}

		}).whenCompleteAsync((result, throwable) -> deferredResult.setResult(result));
		return deferredResult;
	}
	
	@Data public static class User{
		public User(int id, String name) {
			this.id=id;
			this.name=name;
		}
		private Integer id;
		private String name;
	}
}
