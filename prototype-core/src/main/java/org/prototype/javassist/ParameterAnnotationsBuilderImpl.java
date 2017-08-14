package org.prototype.javassist;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prototype.core.AnnotationBuilder;
import org.prototype.core.ParameterAnnotationsBuilder;
import org.springframework.util.Assert;

import javassist.bytecode.ConstPool;
import lombok.Getter;

class ParameterAnnotationsBuilderImpl implements ParameterAnnotationsBuilder {

	private ConstPool cp;
	@Getter
	private javassist.bytecode.annotation.Annotation[][] annotations;

	public ParameterAnnotationsBuilderImpl(ConstPool cp, javassist.bytecode.annotation.Annotation[][] annotations) {
		this.cp = cp;
		this.annotations = annotations;
	}
	
	public boolean hasParameterAnnotations(){
		int k=annotations.length;
		for(int i=0;i<k;i++){
			if(annotations[i].length>0){
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public AnnotationBuilder[] getAnnotationBuilder(int parameterIndex,
			Class<? extends Annotation>... annotationClass) {
		Assert.isTrue(parameterIndex < annotations.length);
		Assert.notEmpty(annotationClass);
		Map<String, javassist.bytecode.annotation.Annotation> map = getAnnotationsMap(annotations[parameterIndex]);
		AnnotationBuilderImpl[] builders = new AnnotationBuilderImpl[annotationClass.length];
		int k = 0;
		for (Class<? extends Annotation> clazz : annotationClass) {
			javassist.bytecode.annotation.Annotation annotation = map.get(clazz.getName());
			if (annotation == null) {
				builders[k] = new AnnotationBuilderImpl(cp, clazz);
				map.put(clazz.getName(), builders[k].getAnnotation());
			} else {
				builders[k] = new AnnotationBuilderImpl(cp, annotation);
			}
		}
		annotations[parameterIndex] = map.values().toArray(new javassist.bytecode.annotation.Annotation[map.size()]);
		return builders;
	}

	private Map<String, javassist.bytecode.annotation.Annotation> getAnnotationsMap(
			javassist.bytecode.annotation.Annotation[] anns) {
		Map<String, javassist.bytecode.annotation.Annotation> map = new HashMap<>();
		for (javassist.bytecode.annotation.Annotation annotation : anns) {
			map.put(annotation.getTypeName(), annotation);
		}
		return map;
	}

	@Override
	public void copyAnnotations(int parameterIndex, Annotation[] annotations) {
		if (annotations == null || annotations.length == 0) {
			return;
		}
		Assert.isTrue(parameterIndex < annotations.length);
		List<javassist.bytecode.annotation.Annotation> list = CtMethodBuilder.getAnnotations(cp, annotations);
		list.addAll(Arrays.asList(this.annotations[parameterIndex]));
		this.annotations[parameterIndex] = list.toArray(new javassist.bytecode.annotation.Annotation[list.size()]);
	}

}
