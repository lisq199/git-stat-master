package io.ologn.gitstat.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.commons.lang3.SystemUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import io.ologn.gitstat.tokenizer.BetterParserC;
import io.ologn.gitstat.tokenizer.TokenParser;
import io.ologn.gitstat.vis.VisRunner;

public class ConfigRunner {
	
	protected static final String DEFAULT_CONFIG_NAME = "config.json";
	
	protected static final String DEFAULT_CONFIG_PATH = DEFAULT_CONFIG_NAME;
	
	/**
	 * Print out an error message and exit
	 * @param errMsg
	 */
	static void err(String errMsg) {
		System.err.println("[error] " + errMsg);
		System.exit(-1);
	}
	
	/**
	 * Ask the user for the path to the config file. If the user 
	 * doesn't provide one, the default path will be used.
	 * @param scanner
	 * @return
	 */
	static Path getConfigPath(Scanner scanner) {
		System.out.println("Please enter the path to your config file: "
				+ "(If no path is entered, "
				+ DEFAULT_CONFIG_NAME + " in your "
				+ "current working directory will be used.)");
		String configPath = scanner.nextLine();
		if (configPath.isEmpty()) {
			configPath = DEFAULT_CONFIG_PATH;
		}
		Path configFilePath = Paths.get(configPath);
		if (!Files.exists(configFilePath)) {
			err("Config file " + configFilePath + " does not exist.");
		}
		return configFilePath;
	}
	
	static JSONObject getConfig() {
		JSONObject config = null;
		try (
			Scanner stdin = new Scanner(System.in);
		) {
			Path configPath = getConfigPath(stdin);
			config = ConfigReader.read(configPath);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return config;
	}
	
	static String getDotGitPath(JSONObject config) {
		String repoPath = ConfigReader.getString(config, "repoPath");
		String dotGitPath = SystemUtils.USER_HOME + File.separator + repoPath;
		if (!Files.exists(Paths.get(dotGitPath))) {
			err(".git file not found.");
		}
		return dotGitPath;
	}
	
	static TokenParser getTokenParser(JSONObject config) {
		String tokenizerName = ConfigReader.getString(config, "tokenizer");
		if (tokenizerName.equalsIgnoreCase("BetterParserC")) {
			return new BetterParserC();
		} else {
			err("Tokenizer " + tokenizerName + " not supported");
			return null;
		}
	}
	
	public static void run(String[] args) {
		JSONObject config = getConfig();
		String dotGitPath = getDotGitPath(config);
		
		VisRunner visRunner = VisRunner.init(dotGitPath);
		
		int visType = ConfigReader.getInt(config, "visType");
		
		String filePath;
		String revstr;
		TokenParser parser;
		boolean combineSmallValues;
		double percentageForOther;
		boolean sortByAge;
		boolean sortByAgeAscending;
		boolean sortByAuthor;
		boolean sortByAuthorContribution;
		int pixelHeight;
		int pixelWidth;
		boolean displayLegend;
		boolean displayBookmarks;
		String bookmarkType;
		boolean displayVertical;
		
		switch (visType) {
		case 1:
			filePath = ConfigReader.getString(config, "filePath");
			revstr = ConfigReader.getString(config, "revisionString");
			parser = getTokenParser(config);
			combineSmallValues = ConfigReader.getBoolean(config,
					"combineSmallValues");
			percentageForOther = ConfigReader.getDouble(config,
					"percentageForOther");
			visRunner.type1(filePath, revstr, parser,
					combineSmallValues, percentageForOther);
			break;
		case 2:
			filePath = ConfigReader.getString(config, "filePath");
			sortByAge = ConfigReader.getBoolean(config, "sortByAge");
			sortByAgeAscending = ConfigReader.getBoolean(config,
					"sortByAgeAscending");
			pixelHeight = ConfigReader.getInt(config, "pixelHeight");
			pixelWidth = ConfigReader.getInt(config, "pixelWidth");
			displayBookmarks = ConfigReader.getBoolean(config,
					"displayBookmarks");
			bookmarkType = ConfigReader.getString(config, "bookmarkType");
			displayLegend = ConfigReader.getBoolean(config, "displayLegend");
			displayVertical = ConfigReader.getBoolean(config,
					"displayVertical");
			visRunner.type2(filePath, sortByAge, sortByAgeAscending,
					pixelHeight, pixelWidth, displayBookmarks, bookmarkType,
					displayLegend, displayVertical);
			break;
		case 3:
			filePath = ConfigReader.getString(config, "filePath");
			sortByAuthor = ConfigReader.getBoolean(config, "sortByAuthor");
			sortByAuthorContribution = ConfigReader.getBoolean(
					config, "sortByAuthorContribution");
			pixelHeight = ConfigReader.getInt(config, "pixelHeight");
			pixelWidth = ConfigReader.getInt(config, "pixelWidth");
			displayLegend = ConfigReader.getBoolean(config, "displayLegend");
			displayBookmarks = ConfigReader.getBoolean(config,
					"displayBookmarks");
			bookmarkType = ConfigReader.getString(config, "bookmarkType");
			displayVertical = ConfigReader.getBoolean(config,
					"displayVertical");
			visRunner.type3(filePath, sortByAuthor, sortByAuthorContribution,
					pixelHeight, pixelWidth, displayBookmarks, bookmarkType,
					displayLegend, displayVertical);
			break;
		default:
			err("Invalid property: visType.");
			break;
		}
	}

}
