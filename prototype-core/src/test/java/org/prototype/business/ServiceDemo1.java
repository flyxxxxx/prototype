package org.prototype.business;

@ServiceDefine(hint = "...", value = "second demo")
public class ServiceDemo1 extends Business{

	@Input(@Prop(desc = "关键字",maxLength=20))
	private String keyword;
}
