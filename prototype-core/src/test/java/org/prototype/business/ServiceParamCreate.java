package org.prototype.business;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;

import org.junit.Assert;

import lombok.Data;
import lombok.EqualsAndHashCode;

@ServiceDefine(value = "service create")
@Data
@EqualsAndHashCode(callSuper=true)
public class ServiceParamCreate extends Business {

	@Input(@Prop(desc = "年龄"))
	private int age;

	@Input({ @Prop(desc = "年龄1", pattern = "0.##", maxLength = 5) })
	private int age1;

	@Input({ @Prop(desc = "年龄2", pattern = "method:getAge", maxLength = 5) })
	private int age2;

	@Input({ @Prop(desc = "年龄3", required = false) })
	private Integer age3;

	@Input({ @Prop(desc = "年龄4") })
	private BigInteger age4;

	@Input({ @Prop(desc = "年龄5") })
	private int[] age5;
	@Input({ @Prop(desc = "年龄6", pattern = "method:getAge", maxLength = 5) })
	private int[] age6;
	@Input({ @Prop(desc = "年龄7", pattern = "0.##", maxLength = 5) })
	private int[] age7;

	@Input(@Prop(desc = "是否学生"))
	private boolean student;
	@Input(@Prop(desc = "是否学生1"))
	private Boolean student1;

	@Input(@Prop(desc = "字符1",maxLength=1))
	private char char1;

	@Input(@Prop(desc = "字符2",maxLength=1))
	private Character char2;

	@Input({ @Prop(desc = "年龄8") })
	private List<Integer> age8;

	@Input({ @Prop(desc = "年龄9", pattern = "0.##", maxLength = 5) })
	private List<Integer> age9;

	@Input({ @Prop(desc = "年龄10", pattern = "method:getAge1", maxLength = 5) })
	private List<Integer> age10;

	@Input(@Prop(desc = "开始时间", pattern = "yyyy-MM-dd HH:mm", maxLength = 16))
	private Date startTime;

	@Input(@Prop(desc = "开始时间1"))
	private java.sql.Date startTime1;

	@Input(@Prop(desc = "Enum", maxLength = 10))
	private Enum1 enum1;

	@Input(@Prop(desc = "关键字", maxLength = 20))
	private String keyword;

	@Input(@Prop(desc = "密码", maxLength = 20))
	private byte[] password;


	@Input(@Prop(desc = "参数", maxLength = 20))
	private Map<String, String> parameters;

	@Input(desc = "用户", value = { @Prop(desc = "ID", name = "id"), @Prop(desc = "TYPES", name = "types") })
	private User user;

	@Input(desc = "用户列表", value = { @Prop(desc = "ID", name = "id"), @Prop(desc = "TYPES", name = "types") })
	private User[] users;

	@InputOutput(input = {
			@Input(type = User.class, desc = "用户列表", value = { @Prop(desc = "ID", name = "id"),
					@Prop(desc = "类型", name = "types"), @Prop(desc = "类别", name = "type"),
					@Prop(desc = "创建时间", name = "createTime", pattern = "yyyy-MM-dd", maxLength = 10) }),
			@Input(type = UserType.class, value = { @Prop(desc = "ID", name = "id"),
					@Prop(desc = "创建者", name = "creator") }) })
	private List<User> users1;

	public int getAge(String value) {
		return value.length();
	}

	public Integer getAge1(String value) {
		return value.length();
	}
	public static void testType(Class<?> clazz) throws Exception{
		Assert.assertEquals(int.class, clazz.getDeclaredField("age").getType());
		Assert.assertEquals(String.class, clazz.getDeclaredField("age1").getType());
		Assert.assertEquals(String.class, clazz.getDeclaredField("age2").getType());
		Assert.assertEquals(Integer.class, clazz.getDeclaredField("age3").getType());
		Assert.assertEquals(BigInteger.class, clazz.getDeclaredField("age4").getType());
		Assert.assertEquals(int[].class, clazz.getDeclaredField("age5").getType());
		Assert.assertEquals(String[].class, clazz.getDeclaredField("age6").getType());
		Assert.assertEquals(String[].class, clazz.getDeclaredField("age7").getType());
		Assert.assertEquals(boolean.class, clazz.getDeclaredField("student").getType());
		Assert.assertEquals(Boolean.class, clazz.getDeclaredField("student1").getType());
		Assert.assertEquals(String.class, clazz.getDeclaredField("startTime").getType());
		Assert.assertEquals(Date.class, clazz.getDeclaredField("startTime1").getType());
		Assert.assertEquals(String.class, clazz.getDeclaredField("enum1").getType());
		Assert.assertEquals(String.class, clazz.getDeclaredField("keyword").getType());
		Assert.assertEquals(String.class, clazz.getDeclaredField("password").getType());
		Field f = clazz.getDeclaredField("age8");
		Assert.assertEquals(List.class, f.getType());
		Assert.assertEquals(Integer.class, IteratorBuilder.getGeneric(f, 0));
		f = clazz.getDeclaredField("age9");
		Assert.assertEquals(List.class, f.getType());
		Assert.assertEquals(String.class, IteratorBuilder.getGeneric(f, 0));

		f = clazz.getDeclaredField("parameters");
		Assert.assertEquals(Map.class, f.getType());
		Assert.assertEquals(String.class, IteratorBuilder.getGeneric(f, 0));
		Assert.assertEquals(String.class, IteratorBuilder.getGeneric(f, 1));

		Class<?> userClass = clazz.getDeclaredField("user").getType();
		Assert.assertEquals(Integer.class, userClass.getDeclaredField("id").getType());
		f = userClass.getDeclaredField("types");
		Assert.assertEquals(List.class, f.getType());
		Assert.assertEquals(Integer.class, IteratorBuilder.getGeneric(f, 0));

		userClass = clazz.getDeclaredField("users").getType();
		Assert.assertNotEquals(User.class, userClass.getComponentType());
		f = userClass.getComponentType().getDeclaredField("id");
		Assert.assertNotNull(f);
		Assert.assertEquals(Integer.class, f.getType());
		Assert.assertEquals(2, userClass.getComponentType().getDeclaredFields().length);

		f = clazz.getDeclaredField("users1");
		userClass = IteratorBuilder.getGeneric(f, 0);

		f = userClass.getDeclaredField("type");
		Assert.assertNotNull(f);
		Class<?> userTypeClass = f.getType();
		Assert.assertEquals(2, userTypeClass.getDeclaredFields().length);

		Assert.assertEquals(Integer.class, userTypeClass.getDeclaredField("creator").getType());
	}

	public enum Enum1 {
		A, B, C;
	}

	@Data
	public class User {
		@Id
		private Integer id;
		private Collection<Integer> types;
		private Date createTime;
		private UserType type;
	}

	@Data
	public class UserType {
		private Integer id;
		private String name;
		private User creator;
	}
}
