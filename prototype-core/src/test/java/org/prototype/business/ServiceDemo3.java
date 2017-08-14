package org.prototype.business;

import java.util.List;

import org.prototype.entity.Dict;
import org.prototype.entity.User;

@ServiceDefine(hint = "...", value = "third demo")
public class ServiceDemo3 extends Business {

	@Input(desc="词典",value={ @Prop(name = "id", desc = "id", required = false), @Prop(name = "name", desc = "条目名", maxLength = 20) })
	private Dict dict;

	@InputOutput(input = { @Input(type = User.class, desc = "用户", value ={ @Prop(desc = "ID", name = "id"),@Prop(desc="类型",name="type")}),
			@Input(type = Dict.class, value = { @Prop(name = "id", desc = "id", required = false),
					@Prop(name = "name", desc = "条目名", maxLength = 20) }) })
	private List<User> users;


	@InputOutput(input = { @Input(type = User.class, desc = "用户", value ={ @Prop(desc = "ID", name = "id"),@Prop(desc="类型",name="type")}),
			@Input(type = Dict.class, value = { @Prop(name = "id", desc = "id", required = false),
					@Prop(name = "name", desc = "条目名", maxLength = 20) }) })
	private User current;
}
