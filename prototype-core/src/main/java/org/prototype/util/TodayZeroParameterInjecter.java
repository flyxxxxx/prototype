package org.prototype.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.prototype.core.ParameterInjecter;
import org.springframework.stereotype.Component;

/**
 * 获得今天0点的注解实现. <br>
 * @see TodayZero
 * @author flyxxxxx@163.com
 *
 */
@Component
public class TodayZeroParameterInjecter implements ParameterInjecter<TodayZero,Object>{

	/**
	 * 只支持java.util.Date类型及子类型或java.util.Calendar类型的参数
	 */
	@Override
	public List<String> validate(TodayZero annotation,Class<Object> type) {
		if(!Date.class.isAssignableFrom(type)&&!Calendar.class.equals(type)){
			return Arrays.asList("util.today.zero");
		}
		return new ArrayList<String>();
	}

	/**
	 * 根据类型获取当天0点值
	 */
	@Override
	public Object inject(TodayZero annotation,Class<Object> type) {
		Calendar cal=Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		if(Calendar.class.equals(type)){
			return cal;
		}
		try {
			return type.getConstructor(long.class).newInstance(cal.getTimeInMillis());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new UnsupportedOperationException(e);
		}
	}

}
