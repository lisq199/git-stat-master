package io.ologn.gitstat.tokenizer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Object storing the value of each token based on how important the token is. 
 * Objects of this class are not supposed to and cannot be modified once they 
 * are created.
 * @author lisq199
 *
 */
public class TokenValue {
	
	private Map<String, Integer> map;
	private int defaultVaule;
	
	/**
	 * Disable default constructor
	 */
	private TokenValue() {}
	
	/**
	 * Constructor
	 * @param map A Map<String, Integer> storing the value of each token. The 
	 * key of the map is the token, and the value of the map is the value of 
	 * the corresponding token. The map should be pre constructed, because 
	 * once the TokenValue object is created, it cannot be modified again.
	 * @param defaultValue
	 */
	public TokenValue(Map<String, Integer> map, int defaultValue) {
		this();
		this.map = new HashMap<String, Integer>(map);
		this.defaultVaule = defaultValue;
	}
	
	/**
	 * Constructor
	 * @param map a Map<String, Integer> storing the value of each token.
	 */
	public TokenValue(Map<String, Integer> map) {
		this(map, 0);
	}

	/**
	 * Check if a token is present.
	 * @param token
	 * @return
	 */
	public boolean containsToken(String token) {
		return map.containsKey(token);
	}
	
	/**
	 * Get the value of a token. If the token is not present, then it means 
	 * that it's unimportant, and therefore its value is the default value.
	 * @param token
	 * @return
	 */
	public int getValue(String token) {
		if (containsToken(token)) {
			return map.get(token).intValue();
		} else {
			return getDefaultValue();
		}
	}
	
	public int getDefaultValue() {
		return defaultVaule;
	}
	
	public void forEach(BiConsumer<String, Integer> action) {
		map.forEach(action);
	}
	
}
