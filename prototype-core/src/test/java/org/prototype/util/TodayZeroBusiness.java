package org.prototype.util;

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.prototype.annotation.Chain;
import org.prototype.core.Prototype;

/**
 * 当天0点业务
 * @author flyxxxxx@163.com
 *
 */

@Prototype
public class TodayZeroBusiness{

	@Chain("now")
	public void business(){
	}

	void now(@TodayZero Date date,@TodayZero Calendar now){
		Assert.assertEquals(date, now.getTime());
	}
	
}
