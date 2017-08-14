package org.prototype;

import java.util.List;

import org.prototype.business.Service;
import org.prototype.business.api.JavascriptApiCreator;
import org.prototype.business.api.JsonApiCreator.JsonApi;

/**
 * 数据类型测试接口
 * @author lj
 *
 * @param <T>
 */
public interface DataTest{

	void checkParamType(Class<?> type);
	void checkResultType(Class<?> type);
	void initParam(Object param) throws Exception;
	void checkBusiness();
	void checkResult(Object result) throws Exception;
	void checkJavaApi(Service service,Class<?> implementType) throws Exception;
	void checkJavascriptApi(List<JavascriptApiCreator.Validation> validations);
	void checkJsonApi(JsonApi api);
}
