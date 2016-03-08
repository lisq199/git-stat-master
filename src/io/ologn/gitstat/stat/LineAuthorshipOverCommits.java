package io.ologn.gitstat.stat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jgit.lib.Repository;

import io.ologn.gitstat.jgit.MiscJGitUtils;

/**
 * Objects representing the author of each line of code in a file 
 * over commits.
 * @author lisq199
 */
public class LineAuthorshipOverCommits {
	
	protected Map<String, LineAuthorship> map;
	
	protected Map<GitAuthor, Integer> authorIdMap;
	
	protected LineAuthorshipOverCommits() {
		this.authorIdMap = new HashMap<GitAuthor, Integer>();
	}
	
	protected LineAuthorshipOverCommits(Repository repo, boolean ascending) {
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
	
	public void forEach(BiConsumer<String, LineAuthorship> action) {
		map.forEach(action);
	}
	
	/**
	 * Get the author ID of a GitAuthor
	 * @param author
	 * @return
	 */
	public int getAuthorId(GitAuthor author) {
		if (authorIdMap.containsKey(author)) {
			return authorIdMap.get(author);
		} else {
			return -1;
		}
	}
	
	/**
	 * Get the author by the ID. Each author should have a unique ID.
	 * @param id
	 * @return
	 */
	public GitAuthor getAuthorById(int id) {
		for (Map.Entry<GitAuthor, Integer> e : authorIdMap.entrySet()) {
			if (e.getValue() == id) {
				return e.getKey();
			}
		}
		return null;
	}
	
	public List<long[]> getColorPixelsDataArrays() {
		List<long[]> result = new ArrayList<long[]>();
		Function<LineAuthorship, long[]> getIds = a -> {
			long[] data = new long[a.getBlameSize()];
			for (int i = 0; i < data.length; i++) {
				data[i] = this.getAuthorId(a.getAuthorAtLine(i));
			}
			return data;
		};
		forEach((sha1, a) -> result.add(getIds.apply(a)));
		return result;
	}
	
	public List<long[]> getColorPixelsDataArraysSortedByAuthorId() {
		List<long[]> result = new ArrayList<long[]>();
		Function<LineAuthorship, long[]> getSortedIds = a -> {
			long[] data = new long[a.getBlameSize()];
			for (int i = 0; i < data.length; i++) {
				data[i] = this.getAuthorId(a.getAuthorAtLine(i));
			}
			Arrays.sort(data);
			return data;
		};
		forEach((sha1, a) -> result.add(getSortedIds.apply(a)));
		return result;
	}
	
	public List<long[]> getColorPixelsDataArraysSortedByContribution(
			boolean ascending) {
		List<long[]> result = new ArrayList<long[]>();
		Function<LineAuthorship, long[]> getSortedIds = a -> {
			long[] data = new long[a.getBlameSize()];
			for (int i = 0; i < data.length; i++) {
				data[i] = this.getAuthorId(a.getAuthorAtLine(i));
			}
			Long[] sortedData = Arrays.stream(ArrayUtils.toObject(data))
					.sorted((x, y) -> {
						int xLines = a.getNumberOfLinesWrittenBy(
								getAuthorById(x.intValue()));
						int yLines = a.getNumberOfLinesWrittenBy(
								getAuthorById(y.intValue()));
						if (ascending) {
							return Integer.compare(xLines, yLines);
						} else {
							return Integer.compare(yLines, xLines);
						}
					})
					.toArray(Long[]::new);
			return ArrayUtils.toPrimitive(sortedData);
		};
		forEach((sha1, a) -> result.add(getSortedIds.apply(a)));
		return result;
	}
	
	public List<String[]> getColorPixelsTitleArrays() {
		List<String[]> result = new ArrayList<String[]>();
		forEach((sha1, a) -> result.add(Arrays.stream(a.getAuthors())
				.map(author -> author.toString())
				.toArray(size -> new String[size])));
		return result;
	}
	
	public List<String[]> getColorPixelsTitleArraysSortedByAuthorId() {
		List<String[]> result = new ArrayList<String[]>();
		forEach((sha1, a) -> result.add(Arrays.stream(a.getAuthors())
				.sorted((a1, a2) -> Integer.compare(
						getAuthorId(a1), getAuthorId(a2)))
				.map(author -> author.toString())
				.toArray(size -> new String[size])));
		return result;
	}
	
	public List<String[]> getColorPixelsTitleArraysSortedByContribution(
			boolean ascending) {
		List<String[]> result = new ArrayList<String[]>();
		forEach((sha1, a) -> result.add(Arrays.stream(a.getAuthors())
				.sorted((a1, a2) -> {
					int a1Lines = a.getNumberOfLinesWrittenBy(a1);
					int a2Lines = a.getNumberOfLinesWrittenBy(a2);
					if (ascending) {
						return Integer.compare(a1Lines, a2Lines);
					} else {
						return Integer.compare(a2Lines, a1Lines);
					}
				})
				.map(author -> author.toString())
				.toArray(size -> new String[size])));
		return result;
	}
	
	protected void initAuthorIdMap() {
		int id = 0;
		for (LineAuthorship authorship : map.values()) {
			for (GitAuthor author : authorship.getAuthors()) {
				if (!authorIdMap.containsKey(author)) {
					authorIdMap.put(author, id++);
				}
			}
		}
	}
	
	/**
	 * Calculate a LineAuthorshipOverCommits object
	 * @param repo
	 * @param authorships
	 * @param filePath
	 * @param ascending
	 * @return
	 */
	public static LineAuthorshipOverCommits calculate(Repository repo,
			Iterable<LineAuthorship> authorships, String filePath,
			boolean ascending) {
		LineAuthorshipOverCommits laoc = new LineAuthorshipOverCommits(
				repo, ascending);
		StreamSupport.stream(authorships.spliterator(), false)
				.filter(a -> a.getFilePath().equals(filePath))
				.forEach(a -> laoc.map.put(a.getSha1(), a));
		laoc.initAuthorIdMap();
		return laoc;
	}

}
