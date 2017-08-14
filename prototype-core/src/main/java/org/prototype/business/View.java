package org.prototype.business;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标明这是一个WEB视图. <br>
 * 用在业务类的成员变量上（只允许有一个成员变量有此注解），表示此业务类将要输出网页视图，而不是一个POJO对象的结果.
 * @author lj
 *
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface View {

}
