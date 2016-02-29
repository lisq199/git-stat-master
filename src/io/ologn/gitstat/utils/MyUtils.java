package io.ologn.gitstat.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class MyUtils {
	
	public static final String SPACES_AS_TAB = "    ";
	
	public static final String HTML_DIR = "res" + File.separator + "html"
			+ File.separator;
	
	public static final String JS_RANDOM_COLOR =
			"'rgb(' + (Math.floor(Math.random() * 256)) + ',' + "
			+ "(Math.floor(Math.random() * 256)) + ',' + "
			+ "(Math.floor(Math.random() * 256)) + ')'";
	
	public static void writeToFile(File file, String str, boolean append) {
		try (
			FileWriter fw = new FileWriter(file, append);
			BufferedWriter bw = new BufferedWriter(fw);
		) {
			bw.write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String readFileToString(Path path, Charset charset)
			throws IOException {
		return new String(Files.readAllBytes(path), charset);
	}
	
	public static String readFileToString(Path path) throws IOException {
		return new String(Files.readAllBytes(path));
	}
	
	public static String getIso8601FromUnixTime(long epochSecond) {
		Instant instant = Instant.ofEpochSecond(epochSecond);
		return DateTimeFormatter.ISO_INSTANT.format(instant);
	}
	
	public static String getIso8601FromDate(Date date) {
		return getIso8601FromUnixTime(date.getTime() / 1000l);
	}
	
}
