package org.prototype.template;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用模板渲染数据. <br>
 * 
 * <pre>
   &#064;Template("尊敬的客户${user.name}，您的验证码为${code}，五分钟内有效")
   String getSMS(Map&lt;String,Object&gt; params){
  	 return null;//一般不需要做处理，但也可在此方法中对params进行处理，使用模板时，以处理后的params中的数据为准.
   }
          也可以使用外部文件，如：
   &#064;Template(file="classpath:com/zenking.prototype/sms.txt")
   &#064;Template(file="/usr/template/sms.txt")
 * </pre>
 * 
 * @author lj
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Template {
	/**
	 * //模板内容，与file属性二选一。以key:开头表示将调用TemplateService
	 * 
	 * @return 模板内容
	 */
	String value() default "";

	/**
	 * 模板文件，支持classpath：或以/开头的绝对路径，或是相对于加此注解的类的路径.
	 * 
	 * @return 模板文件
	 */
	String file() default "";

	/**
	 * 模板引擎名称，默认为el(jsp el 表达式)，也可以是js
	 * 
	 * @return 模板引擎名称
	 */
	String engine() default "el";

	/**
	 * 模板文件编码（默认为UTF-8）
	 * 
	 * @return 模板文件编码
	 */
	String encoding() default "UTF-8";// 文件的编码
}
