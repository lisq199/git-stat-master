package io.ologn.gitstat.stat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
	
	/**
	 * Get the number of commits
	 * @return
	 */
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
	
	public List<long[]> getColorPixelsDataArrays() {
		List<long[]> result = new ArrayList<long[]>();
		forEach((sha1, age) -> result.add(age.getAgesOfLinesInMillis()));
		return result;
	}
	
	public List<long[]> getColorPixelsDataArraysSortedByAge(
			boolean ascending) {
		List<long[]> result = new ArrayList<long[]>();
		forEach((sha1, age) -> {
			long[] sortedAges = age.getAgesOfLinesInMillis();
			Arrays.sort(sortedAges);
			if (!ascending) {
				ArrayUtils.reverse(sortedAges);
			}
			result.add(sortedAges);
		});
		return result;
	}
	
	/**
	 * Calculate a FileAgeOverCommits object
	 * @param repo
	 * @param ages
	 * @param filePath
	 * @param ascending
	 * @return
	 */
	public static FileAgeOverCommits calculate(Repository repo,
			Iterable<FileAge> ages, String filePath, boolean ascending) {
		FileAgeOverCommits faoc = new FileAgeOverCommits(repo, ascending);
		StreamSupport.stream(ages.spliterator(), false)
				.filter(a -> a.getFilePath().equals(filePath))
				.forEach(a -> faoc.map.put(a.getSha1(), a));
		return faoc;
	}

}
