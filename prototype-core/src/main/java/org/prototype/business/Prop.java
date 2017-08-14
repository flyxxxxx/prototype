package org.prototype.business;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 属性注解
 * 
 * @author lj
 *
 */

@Target({ ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Prop {

	/**
	 * 属性名，未指定将当前成员变量当name，支持输出注解Id的成员变量
	 * 
	 * @return 属性名
	 */
	String name() default "";

	/**
	 * 提示信息
	 * 
	 * @return 提示信息
	 */
	String hint() default "";

	/**
	 * 属性的描述，必须和value成对出现
	 * 
	 * @return 属性的描述
	 */
	String desc();

	/**
	 * 输入数据的最大长度，作为输入参数时才需要，作为输出时仅作为API文档的参考.
	 * 对于数组、集合和映射，是指每个元素的最大长度.
	 * @return 输入数据的最大长度
	 */
	int maxLength() default -1;

	/**
	 * 数值或日期格式或表达式. <br>
	 * 
	 * <pre>
	 * 对于数组、集合和映射，是指每个元素的格式或表达式.
	 * 对日期类型数据，请使用形式yyyy-dd-MM.
	 * 对数值类型数据，可使用0.##形式的表达式.
	 * 可用"method:getValue"形式的表达式指定一个方法作为输入输出的转换：
	 * 输入： 
	 * &#064;Input(@Prop(...,pattern="method:getLength"))
	 * private Integer length;
	 * public Integer getLength(String value){return value.length();}//参数类型必须是字符串，返回值类型和成员变量类型一致
	 * 输出：
	 * &#064;Input(@Prop(...,pattern="method:getString"))
	 * private String description;
	 * public String getString(String value){//参数类型必须是成员变量的类型，返回值必须是字符串
	 *   return value.length&gt;20?(value.substring(0,20)+"..."):value;
	 * }	 * 
	 * 作为输出时还可使用模板类型:表达式，如pattern="el:${user.flag==1?"...":"..."}"
	 * </pre>
	 * 
	 * @return 参数或日期格式
	 */
	String pattern() default "";

	/**
	 * 作为参数时是否必填字段，默认为true
	 * 
	 * @return 作为参数时是否必填字段
	 */
	boolean required() default true;

}
