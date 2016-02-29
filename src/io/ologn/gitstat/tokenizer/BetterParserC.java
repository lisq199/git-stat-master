package io.ologn.gitstat.tokenizer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * TokenParser for Better Parser for C, as a sample implementation.
 * @author lisq199
 *
 */
public class BetterParserC implements TokenParser {

	@Override
	public TokenValue getTokenValue() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		//map.put("DECL|function", 1);
		//map.put("suffix:semicolon", 2);
		return new TokenValue(map, 1);
	}
	
	@Override
	public Function<String, String> parseToken() {
		return BetterParserC::parseToken;
	}

	public static String parseToken(String line) {
		String l = line.trim(); // Just in case
		int index; // index is the index of '|'
		if (l.startsWith("DECL|")) {
			// If it starts with "DECL|", then ignore the first '|'
			index = l.indexOf('|', 5);
		} else {
			index = l.indexOf('|');
		}
		if (index <= 0) {
			return l;
		} else {
			return l.substring(0, index);
		}
	}
	
}
