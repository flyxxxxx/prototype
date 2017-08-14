package org.prototype.business.api;

import java.util.ArrayList;
import java.util.List;

import org.prototype.business.Service;
import org.prototype.business.api.JsonApiCreator.JsonApi;
import org.prototype.business.api.JsonApiCreator.JsonApiBuilder;
import org.prototype.core.ConditionalHasClass;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 将RequestMapping中的内容补充到JsonApi的属性中. <br>
 * 仅在web工程中有效.
 * 将RequestMapping的value、methods、consumes、products，填充到JsonApi的url、methods、consumes、products属性中.
 * 
 * @author lj
 *
 */
@Component
@ConditionalHasClass(RequestMapping.class)
public class JsonApiBuilderImpl implements JsonApiBuilder {

	/**
	 * 构建api的url、methods、consumes、products属性.
	 */
	@Override
	public void build(Service service, JsonApi api) {
		Class<?> type = service.getType();
		RequestMapping mapping = type.getAnnotation(RequestMapping.class);// 查询RequestMapping
		if (mapping == null) {
			mapping = service.getConstructor().getAnnotation(RequestMapping.class);
			if (mapping == null) {// 未找到则不处理
				return;
			}
		}
		List<String> methods = new ArrayList<>();
		for (RequestMethod m : mapping.method()) {
			methods.add(m.name());
		}
		api.setUrl(mapping.value());
		api.setMethods(methods.toArray(new String[methods.size()]));
		api.setConsumes(mapping.consumes());
		api.setProducts(mapping.produces());
	}

}
