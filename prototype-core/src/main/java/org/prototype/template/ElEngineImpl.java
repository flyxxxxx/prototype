package org.prototype.template;

import java.util.Map;
import java.util.Properties;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.springframework.stereotype.Component;

import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.util.SimpleContext;

/**
 * EL表达式模版引擎
 * 
 */
@Component
public class ElEngineImpl implements Engine {
	public static final String TYPE="el";
	/** 定义EL表达式工厂 */
	private ExpressionFactory factory;

	/**
	 * 构造方法初始化类变量
	 */
	public ElEngineImpl() {
		Properties properties=new Properties(System.getProperties());
		properties.setProperty("javax.el.nullProperties", "true");
		factory = new ExpressionFactoryImpl(properties);
	}

	/**
	 * EL渲染
	 */
	public String render(String template, Map<String, Object> properties) {
		// 执行EL表达式并把结果存储到表达式上下文对象中
		SimpleContext context = new SimpleContext();
		ValueExpression value = factory.createValueExpression(context, template, String.class);
		if(properties!=null){
			for(Map.Entry<String, Object> e:properties.entrySet()){
				context.getELResolver().setValue(context, null, e.getKey(),e.getValue());
			}
		}
		// 返回表达式上下文对象中的值
		return (String) value.getValue(context);
	}

	@Override
	public String getType() {
		return TYPE;
	}

}
