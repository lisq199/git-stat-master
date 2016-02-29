package io.ologn.gitstat.stat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * The purpose of this class is to count different types of tokens in a file.
 * @author lisq199
 *
 */
public class TokenCounter {
	
	/**
	 * A map storing all the tokens and counters
	 */
	protected Map<String, Integer> map;
	
	public TokenCounter() {
		map = new HashMap<String, Integer>();
	}
	
	/**
	 * Check if a token is already present
	 * @param token
	 * @return
	 */
	public boolean containsToken(String token) {
		return map.containsKey(token);
	}
	
	public Set<String> getTokens() {
		return map.keySet();
	}
	
	public void forEach(BiConsumer<String, Integer> action) {
		map.forEach(action);
	}
	
	/**
	 * Add another token to the counter and set the counter to 0. This 
	 * method does nothing if the token already exists.
	 * @param token
	 */
	public void addToken(String token) {
		if (!containsToken(token)) {
			map.put(token, 0);
		}
	}
	
	/**
	 * Get the count of a token. If the token is not present, then its count 
	 * is 0. 
	 * @param token
	 * @return
	 */
	public int getCount(String token) {
		if (containsToken(token)) {
			return map.get(token).intValue();
		} else {
			return 0;
		}
	}
	
	/**
	 * Increase the count of a token by an integer
	 * @param token
	 * @param n
	 */
	public void increaseCountBy(String token, int n) {
		map.replace(token, map.get(token) + n);
	}
	
	/**
	 * Increase the count of a token by 1
	 * @param token
	 */
	public void increaseCount(String token) {
		increaseCountBy(token, 1);
	}
	
	/**
	 * Reset the count of one or multiple tokens to 0. Tokens that don't 
	 * exist will be skipped.
	 * @param token
	 */
	public void resetToken(String... tokens) {
		Arrays.stream(tokens).filter(t -> containsToken(t))
				.forEach(t -> map.replace(t, 0));
	}
	
	/**
	 * Reset all the tokens to 0
	 */
	public void resetAll() {
		forEach((token, v) -> map.replace(token, 0));
	}
	
	/**
	 * Count a token. If the token does not exist, then the token will be 
	 * created and set to 1. Otherwise, it will be increased by 1.
	 * @param token
	 */
	public void countToken(String token) {
		addToken(token);
		increaseCount(token);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("TokenCounter[");
		forEach((token, count) -> builder.append('"').append(token)
				.append("\": ").append(count).append(", "));
		builder.append("]");
		return builder.toString();
	}
	
	/**
	 * Filter a list (Iterable) of TokenCounter objects by a Predicate
	 * @param counters
	 * @param predicate
	 * @return the resulting list (List)
	 */
	public static List<TokenCounter> filter(Iterable<TokenCounter> counters,
			Predicate<TokenCounter> predicate) {
		return StreamSupport.stream(counters.spliterator(), false)
				.filter(predicate)
				.collect(Collectors.toList());
	}
	
	/**
	 * Filter a list (Iterable) of TokenCounter objects so the result only 
	 * contains once that contain at least one of the specified tokens
	 * @param counters
	 * @param tokens
	 * @return the resulting list (List)
	 */
	public static List<TokenCounter> filterByToken(
			Iterable<TokenCounter> counters, String... tokens) {
		return filter(counters, c -> Arrays.stream(tokens)
				.anyMatch(t -> c.containsToken(t)));
	}

}
