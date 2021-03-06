package io.ologn.gitstat.config;

import static io.ologn.gitstat.config.ConfigRunner.err;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Methods for reading the (slightly non-standard) JSON config 
 * file.
 * @author lisq199
 */
public class ConfigReader {
	
	/**
	 * Get rid of all the non-standard comments so it can be 
	 * parsed by a JSON parser.
	 * @param configString
	 * @return
	 */
	public static String parseConfigString(String configString) {
		String[] lines = configString.split("\n");
		return Arrays.stream(lines)
				.filter(s -> !s.trim().startsWith("//"))
				.collect(Collectors.joining("\n"));
	}
	
	/**
	 * Get a JSONObject from a config string
	 * @param path
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public static JSONObject read(Path path)
			throws IOException, ParseException {
		JSONParser parser = new JSONParser();
		String configString = FileUtils.readFileToString(path.toFile(),
				Charset.defaultCharset());
		configString = parseConfigString(configString);
		JSONObject config = (JSONObject) parser.parse(configString);
		return config;
	}
	
	public static boolean getBoolean(JSONObject config, Object key) {
		Boolean b = (Boolean) config.get(key);
		if (b == null) {
			err("Property + " + key + " not found.");
		}
		return b;
	}
	
	public static long getLong(JSONObject config, Object key) {
		Long l = (Long) config.get(key);
		if (l == null) {
			err("Property " + key + " not found.");
		}
		return l;
	}
	
	public static int getInt(JSONObject config, Object key) {
		return (int) getLong(config, key);
	}

	public static double getDouble(JSONObject config, Object key) {
		Double d = (Double) config.get(key);
		if (d == null) {
			err("Property " + key + " not found.");
		}
		return d;
	}

	public static String getString(JSONObject config, Object key) {
		String s = (String) config.get(key);
		if (s == null) {
			err("Property " + key + " not found.");
		}
		return s;
	}
	
}
