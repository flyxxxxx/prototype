package org.prototype.template;

import java.util.Map;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngineManager;

import org.springframework.stereotype.Component;

/**
 * javascript引擎实现. <br>
 * 
 * @author flyxxxxx@163.com
 *
 */
@Component
public class JsEngineImpl implements Engine{
    private final static String FUNCTION_NAME = "internal_function_name";
	
    /**
     * 类型：js
     */
	public static final String TYPE="js";
	/**
	 * 脚本引擎
	 */
    private Compilable scriptEngine;

    /**
     * 构造，需要JavaScript引擎支持(JDK6)
     */
    public JsEngineImpl() {
        scriptEngine = (Compilable) new ScriptEngineManager().getEngineByName("JavaScript");
    }
    /**
     * 返回{@link #TYPE}
     */
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String render(String template, Map<String, Object> properties) {
        StringBuilder sb = new StringBuilder();
        sb.append("function " + FUNCTION_NAME + "(");
        for(String key:properties.keySet()){
            sb.append(key);
            sb.append(',');
        }
        if(properties.size()>0){
            sb.deleteCharAt(sb.length()-1);
        }
        sb.append("){");
        sb.append("return "+template);
        sb.append("\r\n}");
        return renderText(sb.toString(),properties);
	}

	/**
	 * 渲染数据
	 * @param template 模板
	 * @param properties 属性名与值的映射
	 * @return 渲染后的内容
	 */
    private String renderText(String template, Map<String, Object> properties){
        try {
            // 编译JS脚本
            CompiledScript jsFunction = scriptEngine.compile(template);
            jsFunction.eval();
            // 方法参数数组对象
            Object[] objs = new Object[properties.size()];
            // 判断脚本方法参数变量集合中的参数数量是否大于0
            if (properties.size() > 0) {
                int k=0;
                for(String key:properties.keySet()){
                    objs[k++]=properties.get(key);
                }
            }
            Invocable invoke = (Invocable) scriptEngine;
            // 执行JS脚步并返回结果
            if (objs != null && objs.length > 0) {
                return String.valueOf(invoke.invokeFunction(FUNCTION_NAME, objs));
            }

            return String.valueOf(invoke.invokeFunction(FUNCTION_NAME));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
