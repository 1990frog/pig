package com.clinbrain.dip;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Liaopan on 2020/8/5 0005.
 */
public class Test2 {

	@Test
	public void test() throws ParseException {
		TimeZone timeZone = TimeZone.getTimeZone(ZoneId.of("Asia/Shanghai"));
		Calendar calendar = Calendar.getInstance(timeZone);
		calendar.set(1990,Calendar.SEPTEMBER,5, 0, 0, 0);
//		calendar.setTime(DateUtils.parseDate("1990-09-05","yyyy-MM-dd"));
		System.out.println(timeZone.inDaylightTime(calendar.getTime()));
		final String format = DateFormatUtils.format(calendar.getTime(), "yyyy-MM-dd HH:mm:ss",timeZone);
		System.out.println(format);
	}

	@Test
	public void test2() {

	}
}
