package org.prototype.web;

import java.lang.annotation.Annotation;
import java.util.*;

import org.prototype.core.AnnotatedBuilder;
import org.prototype.core.AnnotationBuilder;
import org.prototype.core.ConditionalHasClass;
import org.prototype.reflect.ClassUtils;
import org.prototype.web.ControllerServiceCreator.MethodParameter;
import org.springframework.stereotype.Component;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.builders.ApiInfoBuilder;

/**
 * SwaggerAnnotationBuilder实现类
 * 
 * @author lijin
 *
 */
@Component
@ConditionalHasClass({ ApiInfoBuilder.class, ApiParam.class })
class SwaggerAnnotationBuilderImpl implements SwaggerAnnotationBuilder {

	@Override
	public AnnotationBuilder buildOperation(AnnotatedBuilder builder) {
		return builder.getAnnotationBuilder(ApiOperation.class);
	}

	@Override
	public void buildParams(AnnotatedBuilder builder, MethodParameter[] mps) {
		AnnotationBuilder ab = builder.getAnnotationBuilder(ApiImplicitParams.class);
		List<AnnotationBuilder> list = new ArrayList<>();
		for (MethodParameter parameter : mps) {
			for (Annotation annotation : parameter.getAnnotations()) {
				if (!ApiModelProperty.class.equals(annotation.annotationType())) {
					break;
				}
				ApiModelProperty property = (ApiModelProperty) annotation;
				AnnotationBuilder b = ab.newAnnotation(ApiImplicitParam.class);
				b.setAttribute("name", parameter.getName()).setAttribute("value", property.value())
						.setAttribute("required", property.required()).setAttribute("paramType", "query")
						.setAttribute("allowMultiple", isArray(parameter.getType()))
						.setAttribute("notes", property.notes())
						.setAttribute("allowableValues", property.allowableValues())
						.setAttribute("example", property.example())
						.setAttribute("dataType", getType(parameter.getType()));
				list.add(b);
			}
		}
		ab.setAttribute("value", list.toArray());
	}
	
	private String getType(Class<?> type){
		if(type.isArray()){
			return getType(type.getComponentType());
		}else if(Collection.class.isAssignableFrom(type)){
			return "String";
		}else if(Number.class.isAssignableFrom(type)){
			return ClassUtils.getBaseType(type).getName();
		}else if(Date.class.isAssignableFrom(type)){
			return "Date";
		}
		return "String";
	}

	private boolean isArray(Class<?> type) {
		return (type.isArray() && !byte[].class.equals(type) && !Byte[].class.equals(type))
				|| Collection.class.isAssignableFrom(type);
	}

}
