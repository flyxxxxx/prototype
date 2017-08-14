package org.prototype.business;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.prototype.DataTest;
import org.prototype.business.api.JavascriptApiCreator.Validation;
import org.prototype.business.api.JsonApiCreator.JsonApi;
import org.prototype.business.api.JsonApiCreator.Type;
import org.prototype.reflect.ClassUtils;
import org.prototype.reflect.Property;
/**
 * boolean类型测试
 * @author lj
 *
 */
@ServiceDefine(value="boolean",hint="test1")
public class BooleanBusiness extends Business implements DataTest {

	@Input(@Prop(desc = "value1"))
	private boolean value1;

	@Input(@Prop(desc = "value2",required=false))
	private Boolean value2;

	@Output(@Prop(desc = "value3"))
	private boolean value3;
	
	@Output(@Prop(desc = "value4"))
	private Boolean value4;
	
	void business(){
		value3=value1;
		value4=value2;
	}

	@Override
	public void checkParamType(Class<?> type) {
		Map<String, Property> map = ClassUtils.properties(type);
		Assert.assertEquals(boolean.class, map.get("value1").getType());
		Assert.assertEquals(Boolean.class, map.get("value2").getType());
	}

	@Override
	public void checkResultType(Class<?> type) {
		Map<String, Property> map = ClassUtils.properties(type);
		Assert.assertEquals(boolean.class, map.get("value3").getType());
		Assert.assertEquals(Boolean.class, map.get("value4").getType());
	}

	@Override
	public void initParam(Object param) throws Exception {
		Map<String, Property> map = ClassUtils.properties(param.getClass());
		map.get("value1").setValue(param, true);
		map.get("value2").setValue(param, true);
	}

	@Override
	public void checkBusiness() {
		Assert.assertEquals(true, value1);
		Assert.assertEquals(Boolean.TRUE, value2);
	}

	@Override
	public void checkResult(Object result) throws Exception{
		Map<String, Property> map = ClassUtils.properties(result.getClass());
		Assert.assertEquals(1, map.get("result").getValue(result));
		Assert.assertEquals(true, map.get("value3").getValue(result));
		Assert.assertEquals(Boolean.TRUE, map.get("value4").getValue(result));
	}

	@Override
	public void checkJavaApi(Service service,Class<?> implementType) throws Exception{
		Method method=implementType.getMethod("booleanBusiness", service.getParamType());
		Assert.assertEquals(service.getResultType(), method.getReturnType());
		method=implementType.getMethod("booleanBusiness",boolean.class,Boolean.class);
		Assert.assertEquals(service.getResultType(), method.getReturnType());
	}

	@Override
	public void checkJavascriptApi(List<Validation> validations) {
		Map<String, Property> props = ClassUtils.properties(getClass());
		Map<String, Validation> map = Validation.getMap(validations);
		Validation validation=map.get("value1");
		Prop prop=props.get("value1").getField().getAnnotation(Input.class).value()[0];
		Assert.assertFalse(validation.isArray());
		Assert.assertEquals(prop.required(), validation.isRequired());
		Assert.assertEquals("boolean", validation.getType());
		Assert.assertEquals(-1,validation.getMaxLength());
		

		validation=map.get("value2");
		prop=props.get("value2").getField().getAnnotation(Input.class).value()[0];
		Assert.assertFalse(validation.isArray());
		Assert.assertEquals(prop.required(), validation.isRequired());
		Assert.assertEquals("boolean", validation.getType());
		Assert.assertEquals(-1,validation.getMaxLength());
		
	}

	@Override
	public void checkJsonApi(JsonApi api) {
		ServiceDefine define=getClass().getAnnotation(ServiceDefine.class);
		Assert.assertEquals(define.value(), api.getDescription());
		Assert.assertEquals(define.hint(), api.getHint());
		Assert.assertEquals(define.version(), api.getVersion());
		Assert.assertEquals(define.url(), api.getUrl()[0]);
		List<Type> list = api.getParams();
		Assert.assertEquals(2,list.size());
		Map<String, Property> props = ClassUtils.properties(getClass());
		Map<String, Type> map = JsonApi.getMap(list);
		
		Field field=props.get("value1").getField();
		Type type=map.get("value1");
		Input input=field.getAnnotation(Input.class);
		Prop prop=input.value()[0];
		Assert.assertEquals(prop.desc(), type.getDescription());
		Assert.assertEquals(prop.hint(), type.getHint());
		Assert.assertEquals(prop.required()?Boolean.TRUE:Boolean.FALSE, type.getRequired());
		Assert.assertEquals("boolean", type.getType());
		Assert.assertNull(type.getMaxLength());
		Assert.assertNull(type.getPattern());
		Assert.assertNull(type.getChilds());
		Assert.assertFalse(type.isArray());
		
		field=props.get("value2").getField();
		prop=field.getAnnotation(Input.class).value()[0];
		type=map.get("value2");
		Assert.assertEquals(prop.desc(), type.getDescription());
		Assert.assertEquals(prop.hint(), type.getHint());
		Assert.assertEquals(prop.required()?Boolean.TRUE:Boolean.FALSE, type.getRequired());
		Assert.assertEquals("boolean", type.getType());		
		
		list = api.getResults();
		Assert.assertEquals(5,list.size());
		map = JsonApi.getMap(list);
		field=props.get("value3").getField();
		prop=field.getAnnotation(Output.class).value()[0];
		type=map.get("value3");
		Assert.assertEquals(prop.desc(), type.getDescription());
		Assert.assertEquals(prop.hint(), type.getHint());
		Assert.assertEquals("boolean", type.getType());
		Assert.assertNull(type.getRequired());
		Assert.assertNull(type.getMaxLength());
		Assert.assertNull(type.getPattern());
		Assert.assertNull(type.getChilds());
		Assert.assertFalse(type.isArray());

		type=map.get("value4");
		field=props.get("value4").getField();
		prop=field.getAnnotation(Output.class).value()[0];
		Assert.assertEquals(prop.desc(), type.getDescription());
		Assert.assertEquals(prop.hint(), type.getHint());
		Assert.assertEquals("boolean", type.getType());
	}

}
