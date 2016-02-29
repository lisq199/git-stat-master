package io.ologn.gitstat.stat;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.StreamSupport;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jgit.lib.Repository;

import io.ologn.gitstat.jgit.MiscJGitUtils;

/**
 * Objects representing the age of one file over multiple commits
 * @author lisq199
 */
public class FileAgeOverCommits {
	
	/**
	 * The key is the SHA-1 of the commit.
	 */
	protected Map<String, FileAge> map;
	
	/**
	 * Disable default constructor
	 */
	protected FileAgeOverCommits() {}
	
	protected FileAgeOverCommits(Repository repo, boolean ascending) {
		this();
		this.map = MiscJGitUtils.getMapSortedByAuthorTime(repo, ascending);
	}
	
	public int size() {
		return map.size();
	}
	
	public void forEach(BiConsumer<String, FileAge> action) {
		map.forEach(action);
	}

	/**
	 * Get the maximum age among all of the commits in Duration
	 * @return
	 */
	public Duration getMax() {
		return map.values().stream()
				.map(v -> v.getMax())
				.max((x, y) -> x.compareTo(y))
				.get();
	}

	/**
	 * Get the minimum age among all of the commits in Duration
	 * @return
	 */
	public Duration getMin() {
		return map.values().stream()
				.map(v -> v.getMin())
				.min((x, y) -> x.compareTo(y))
				.get();
	}
	
	public Map<String, long[]> getColorPixelsMap() {
		Map<String, long[]> newMap = new LinkedHashMap<String, long[]>();
		forEach((sha1, age) -> newMap.put(sha1, age.getAgesOfLinesInMillis()));
		return newMap;
	}
	
	public Map<String, long[]> getColorPixelsMapSortedByAge(
			boolean ascending) {
		Map<String, long[]> newMap = new LinkedHashMap<String, long[]>();
		forEach((sha1, age) -> {
			long[] sortedAges = age.getAgesOfLinesInMillis();
			Arrays.sort(sortedAges);
			if (!ascending) {
				ArrayUtils.reverse(sortedAges);
			}
			newMap.put(sha1, sortedAges);
		});
		return newMap;
	}
	
	public static FileAgeOverCommits calculate(Repository repo,
			Iterable<FileAge> ages, String filePath, boolean ascending) {
		FileAgeOverCommits faoc = new FileAgeOverCommits(repo, ascending);
		StreamSupport.stream(ages.spliterator(), false)
				.filter(a -> a.getFilePath().equals(filePath))
				.forEach(a -> faoc.map.put(a.getSha1(), a));
		return faoc;
	}

}
