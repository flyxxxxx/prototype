package org.prototype.business;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@ServiceDefine(value = "获取结果业务")
@Data
@Slf4j
public class GetResult {

	@Output(value = { @Prop(desc = "enum1", maxLength = 10) })
	private Enum1 enum1;

	@Output(value = { @Prop(desc = "enum2", maxLength = 10) })
	private Enum1[] enum2;

	@Output(value = { @Prop(desc = "enum3", maxLength = 10) })
	private Collection<Enum1> enum3;

	@Output(value = { @Prop(desc = "enum4", maxLength = 10) })
	private Set<Enum1> enum4 = new HashSet<>();

	@Output(value = { @Prop(desc = "enum5", maxLength = 10) })
	private Map<String, Enum1> enum5;
	@Output(value = { @Prop(desc = "enum6", maxLength = 10) })
	private Map<String, Enum1> enum6 = new HashMap<>();

	@Output(value = { @Prop(desc = "enum6", maxLength = 10) })
	private EnumSet<Enum1> enum7;

	@Output(value = { @Prop(desc = "姓名", maxLength = 20) })
	private String name1;

	@Output(value = { @Prop(desc = "姓名", maxLength = 20) })
	private String[] name2;

	@Output(value = { @Prop(desc = "姓名", maxLength = 20) })
	private List<String> name3;

	@Output(value = { @Prop(desc = "boolean") })
	private boolean boolean1;
	@Output(value = { @Prop(desc = "boolean") })
	private boolean[] boolean2;
	@Output(value = { @Prop(desc = "boolean") })
	private Boolean boolean3;
	@Output(value = { @Prop(desc = "boolean") })
	private Boolean[] boolean4;
	@Output(value = { @Prop(desc = "boolean") })
	private List<Boolean> boolean5;

	@Output(value = { @Prop(desc = "ages") })
	private Integer[] age2;

	@Output(value = { @Prop(desc = "age") })
	private Integer age1;

	@Output(value = { @Prop(desc = "age") })
	private int age4;
	@Output(value = { @Prop(desc = "age", maxLength = 10) })
	private BigInteger age5;

	@Output(value = { @Prop(desc = "value1") })
	private TreeSet<Integer> age3;

	/**
	 * 日期格式
	 */
	@Output(value = { @Prop(desc = "createTime") })
	private Date createTime;
	@Output(value = { @Prop(desc = "createTime1", pattern = "yyyy-MM-dd") })
	private Date createTime1;
	@Output(value = { @Prop(desc = "createTime1") })
	private List<java.sql.Date> createTime2;
	@Output(value = { @Prop(desc = "createTime1", pattern = "yyyy-MM-dd HH:mm") })
	private List<Date> createTime3;

	@Output(value = { @Prop(desc = "ID", name = "id"), @Prop(desc = "NAME", name = "name") })
	private User user;
	@Output(value = { @Prop(desc = "ID", name = "id")})
	private User user2;

	@InputOutput(output = {
			@Output(type = User.class, value = { @Prop(desc = "ID", name = "id"),
					@Prop(desc = "NAME", name = "name", maxLength = 20), @Prop(desc = "类别", name = "type") }),
			@Output(type = Dict.class, value = { @Prop(desc = "ID", name = "id") }) })
	private User user1;

	@Output(value = { @Prop(desc = "createTime1", pattern = "0.##", maxLength = 10) })
	private float score;

	@Output(value = { @Prop(desc = "char1") })
	private char char1;

	@Output(value = { @Prop(desc = "char1") })
	private Character char2;

	/**
	 * 调用一个方法获得相应值
	 */
	@Output(value = { @Prop(desc = "value2", pattern = "method:getValue", maxLength = 100) })
	private Integer value2;

	@InputOutput(output = {
			@Output(type = User.class, value = { @Prop(desc = "ID", name = "id"),
					@Prop(desc = "NAME", name = "name", maxLength = 20), @Prop(desc = "类别", name = "type") }),
			@Output(type = Dict.class, value = { @Prop(desc = "ID", name = "id"),
					@Prop(desc = "NAME", name = "name",maxLength=20) }) })
	private List<User> users;
	
	public String getValue(Integer value){
		return value.toString();
	}
	
	public static void main(String[] args) throws Exception{
		GetResult result=new GetResult();
		result.enum1=Enum1.A;
		result.enum2=new Enum1[]{Enum1.B};
		result.enum3=new ArrayList<>();
		result.enum3.add(Enum1.C);
		result.enum4=new HashSet<>();
		result.enum4.add(Enum1.A);
		result.enum5=new HashMap<>();
		result.enum5.put("1", Enum1.B);
		result.enum6=new HashMap<>();
		result.enum6.put("1", Enum1.C);
		result.enum7=EnumSet.of(Enum1.A);
		
		result.name1="3210aq";
		result.name2=new String[0];
		result.name3=new ArrayList<>();
		result.name3.add("89flsaf1");
		
		result.boolean1=true;
		result.boolean2=new boolean[]{true};
		result.boolean3=Boolean.TRUE;
		result.boolean4=new Boolean[]{Boolean.TRUE,Boolean.FALSE};
		result.boolean5=new ArrayList<>();
		result.boolean5.add(Boolean.TRUE);
		
		result.age1=1321;
		result.age2=new Integer[]{314321,null};		
		result.age3=new TreeSet<>();
		result.age3.add(5);
		result.age4=14321;
		result.age5=new BigInteger("514132");
		
		result.createTime=new Date();
		result.createTime1=new Date();
		result.createTime2=new ArrayList<>();
		result.createTime2.add(new java.sql.Date(System.currentTimeMillis()));
		result.createTime3=new ArrayList<>();
		result.createTime3.add(new Date());
		
		result.value2=5;
		result.score=34321.13f;
		result.char1='c';
		result.char2='b';

		result.user1 = new User();
		result.user = result.user1;
		result.users = new ArrayList<>();
		result.users.add(result.user1);
		result.user1.setId(5);
		result.user1.setName("test1");
		result.user1.setType(new Dict());
		result.user1.getType().setId(91);
		result.user1.getType().setName("fdsa");
		result.user2=new User();
		result.user2.setId(5);
		
		Result rs=new Result();
		new GetResultExecuteFilter.ResultSetter(result,rs).build();
		long t=System.currentTimeMillis();
		for(int i=0;i<10000;i++){
			//TODO new GetResultExecuteFilter.ResultSetter(result,new Result()).build();
		}
		log.info("Get result 10000 times use time : "+(System.currentTimeMillis()-t)+" ms .");
		
		Assert.assertEquals(result.enum1, rs.enum1);
		Assert.assertEquals(Enum1.B, rs.enum2[0]);
		Assert.assertEquals(Enum1.C, rs.enum3.toArray()[0]);
		Assert.assertEquals(Enum1.A, rs.enum4.toArray()[0]);
		Assert.assertEquals(Enum1.B, rs.enum5.values().toArray()[0]);
		Assert.assertEquals(Enum1.C, rs.enum6.values().toArray()[0]);
		Assert.assertEquals(Enum1.A, rs.enum7.toArray()[0]);
		
		Assert.assertEquals(result.name1, rs.name1);
		Assert.assertEquals(0, rs.name2.length);
		Assert.assertEquals(result.name3.toArray()[0], rs.name3.toArray()[0]);
		
		Assert.assertEquals(result.boolean1, rs.boolean1);
		Assert.assertArrayEquals(result.boolean2, rs.boolean2);
		Assert.assertEquals(result.boolean3, rs.boolean3);
		Assert.assertArrayEquals(result.boolean4, rs.boolean4);
		Assert.assertArrayEquals(result.boolean5.toArray(), rs.boolean5.toArray());
		
		Assert.assertEquals(result.createTime, rs.createTime);
		Assert.assertTrue(rs.createTime1.length()>0);
		Assert.assertEquals(result.createTime2.toArray(new java.sql.Date[1])[0].getTime(), rs.createTime2.toArray(new Date[1])[0].getTime());
		String date=new SimpleDateFormat("yyyy-MM-dd HH:mm").format(result.getCreateTime3().toArray(new Date[1])[0]);
		Assert.assertEquals(date, rs.getCreateTime3().toArray()[0]);
		
		Assert.assertNotNull(rs.value2);
		Assert.assertNotNull(rs.score);
		Assert.assertNotNull(rs.char1);
		Assert.assertNotNull(rs.char2);

		Assert.assertNotNull(rs.user);
		Assert.assertNotNull(rs.user1);
		Assert.assertNotNull(rs.user2);
		Assert.assertNotNull(rs.users);
		log.info(new ObjectMapper().writeValueAsString(rs.user));
		log.info(new ObjectMapper().writeValueAsString(rs.user1));
		log.info(new ObjectMapper().writeValueAsString(rs.user2));
		log.info(new ObjectMapper().writeValueAsString(rs.users));
	}

	public static enum Enum1 {
		A, B, C;
	}
	@Data
	public static class Result {
		private Enum1 enum1;
		private Enum1[] enum2;
		private Collection<String> enum3;
		private Set<Enum1> enum4;
		private Map<String, String> enum5;
		private Map<String, Enum1> enum6;
		private Set<Enum1> enum7;

		private String name1;
		private String[] name2;
		private List<String> name3;

		private boolean boolean1;
		private boolean[] boolean2;
		private Boolean boolean3;
		private Boolean[] boolean4;
		private List<Boolean> boolean5;

		private Integer age1;
		private Integer[] age2;
		private Set<Integer> age3;
		private int age4;
		private BigInteger age5;

		private Date createTime;
		private String createTime1;
		private List<Date> createTime2;
		private List<String> createTime3;

		private String value2;

		private String score;
		private String char1;
		private String char2;

		private User user;
		private User user1;
		private User user2;
		private List<User> users;

	}
}
