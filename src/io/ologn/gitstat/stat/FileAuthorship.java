package io.ologn.gitstat.stat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import org.eclipse.jgit.blame.BlameResult;

import io.ologn.gitstat.tokenizer.TokenValue;

/**
 * Data for the authorship of a file. Objects of this class should not 
 * be modified. To create an object, use FileAuthorship.calculate() 
 * instead of the default constructor, although the default constructor is 
 * inaccessible anyway.
 * @author lisq199
 *
 */
public class FileAuthorship implements BlameResultContainer {
	
	/**
	 * The key is the author, and the value is how much score the author has 
	 * got.
	 */
	protected Map<GitAuthor, Integer> map;
	protected int totalScore;
	protected String sha1;
	protected BlameResult blameResult;
	
	protected FileAuthorship() {
		map = new HashMap<GitAuthor, Integer>();
		totalScore = 0;
	}
	
	protected FileAuthorship(String sha1, BlameResult blameResult) {
		this();
		this.sha1 = sha1;
		this.blameResult = blameResult;
	}
	
	protected FileAuthorship(BlameResultContainer container) {
		this(container.getSha1(), container.getBlameResult());
	}
	
	@Override
	public BlameResult getBlameResult() {
		return blameResult;
	}

	@Override
	public String getSha1() {
		return sha1;
	}
	
	/**
	 * Return a copy of the map
	 * @return
	 */
	public Map<GitAuthor, Integer> getMap() {
		return new HashMap<GitAuthor, Integer>(map);
	}
	
	/**
	 * Check if an author is already present
	 * @param author
	 * @return
	 */
	public boolean containsAuthor(GitAuthor author) {
		return map.containsKey(author);
	}
	
	public Set<GitAuthor> getAuthors() {
		return map.keySet();
	}
	
	/**
	 * TreeMap cannot sort entries by value. So it's being sorted here.
	 * @param ascending
	 * @param action
	 */
	public void forEach(boolean ascending,
			BiConsumer<GitAuthor, Integer> action) {
		map.entrySet().stream().sorted((e1, e2) -> {
			Integer v1 = e1.getValue();
			Integer v2 = e2.getValue();
			if (ascending) {
				return v1.compareTo(v2);
			} else {
				return v2.compareTo(v1);
			}
		}).forEach(e -> action.accept(e.getKey(), e.getValue()));
	}
	
	/**
	 * Same as map.forEach(), but it will be sorted by value (score) in 
	 * descending order.
	 * @param action
	 */
	public void forEach(BiConsumer<GitAuthor, Integer> action) {
		forEach(false, action);
	}
	
	public void addAuthor(GitAuthor author) {
		if (!containsAuthor(author)) {
			map.put(author, 0);
		}
	}
	
	/**
	 * Get the score of an author. If the author is not present, the score 
	 * is considered to be 0.
	 * @param author
	 * @return
	 */
	public int getScore(GitAuthor author) {
		if (containsAuthor(author)) {
			return map.get(author).intValue();
		} else {
			return 0;
		}
	}
	
	public void increseScoreBy(GitAuthor author, int n) {
		map.replace(author, getScore(author) + n);
		totalScore += n;
	}
	
	public void resetAuthor(GitAuthor... authors) {
		Arrays.stream(authors).filter(a -> containsAuthor(a)).forEach(a -> {
			totalScore -= getScore(a);
			map.replace(a, 0);
		});
	}
	
	public void resetAll() {
		forEach((author, score) -> map.replace(author, 0));
		totalScore = 0;
	}
	
	public int getTotalScore() {
		return totalScore;
	}
	
	public double getPercentage(GitAuthor author) {
		return 100.0 * getScore(author) / getTotalScore();
	}

	/**
	 * Modifying this map will not change the original object, since it's 
	 * a copy.
	 * @return
	 */
	public Map<String, Double> getChartJsPieMap() {
		// Use LinkedHashMap to preserve the ordering
		Map<String, Double> newMap = new LinkedHashMap<String, Double>();
		this.forEach((author, score) -> {
			newMap.put(author.getName() + " (" + author.getEmail() + ")",
					this.getPercentage(author));
		});
		return newMap;
	}
	
	/**
	 * Get the map for generating Chart.js pie chart data.
	 * @param otherPercentage authors with little contribution will be 
	 * combined into "other". This parameter is the upper bound of "other".
	 * @return
	 */
	public Map<String, Double> getTrimmedChartJsPieMap(
			double otherPercentage) {
		Map<String, Double> pieMap = getChartJsPieMap();
		Map<String, Double> newMap = new LinkedHashMap<String, Double>();
		double totalPercentage = 0;
		for (Map.Entry<String, Double> e : pieMap.entrySet()) {
			double percentage = e.getValue();
			totalPercentage += percentage;
			if (totalPercentage > 100 - otherPercentage) {
				newMap.put("Other", 100.0 - totalPercentage);
				break;
			}
			newMap.put(e.getKey(), percentage);
		}
		return newMap;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("FileAuthorship[\n");
		builder.append("\tSHA1[").append(getSha1()).append("],\n");
		builder.append("\tfilePath[").append(getFilePath()).append("],\n");
		forEach((author, score) -> {
			builder.append("\t").append(author).append(": ").append(score)
					.append(" - ").append(getPercentage(author))
					.append("%,\n");
		});
		builder.append("]");
		return builder.toString();
	}
	
	/**
	 * Calculate the FileAuthorship from a FileCommitStat
	 * @param tokenValue
	 * @param stat
	 * @return
	 */
	public static FileAuthorship calculate(TokenValue tokenValue,
			FileCommitStat stat) {
		FileAuthorship authorship = new FileAuthorship(stat);
		stat.forEach((author, counter) -> {
			authorship.addAuthor(author);
			counter.forEach((token, count) -> {
				int value = tokenValue.getValue(token);
				int totalValue = value * count;
				authorship.increseScoreBy(author, totalValue);
			});
		});
		return authorship;
	}
	
	/**
	 * Calculate multiple TokenParser objects from a list (Iterable) of 
	 * FileCommitStat objects
	 * @param tokenValue
	 * @param stats
	 * @return
	 */
	public static List<FileAuthorship> calculateMultiple(TokenValue tokenValue,
			Iterable<FileCommitStat> stats) {
		List<FileAuthorship> result = new ArrayList<FileAuthorship>();
		for (FileCommitStat stat : stats) {
			result.add(calculate(tokenValue, stat));
		}
		return result;
	}
	
}
