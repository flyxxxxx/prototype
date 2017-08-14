package org.prototype.business;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.prototype.PrototypeInitializer;
import org.prototype.business.ServiceClassAdvisor.ServiceClassBuilder;
import org.prototype.business.ServiceClassAdvisor.ServiceNameGenerator;
import org.prototype.business.ServiceParamCreate.User;
import org.prototype.core.ClassFactory;
import org.prototype.core.Errors;

public class ErrorParamCreate extends Business {

	@Input({})
	private int error_age;//service.inputoutput.error
	
	@Input({ @Prop(desc = "年龄"), @Prop(desc = "年龄") })
	private int error_age1;//service.inputoutput.error
	
	@Input(@Prop(desc = ""))
	private int error_age2;//service.prop.desc
	
	@Input({ @Prop(desc = "年龄2", pattern = "dsewq") })
	private int error_age3;//service.pattern.unsupported
	
	@Input({ @Prop(desc = "年龄1", pattern = "0.##") })
	private int error_age4;//service.prop.maxLength
	
	@Input({ @Prop(desc = "年龄2", pattern = "method:getAge") })
	private int error_age5;//service.prop.pattern.input.method

	@Output({ @Prop(desc = "年龄2", pattern = "method:getAge") })
	private int error_age51;//service.prop.pattern.output.method
	

	@Input(@Prop(desc = "年龄"))
	private int[][] error_age7;

	@Input({ @Prop(desc = "年龄6") })
	private List<?> error_age6;
	
	@Input(value = { @Prop(desc = "ID", name = "id")})//service.pojo.desc
	private User user;

	@Input(@Prop(desc = "参数", maxLength = 20))
	private Map<String, ?> error_parameters;

	@Input(@Prop(desc = "参数", maxLength = 20))
	private Map<?, String> error_parameters1;

	@InputOutput(input = {
			@Input(type = User.class, desc = "用户列表", value = { @Prop(desc = "ID", name = "id"),
					@Prop(desc = "类型", name = "types"),
					@Prop(desc = "创建时间", name = "createTime", pattern = "yyyy-MM-dd", maxLength = 10) }),
			@Input(type = User.class, value = { @Prop(desc = "ID", name = "id"),
					@Prop(desc = "创建者", name = "creator") }) })//service.input.repeat
	private List<User> users1;

	@InputOutput(output = {
			@Output(type = User.class, desc = "用户列表", value = { @Prop(desc = "ID", name = "id"),
					@Prop(desc = "类型", name = "types"), @Prop(desc = "类别", name = "type"),
					@Prop(desc = "创建时间", name = "createTime", pattern = "yyyy-MM-dd", maxLength = 10) }),
			@Output(type = User.class, value = { @Prop(desc = "ID", name = "id"),
					@Prop(desc = "创建者", name = "creator") }) })//service.output.repeat,service.output.repeat
	private List<User> users2;

	@InputOutput(input = {@Input(value = { @Prop(desc = "ID", name = "id")})})//service.inputoutput
	private List<User> users3;
	
	public static void test(ClassFactory factory, ServiceNameGenerator generator, PrototypeInitializer initializer)
			throws Exception {
		ServiceClassBuilder builder = new ServiceClassBuilder();
		builder.generator = generator;
		builder.factory = factory;
		builder.businessType = ServiceParamCreate.class;
		builder.errors = new Errors(initializer);
		builder.isInput = true;
		builder.properties = IteratorBuilder.getProperties(ServiceParamCreate.class,true);
		builder.build();
		Assert.assertEquals(5, builder.errors.getMessages().size());
	}
}
