package org.prototype.web;


import org.prototype.core.AnnotatedBuilder;
import org.prototype.core.AnnotationBuilder;
import org.prototype.web.ControllerServiceCreator.MethodParameter;

/**
 * Swagger注解创建接口
 * @author flyxxxxx@163.com
 *
 */
interface SwaggerAnnotationBuilder {

	AnnotationBuilder buildOperation(AnnotatedBuilder builder);

	void buildParams(AnnotatedBuilder builder, MethodParameter[] mps);
}
