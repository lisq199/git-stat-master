package io.ologn.gitstat.stat;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.stream.StreamSupport;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;

import io.ologn.gitstat.jgit.MiscJGitUtils;
import io.ologn.gitstat.utils.MyUtils;
import io.ologn.gitstat.vis.ColorPixels;

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
		forEach((sha1, age) -> result.add(age.getAgesOfLinesInDays()));
		return result;
	}
	
	public List<long[]> getColorPixelsDataArraysSortedByAge(
			boolean ascending) {
		List<long[]> result = new ArrayList<long[]>();
		forEach((sha1, age) -> {
			long[] sortedAges = age.getAgesOfLinesInDays();
			Arrays.sort(sortedAges);
			if (!ascending) {
				ArrayUtils.reverse(sortedAges);
			}
			result.add(sortedAges);
		});
		return result;
	}
	
	public List<String[]> getColorPixelTitleArrays() {
		List<String[]> result = new ArrayList<String[]>();
		forEach((sha1, age) -> {
			String[] titles = new String[age.getBlameSize()];
			for (int i = 0; i < titles.length; i++) {
				titles[i] = age.getAgeOfLine(i).toDays() + " days";
			}
			result.add(titles);
		});
		return result;
	}
	
	public List<String[]> getColorPixelTitleArraysSortedByAge(
			boolean ascending) {
		List<String[]> result = new ArrayList<String[]>();
		forEach((sha1, age) -> {
			Duration[] sortedAges = age.getAgesOfLines();
			if (ascending) {
				Arrays.sort(sortedAges);
			} else {
				Arrays.sort(sortedAges, (a, b) -> b.compareTo(a));
			}
			String[] titles = new String[sortedAges.length];
			for (int i = 0; i < titles.length; i++) {
				titles[i] = sortedAges[i].toDays() + " days";
			}
			result.add(titles);
		});
		return result;
	}
	
	public Map<Long, String> getColorPixelsTitleMap() {
		Map<Long, String> titleMap = new TreeMap<Long, String>(Long::compare);
		forEach((sha1, age) -> {
			long[] agesInDays = age.getAgesOfLinesInDays();
			for (int i = 0; i < agesInDays.length; i++) {
				if (!titleMap.containsKey(agesInDays[i])) {
					String title = "Age of the line: " 
							+ agesInDays[i] + " days";
					titleMap.put(agesInDays[i], title);
				}
			}
		});
		return titleMap;
	}
	
	public List<String> getColorPixelsDatasetDescriptions(
			Repository repo) throws MissingObjectException,
	IncorrectObjectTypeException, IOException {
		List<String> result = new ArrayList<String>();
		for (String sha1 : map.keySet()) {
			StringBuilder builder = new StringBuilder();
			builder.append("SHA-1: ").append(sha1).append(ColorPixels.HTML_LF);
			Date authorDate = MiscJGitUtils.getAuthorTimeFromSha1(repo, sha1);
			builder.append("Commit Author Date: ").append(authorDate)
					.append(ColorPixels.HTML_LF);
			long commitSeconds = MiscJGitUtils.getCommitTimeFromSha1(
					repo, sha1);
			Date commitDate = new Date(commitSeconds * 1000l);
			builder.append("Commit Time: ").append(commitDate);
			result.add(builder.toString());
		}
		return result;
	}
	
	public Map<Integer, String> getColorPixelsBookmarkMap(
			Repository repo) throws MissingObjectException,
	IncorrectObjectTypeException, IOException {
		Map<Integer, String> bookmarkMap = new HashMap<Integer, String>();
		
		int i = 0;
		Date currentDate, previousDate = null;
		for (String sha1 : map.keySet()) {
			currentDate = MiscJGitUtils.getAuthorTimeFromSha1(repo, sha1);
			if (!MyUtils.areInSameYear(currentDate, previousDate)) {
				bookmarkMap.put(i, "" + MyUtils.getYearFromDate(currentDate));
			}
			previousDate = currentDate;
			i++;
		}
		return bookmarkMap;
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
