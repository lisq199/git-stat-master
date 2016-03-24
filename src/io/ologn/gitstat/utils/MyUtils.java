package io.ologn.gitstat.utils;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class MyUtils {
	
	public static String getIso8601FromUnixTime(long epochSecond) {
		Instant instant = Instant.ofEpochSecond(epochSecond);
		return DateTimeFormatter.ISO_INSTANT.format(instant);
	}
	
	public static String getIso8601FromDate(Date date) {
		return getIso8601FromUnixTime(date.getTime() / 1000l);
	}
	
	public static int getYearFromDate(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.YEAR);
	}
	
	public static boolean areInSameYear(Date d1, Date d2) {
		if (d1 == null || d2 == null) {
			return false;
		}
		return getYearFromDate(d1) == getYearFromDate(d2);
	}
	
}
