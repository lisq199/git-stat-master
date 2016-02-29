package io.ologn.gitstat.stat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import org.eclipse.jgit.lib.Repository;

import io.ologn.gitstat.jgit.MiscJGitUtils;

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
	
	public int size() {
		return map.size();
	}
	
	public void forEach(BiConsumer<String, LineAuthorship> action) {
		map.forEach(action);
	}
	
	public int getAuthorId(GitAuthor author) {
		if (authorIdMap.containsKey(author)) {
			return authorIdMap.get(author);
		} else {
			return -1;
		}
	}
	
	public Map<String, long[]> getColorPixelMap() {
		Map<String, long[]> newMap = new LinkedHashMap<String, long[]>();
		Function<LineAuthorship, long[]> getIds = a -> {
			long[] data = new long[a.getBlameSize()];
			for (int i = 0; i < data.length; i++) {
				data[i] = this.getAuthorId(a.getAuthorAtLine(i));
			}
			return data;
		};
		forEach((sha1, a) -> newMap.put(sha1, getIds.apply(a)));
		return newMap;
	}
	
	public Map<String, long[]> getColorPixelMapSortedByAuthor() {
		Map<String, long[]> newMap = new LinkedHashMap<String, long[]>();
		Function<LineAuthorship, long[]> getSortedIds = a -> {
			long[] data = new long[a.getBlameSize()];
			for (int i = 0; i < data.length; i++) {
				data[i] = this.getAuthorId(a.getAuthorAtLine(i));
			}
			Arrays.sort(data);
			return data;
		};
		forEach((sha1, a) -> newMap.put(sha1, getSortedIds.apply(a)));
		return newMap;
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
