package org.prototype.business;

import java.util.Map;
import java.util.TreeSet;

@ServiceDefine(hint = "...", value = "first demo")
public class ServiceDemo2 extends Business{

	@Input(@Prop(desc = "关键字",maxLength=20))
	private String keyword;
	
	@Input(@Prop(desc="第几页"))
	private Integer currentPage;
	
	@Output(@Prop(desc="值"))
	private TreeSet<Integer> values;
	
	@Output(@Prop(desc="值1"))
	private int[] values1;
	
	@Input(@Prop(desc="附加属性",maxLength=20))
	private Map<String, String> attributes;
}
