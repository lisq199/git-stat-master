package io.ologn.gitstat.utils;

import java.sql.Date;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class MyUtils {
	
	public static String getIso8601FromUnixTime(long epochSecond) {
		Instant instant = Instant.ofEpochSecond(epochSecond);
		return DateTimeFormatter.ISO_INSTANT.format(instant);
	}
	
	public static String getIso8601FromDate(Date date) {
		return getIso8601FromUnixTime(date.getTime() / 1000l);
	}
	
}
