package io.ologn.gitstat.tokenizer;

import java.util.function.Function;

/**
 * The purpose of this interface is to make it easier to support other 
 * programming languages and tokenizers in the future.
 * @author lisq199
 *
 */
public interface TokenParser {
	
	/**
	 * @return a TokenValue storing information about the value of each token 
	 * based on how important the token is. If the token is unimportant, 
	 * it does not need to be included in the Map, and its value will be 
	 * treated as 0.
	 */
	public TokenValue getTokenValue();
	
	/**
	 * @return a function that takes a String and returns a String. 
	 * The parameter is a line in the source file, and the return value is 
	 * the extracted token.
	 */
	public Function<String, String> parseToken();

}
