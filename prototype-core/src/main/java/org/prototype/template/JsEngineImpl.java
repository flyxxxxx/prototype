package org.prototype.template;

import java.util.Map;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngineManager;

import org.springframework.stereotype.Component;

@Component
public class JsEngineImpl implements Engine{
    private final static String FUNCTION_NAME = "internal_function_name";
	
	public static final String TYPE="js";
    private Compilable scriptEngine;

    public JsEngineImpl() {
        scriptEngine = (Compilable) new ScriptEngineManager().getEngineByName("JavaScript");
    }
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
