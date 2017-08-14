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
public class CharBusiness extends Business implements DataTest {

	@Input(@Prop(desc = "value1",maxLength=1))
	private char value1;

	@Input(@Prop(desc = "value2",required=false,maxLength=1))
	private Character value2;

	@Output(@Prop(desc = "value3"))
	private char value3;
	
	@Output(@Prop(desc = "value4"))
	private Character value4;
	
	void business(){
		value3=value1;
		value4=value2;
	}

	@Override
	public void checkParamType(Class<?> type) {
		Map<String, Property> map = ClassUtils.properties(type);
		Assert.assertEquals(String.class, map.get("value1").getType());
		Assert.assertEquals(String.class, map.get("value2").getType());
	}

	@Override
	public void checkResultType(Class<?> type) {
		Map<String, Property> map = ClassUtils.properties(type);
		Assert.assertEquals(String.class, map.get("value3").getType());
		Assert.assertEquals(String.class, map.get("value4").getType());
	}

	@Override
	public void initParam(Object param) throws Exception {
		Map<String, Property> map = ClassUtils.properties(param.getClass());
		map.get("value1").setValue(param, "c");
		map.get("value2").setValue(param, "A");
	}

	@Override
	public void checkBusiness() {
		Assert.assertEquals('c', value1);
		Assert.assertEquals(new Character('A'), value2);
	}

	@Override
	public void checkResult(Object result) throws Exception{
		Map<String, Property> map = ClassUtils.properties(result.getClass());
		Assert.assertEquals(1, map.get("result").getValue(result));
		Assert.assertEquals("c", map.get("value3").getValue(result));
		Assert.assertEquals("A", map.get("value4").getValue(result));
	}

	@Override
	public void checkJavaApi(Service service,Class<?> implementType) throws Exception{
		Method method=implementType.getMethod("charBusiness", service.getParamType());
		Assert.assertEquals(service.getResultType(), method.getReturnType());
		for(Method m:implementType.getDeclaredMethods()){
			if("charBusiness".equals(m.getName())){
				System.out.println(m);
			}
		}
		method=implementType.getMethod("charBusiness",String.class,String.class);
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
		Assert.assertEquals("String", validation.getType());
		Assert.assertEquals(1,validation.getMaxLength());
		

		validation=map.get("value2");
		prop=props.get("value2").getField().getAnnotation(Input.class).value()[0];
		Assert.assertFalse(validation.isArray());
		Assert.assertEquals(prop.required(), validation.isRequired());
		Assert.assertEquals("String", validation.getType());
		Assert.assertEquals(1,validation.getMaxLength());
		
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
		Assert.assertEquals("String", type.getType());
		Assert.assertEquals(new Integer(1),type.getMaxLength());
		Assert.assertNull(type.getPattern());
		Assert.assertNull(type.getChilds());
		Assert.assertFalse(type.isArray());
		
		field=props.get("value2").getField();
		prop=field.getAnnotation(Input.class).value()[0];
		type=map.get("value2");
		Assert.assertEquals(prop.desc(), type.getDescription());
		Assert.assertEquals(prop.hint(), type.getHint());
		Assert.assertEquals(prop.required()?Boolean.TRUE:Boolean.FALSE, type.getRequired());
		Assert.assertEquals("String", type.getType());		
		
		list = api.getResults();
		Assert.assertEquals(5,list.size());
		map = JsonApi.getMap(list);
		field=props.get("value3").getField();
		prop=field.getAnnotation(Output.class).value()[0];
		type=map.get("value3");
		Assert.assertEquals(prop.desc(), type.getDescription());
		Assert.assertEquals(prop.hint(), type.getHint());
		Assert.assertEquals("String", type.getType());
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
		Assert.assertEquals("String", type.getType());
	}

}
