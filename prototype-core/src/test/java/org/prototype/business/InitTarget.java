package org.prototype.business;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.prototype.entity.Dict;
import org.prototype.entity.User;
import org.prototype.reflect.ClassUtils;
import org.prototype.reflect.Property;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ServiceDefine(value = "初始化参数业务")
@Data
@EqualsAndHashCode(callSuper = true)
public class InitTarget extends Business {

	@Input(value = { @Prop(desc = "enum1", maxLength = 10) })
	private Enum1 enum1;

	@Input(value = { @Prop(desc = "enum2", maxLength = 10) })
	private Enum1[] enum2;

	@Input(value = { @Prop(desc = "enum3", maxLength = 10) })
	private Collection<Enum1> enum3;

	@Input(value = { @Prop(desc = "enum4", maxLength = 10) })
	private Set<Enum1> enum4 = new HashSet<>();

	@Input(value = { @Prop(desc = "enum5", maxLength = 10) })
	private Map<String, Enum1> enum5;
	@Input(value = { @Prop(desc = "enum6", maxLength = 10) })
	private Map<String, Enum1> enum6 = new HashMap<>();

	@Input(value = { @Prop(desc = "enum6", maxLength = 10) })
	private EnumSet<Enum1> enum7;

	@Input(value = { @Prop(desc = "姓名", maxLength = 20) })
	private String name1;

	@Input(value = { @Prop(desc = "姓名", maxLength = 20) })
	private String[] name2;

	@Input(value = { @Prop(desc = "姓名", maxLength = 20) })
	private List<String> name3;

	@Input(value = { @Prop(desc = "boolean") })
	private boolean boolean1;
	@Input(value = { @Prop(desc = "boolean") })
	private boolean[] boolean2;
	@Input(value = { @Prop(desc = "boolean") })
	private Boolean boolean3;
	@Input(value = { @Prop(desc = "boolean") })
	private Boolean[] boolean4;
	@Input(value = { @Prop(desc = "boolean") })
	private List<Boolean> boolean5;

	@Input(value = { @Prop(desc = "ages") })
	private Integer[] age2;

	@Input(value = { @Prop(desc = "age") })
	private Integer age1;

	@Input(value = { @Prop(desc = "age") })
	private int age4;
	@Input(value = { @Prop(desc = "age", maxLength = 10) })
	private BigInteger age5;
	@Input(value = { @Prop(desc = "age") })
	private BigInteger age6;

	@Input(value = { @Prop(desc = "value1") })
	private TreeSet<Integer> age3;

	@Input(value = { @Prop(desc = "createTime") })
	private Date createTime;
	/**
	 * 日期格式
	 */
	@Input(value = { @Prop(desc = "createTime1", pattern = "yyyy-MM-dd", maxLength = 10) })
	private Date createTime1;
	/**
	 * 日期格式
	 */
	@Input(value = { @Prop(desc = "createTime1", maxLength = 10) })
	private Date createTime2;
	/**
	 * 日期格式
	 */
	@Input(value = { @Prop(desc = "createTime1") })
	private List<java.sql.Date> createTime3;
	/**
	 * 日期格式
	 */
	@Input(value = { @Prop(desc = "createTime1", pattern = "yyyy-MM-dd", maxLength = 10) })
	private List<Date> createTime4;

	@Input(desc = "用户", value = { @Prop(desc = "ID", name = "id"),
			@Prop(desc = "NAME", name = "name", maxLength = 20) })
	private User user;
	@Input(desc = "用户2", value = { @Prop(desc = "ID", name = "id") })
	private User user2;

	@InputOutput(input = {
			@Input(type = User.class, desc = "用户1", value = { @Prop(desc = "ID", name = "id"),
					@Prop(desc = "NAME", name = "name", maxLength = 20), @Prop(desc = "类别", name = "type") }),
			@Input(type = Dict.class, value = { @Prop(desc = "ID", name = "id") }) })
	private User user1;

	@Input(value = { @Prop(desc = "createTime1", pattern = "0.##", maxLength = 10) })
	private float score;
	

	@Input(value = { @Prop(desc = "char1", maxLength = 1) })
	private char char1;

	@Input(value = { @Prop(desc = "char2", maxLength = 1) })
	private Character char2;

	/**
	 * 调用一个方法获得相应值
	 */
	@Input(value = { @Prop(desc = "value2", pattern = "method:getValue", maxLength = 100) })
	private Integer value2;

	@InputOutput(input = {
			@Input(type = User.class, desc = "用户s", value = { @Prop(desc = "ID", name = "id"),
					@Prop(desc = "NAME", name = "name", maxLength = 20), @Prop(desc = "类别", name = "type") }),
			@Input(type = Dict.class, value = { @Prop(desc = "ID", name = "id"),
					@Prop(desc = "NAME", name = "name", maxLength = 20) }) })
	private List<User> users;

	public Integer getValue(String value) {
		return value.length();
	}

	public static void main(String[] args) throws Exception {
		InitTarget t1 = new InitTarget();
		InitTargetParam param = new InitTargetParam();
		param.enum1 = "A";
		param.enum2 = new String[] { "B" };
		param.enum3 = new ArrayList<>();
		param.enum3.add("C");
		param.enum4 = new HashSet<>();
		param.enum4.add(Enum1.A.name());
		param.enum4.add(Enum1.B.name());
		param.enum5 = new HashMap<>();
		param.enum5.put("1", Enum1.A.name());
		param.enum6 = new HashMap<>();
		param.enum6.put("2", Enum1.C.name());
		param.enum7 = new HashSet<>();
		param.enum7.add("A");

		param.name1 = "321432";
		param.name2 = new String[] { "fdsds" };
		param.name3 = new ArrayList<>();
		param.name3.add("314fdqe3214");

		param.boolean1 = true;
		param.boolean2 = new boolean[] { false };
		param.boolean3 = Boolean.TRUE;
		param.boolean4 = new Boolean[] { Boolean.FALSE };
		param.boolean5 = Arrays.asList(param.boolean4);

		param.age1 = "134";
		param.age2 = new Integer[] { 5 };
		param.age3 = new HashSet<Integer>();
		param.age3.add(1);
		param.age4 = 1342;
		param.age5 = "2143214321";
		param.age6 = new BigInteger("13421432");

		param.createTime = new Date();
		param.createTime1 = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		param.createTime2 = new Date();
		param.createTime3 = new ArrayList<>();
		param.createTime3.add(new Date());
		param.createTime4 = new ArrayList<>();
		param.createTime4.add(param.createTime1);

		param.value2 = "214321i8dsa";// 这几行性能不好
		param.score = "142.99";
		param.char1="a";
		param.char2="C";

		param.user1 = new User();
		param.user = param.user1;
		param.users = new ArrayList<>();
		param.users.add(param.user1);
		param.user1.setId(5);
		param.user1.setName("test1");
		param.user1.setType(new Dict());
		param.user1.getType().setId(91);
		param.user1.getType().setName("fdsa");
		param.user2 = new User();
		param.user2.setId(3143);

		Map<String, Object> params=new HashMap<>();
		for(Map.Entry<String, Property> entry:ClassUtils.properties(param.getClass()).entrySet()){
			params.put(entry.getKey(), entry.getValue().getValue(param));
		}
		new InitTargetExecuteFilter.ParamSetter(new ExecuteChainImpl(t1),t1).build(params);
		
		Assert.assertNotNull(t1.enum2);
		Assert.assertEquals(param.enum2[0], t1.enum2[0].name());
		Assert.assertEquals(param.enum3.toArray()[0], t1.enum3.toArray()[0].toString());
		Assert.assertEquals(param.enum4.size(), t1.enum4.size());
		Assert.assertEquals(param.enum5.size(), t1.enum5.size());
		Assert.assertEquals(Enum1.class, t1.enum5.values().toArray()[0].getClass());
		Assert.assertEquals(param.enum6.size(), t1.enum6.size());
		Assert.assertEquals(Enum1.class, t1.enum6.values().toArray()[0].getClass());
		Assert.assertEquals(param.enum7.toArray()[0], t1.enum7.toArray()[0].toString());

		Assert.assertEquals(param.name1, t1.name1);
		Assert.assertEquals(param.name2.length, t1.name2.length);
		Assert.assertEquals(param.name2[0], t1.name2[0]);
		Assert.assertEquals(param.name3.size(), t1.name3.size());
		Assert.assertEquals(param.name3.toArray()[0], t1.name3.toArray()[0]);

		Assert.assertEquals(param.boolean1, t1.boolean1);
		Assert.assertEquals(param.boolean2[0], t1.boolean2[0]);
		Assert.assertEquals(param.boolean3, t1.boolean3);
		Assert.assertEquals(param.boolean4[0], t1.boolean4[0]);
		Assert.assertEquals(param.boolean5.toArray()[0], t1.boolean5.toArray()[0]);

		Assert.assertEquals(param.age1, t1.age1.toString());
		Assert.assertEquals(param.age2[0], t1.age2[0]);
		Assert.assertEquals(param.age3.toArray()[0], t1.age3.toArray()[0]);
		Assert.assertEquals(param.age4, t1.age4);
		Assert.assertEquals(param.age5, t1.age5.toString());
		Assert.assertEquals(param.age6, t1.age6);

		Assert.assertEquals(param.createTime, t1.getCreateTime());
		Assert.assertEquals(param.createTime1, new SimpleDateFormat("yyyy-MM-dd").format(t1.createTime1));
		Assert.assertEquals(param.createTime2, t1.createTime2);
		Assert.assertEquals(param.createTime3.toArray()[0], ((java.sql.Date) t1.createTime3.toArray()[0]));
		Assert.assertEquals(param.createTime4.toArray()[0],
				new SimpleDateFormat("yyyy-MM-dd").format((Date) t1.createTime4.toArray()[0]));

		Assert.assertEquals(param.value2.length(), t1.value2.intValue());
		
		Assert.assertTrue(t1.score > 0);
		Assert.assertNotNull(t1.char1);
		Assert.assertNotNull(t1.char2);

		log.info(new ObjectMapper().writeValueAsString(t1.user));
		log.info(new ObjectMapper().writeValueAsString(t1.user1));
		log.info(new ObjectMapper().writeValueAsString(t1.users));
		Assert.assertNotNull(t1.user2.getId());

	}

	@Data
	public static class InitTargetParam {
		private String enum1;
		private String[] enum2;
		private Collection<String> enum3;
		private Set<String> enum4;
		private Map<String, String> enum5;
		private Map<String, String> enum6;
		private Set<String> enum7;

		private String name1;
		private String[] name2;
		private List<String> name3;

		private boolean boolean1;
		private boolean[] boolean2;
		private Boolean boolean3;
		private Boolean[] boolean4;
		private List<Boolean> boolean5;

		private String age1;
		private Integer[] age2;
		private Set<Integer> age3;
		private int age4;
		private String age5;
		private BigInteger age6;

		private Date createTime;
		private String createTime1;
		private Date createTime2;
		private List<Date> createTime3;
		private List<String> createTime4;

		private String value2;

		private String score;
		private String char1;
		private String char2;

		private User user;
		private User user1;
		private User user2;
		private List<User> users;

	}

	public static enum Enum1 {
		A, B, C;
	}
}
