package org.prototype;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prototype.annotation.Message;
import org.prototype.business.Business;
import org.prototype.business.BusinessExecutor;
import org.prototype.business.DemoBusiness;
import org.prototype.business.GetResult;
import org.prototype.business.InitTarget;
import org.prototype.business.Service;
import org.prototype.business.ServiceClassAdvisor.ServiceNameGenerator;
import org.prototype.business.ServiceDemo1;
import org.prototype.business.ServiceDemo2;
import org.prototype.business.ServiceParamCreate;
import org.prototype.business.api.JavaApiCreator.JavaApiNameGenerator;
import org.prototype.business.api.JavascriptApiCreator.Validation;
import org.prototype.business.api.JsonApiCreator.JsonApi;
import org.prototype.core.ClassScaner;
import org.prototype.demo.AsyncBusiness;
import org.prototype.demo.AsyncChainBusiness;
import org.prototype.demo.CatchBusiness;
import org.prototype.demo.ChainBusiness;
import org.prototype.demo.ChainChildBusiness;
import org.prototype.demo.DecisionBusiness;
import org.prototype.demo.ForkBusiness;
import org.prototype.demo.HystrixBusiness;
import org.prototype.demo.MsgBusiness;
import org.prototype.demo.OverloadAsyncBusiness;
import org.prototype.demo.ParameterInjectBusiness;
import org.prototype.demo.SubscribeMsgBusiness;
import org.prototype.demo.TemplateBusiness;
import org.prototype.demo.TransactionalBusiness;
import org.prototype.sql.PreparedBusiness;
import org.prototype.util.TodayZeroBusiness;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 应用程序测试
 * 
 * @author flyxxxxx@163.com
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@Slf4j
public class ApplicationTest {
	private final static ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.enable(JsonGenerator.Feature.IGNORE_UNKNOWN);
		mapper.setSerializationInclusion(Include.NON_NULL);
	}

	@LocalServerPort
	private int port = 0;

	@Resource
	private ApplicationContext context;

	@Resource
	private BusinessExecutor executor;

	@Resource
	private ClassScaner scaner;

	@Resource
	private ServiceNameGenerator generator;

	@Resource
	private JavaApiNameGenerator apiGenerator;

	/**
	 * 业务注解测试
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBusiness() throws Exception {
		DemoBusiness demo = new DemoBusiness();
		demo.execute();
		Thread.sleep(300);
		Assert.assertEquals(5, demo.getValue().get());
		Field field = Business.class.getDeclaredField("result");
		field.setAccessible(true);
		Assert.assertEquals(Business.SUCCESS, field.get(demo));
		field = Business.class.getDeclaredField("reason");
		field.setAccessible(true);
		Assert.assertNull(field.get(demo));
	}
	
	@Test
	public void testBaseConfig()throws Exception {
		PrototypeConfig config=context.getBean(PrototypeConfig.class);
		Assert.assertEquals(3, config.getApi().size());
	}

	@Test
	public void testServiceParamCreate() throws Exception {
		Service service = executor.getService(ServiceParamCreate.class);
		ServiceParamCreate.testType(service.getParamType());
	}

	@Test
	public void testData() throws Exception {
		for (Service service : executor.listServices(false)) {
			executor.getApi("java", service.getType().getName());
		}
		for (Service service : executor.listServices(true)) {
			if (DataTest.class.isAssignableFrom(service.getType())) {
				testData(service,Class.forName(apiGenerator.newServiceName(service.getType().getPackage().getName())));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void testData(Service service,Class<?> clazz) throws Exception {
		log.info("Test " + service.getType());
		DataTest test = (DataTest) service.getType().newInstance();
		test.checkParamType(service.getParamType());
		test.checkResultType(service.getResultType());
		Object param = service.getParamType().newInstance();
		test.initParam(param);
		Object result = executor.execute(service.getType(), new Object[] { param });
		test.checkResult(result);
		assertImplement(clazz);
		test.checkJavaApi(service, clazz);
		JsonApi api=(JsonApi) executor.getApi("json", service.getType().getName());
		log.info(service.getType().getName()+" json api : "+mapper.writeValueAsString(api));
		test.checkJsonApi(api);
		List<Validation> validations = (List<Validation>) executor.getApi("javascript", service.getType().getName());
		log.info(service.getType().getName()+" javascript api : "+mapper.writeValueAsString(validations));
		test.checkJavascriptApi(validations);
	}

	/**
	 * 确认实现类的接口一致
	 * @param clazz 实现类
	 * @throws Exception
	 */
	private void assertImplement(Class<?> clazz) throws Exception{
		Class<?> type=clazz.getInterfaces()[0];
		for(Method method:type.getDeclaredMethods()){
			Method method2=clazz.getMethod(method.getName(), method.getParameterTypes());
			Assert.assertEquals(method.getReturnType(), method2.getReturnType());
		}
	}

	@Test
	public void testGetResult() throws Exception {
		GetResult.main(new String[0]);
	}

	@Test
	public void testInitTarget() throws Exception {
		InitTarget.main(new String[0]);
	}

	/**
	 * 责任链
	 */
	@Test
	public void testChain() {
		ChainBusiness business = new ChainBusiness();
		business.execute();
		Assert.assertNotNull(ChainBusiness.getDriverClassName());
		Assert.assertEquals(3, business.getValue());
		business.execute1();
		Assert.assertEquals(3, business.getValue());
		business.execute2("hello");
		Assert.assertEquals(4, business.getValue());
	}

	/**
	 * 父子类责任链
	 */
	@Test
	public void testChainExtends() {
		ChainChildBusiness busi = new ChainChildBusiness();
		busi.execute();
		Assert.assertEquals(1, busi.getValue());
	}

	/**
	 * 测试当天0点
	 */
	@Test
	public void testTodayZero(){
		TodayZeroBusiness busi=new TodayZeroBusiness();
		busi.business();
	}
	/**
	 * 重载的异步
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testOverloadAsync() throws InterruptedException {
		OverloadAsyncBusiness busi = new OverloadAsyncBusiness();
		busi.execute();
		Thread.sleep(500);
		Assert.assertEquals(2, busi.getValue().get());
	}

	/**
	 * 事务管理
	 */
	@Test
	public void testTransactional() {
		TransactionalBusiness business = new TransactionalBusiness();
		business.execute();
		Assert.assertNotNull(business.getCurrent());
	}

	/**
	 * 测试注入参数
	 */
	@Test
	public void testParameterInject() {
		ParameterInjectBusiness pi = new ParameterInjectBusiness();
		pi.execute();
	}

	/**
	 * 异步测试
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testAsync() throws InterruptedException {
		AsyncBusiness busi = new AsyncBusiness();
		busi.execute();
		Thread.sleep(500);
		Assert.assertEquals(1, busi.getValue());
	}

	/**
	 * 异步+责任链
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testAsyncChain() throws InterruptedException {
		AsyncChainBusiness busi = new AsyncChainBusiness();
		busi.execute();
		Thread.sleep(500);
		Assert.assertEquals(1, busi.getValue());
	}

	/**
	 * 消息处理
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testMsg() throws InterruptedException {
		Message.getSubject().onNext(new Message("test", getClass().getName(), "hello world"));
		MsgBusiness business = new MsgBusiness();
		business.message1();
		Thread.sleep(500);
		Assert.assertEquals(1, SubscribeMsgBusiness.value.get());
	}

	/**
	 * 决策
	 */
	@Test
	public void testDecision() {
		DecisionBusiness busi = new DecisionBusiness();
		busi.execute();
		Assert.assertEquals(1, busi.getValue());
		busi.execute1();
		Assert.assertEquals(2, busi.getValue());
		busi.execute2();
		Assert.assertEquals(1, busi.getValue());
	}

	/**
	 * 捕获异常
	 */
	@Test
	public void testCatch() {
		CatchBusiness busi = new CatchBusiness();
		busi.execute();
		Assert.assertEquals(-2, busi.getValue());
	}

	/**
	 * 模板测试
	 */
	@Test
	public void testTemplate() {
		TemplateBusiness business = new TemplateBusiness();
		TemplateBusiness.User user = new TemplateBusiness.User();
		user.setName("张三");
		String rs = business.template1(user);
		Assert.assertEquals(user.getName() + ",你好", rs);
		rs = business.template3(user);
		Assert.assertEquals(user.getName() + ",你好", rs);
		rs = business.template2(user.getName(), "你好");
		Assert.assertEquals(user.getName() + ",你好", rs);
	}

	/**
	 * 批处理测试
	 */
	@Test
	public void testBatch()throws Exception {
		Object rs=executor.execute(PreparedBusiness.class, new Object[0]);
		Field field=rs.getClass().getDeclaredField("result");
		field.setAccessible(true);
		Assert.assertEquals(1, field.get(rs));
	}

	/**
	 * 数据库CRUD
	 */
	@Test
	public void testSql() {
		PreparedBusiness business = new PreparedBusiness();
		business.execute();
	}

	/**
	 * 并发
	 */
	@Test
	public void testFork() {
		long t = System.currentTimeMillis();
		ForkBusiness busi = new ForkBusiness();
		busi.fork();
		t = System.currentTimeMillis() - t;
		Assert.assertTrue(t < 1200);
		Assert.assertTrue(t > 900);
		Assert.assertEquals(3, busi.getValue().get());
	}

	/**
	 * 测试服务生成
	 * 
	 * @throws NoSuchFieldException
	 */
	@Test
	public void testService() throws NoSuchFieldException {
		Service service = executor.getService(ServiceDemo1.class);
		Assert.assertNotNull(service);
		Class<?> clazz = service.getParamType();
		Assert.assertEquals(String.class, clazz.getDeclaredField("keyword").getType());
		service = executor.getService(ServiceDemo2.class);
		Assert.assertNotNull(service);
		clazz = service.getParamType();
		Assert.assertNotNull(clazz.getDeclaredField("keyword"));
		Assert.assertNotNull(clazz.getDeclaredField("currentPage"));
		clazz = service.getResultType();
		Field field = clazz.getDeclaredField("values");
		Assert.assertEquals(Set.class, field.getType());
		field = clazz.getDeclaredField("values1");
		Assert.assertEquals(int[].class, field.getType());
	}

	/**
	 * 断路测试
	 */
	@Test
	public void testHystrixCommand() throws Exception {
		HystrixBusiness busi = new HystrixBusiness();
		busi.execute();
		Assert.assertEquals(1, busi.getValue());
		try {
			busi.execute1();
			Assert.assertEquals(10, busi.getValue());// 不会执行这里
		} catch (Exception e) {
			Assert.assertEquals(1, busi.getValue());// 异常被忽略
		}
		final AtomicInteger seq = new AtomicInteger(0);
		Message.getSubject().filter(new Func1<Message, Boolean>() {

			@Override
			public Boolean call(Message t) {
				return Message.FALLBACK.equals(t.getType());// 只处理fallback异常
			}

		}).subscribeOn(Schedulers.io()).subscribe(new Action1<Message>() {

			@Override
			public void call(Message t) {
				seq.incrementAndGet();
			}
		});
		for (int i = 0; i < 100; i++) {
			Thread.sleep(10);
			busi.execute();
		}
		Thread.sleep(300);
		Assert.assertTrue(seq.get() < 80);// 因为发生断路之后不会抛出异常，也就不会有Fallback消息抛出。
	}
}
