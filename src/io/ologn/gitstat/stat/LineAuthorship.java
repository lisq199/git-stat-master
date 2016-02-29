package io.ologn.gitstat.stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import io.ologn.common.collect.OlognMaps;
import io.ologn.gitstat.jgit.BlameUtils;

public class LineAuthorship implements BlameResultContainer {
	
	protected GitAuthor[] authors;
	protected Map<GitAuthor, Integer> map;
	
	protected String sha1;
	protected BlameResult blameResult;
	
	protected LineAuthorship() {
		this.authors = new GitAuthor[0];
		this.map = new HashMap<GitAuthor, Integer>();
	}
	
	protected LineAuthorship(String sha1, BlameResult blameResult) {
		this();
		this.sha1 = sha1;
		this.blameResult = blameResult;
	}
	
	@Override
	public BlameResult getBlameResult() {
		return blameResult;
	}
	
	@Override
	public String getSha1() {
		return sha1;
	}
	
	@Override
	public GitAuthor getAuthorAtLine(int i) {
		return authors[i];
	}
	
	public int getNumberOfLinesWrittenBy(GitAuthor author) {
		if (this.map.containsKey(author)) {
			return this.map.get(author);
		} else {
			return 0;
		}
	}
	
	public GitAuthor[] getAuthors() {
		return authors.clone();
	}
	
	public Comparator<GitAuthor> getGitAuthorComparatorByNumberOfLines(
			boolean ascending) {
		return (a, b) -> {
			int aLines = getNumberOfLinesWrittenBy(a);
			int bLines = getNumberOfLinesWrittenBy(b);
			if (ascending) {
				return Integer.compare(aLines, bLines);
			} else {
				return Integer.compare(bLines, aLines);
			}
		};
	}
	
	public GitAuthor[] getAuthorsSortedByNumberOfLines(boolean ascending) {
		GitAuthor[] sortedAuthors = getAuthors();
		Arrays.sort(authors, getGitAuthorComparatorByNumberOfLines(ascending));
		return sortedAuthors;
	}
	
	public GitAuthor getAuthorWithMostLines() {
		return Arrays.stream(getAuthors())
				.max(getGitAuthorComparatorByNumberOfLines(true))
				.get();
	}
	
	public GitAuthor getAuthorWithLeastLines() {
		return Arrays.stream(getAuthors())
				.min(getGitAuthorComparatorByNumberOfLines(true))
				.get();
	}
	
	public Map<GitAuthor, Integer> getMapSortedByNumberOfLines(
			boolean ascending) {
		return OlognMaps.sortByValue(map, ascending);
	}
	
	protected void countLine(GitAuthor author) {
		int count = 1;
		if (this.map.containsKey(author)) {
			count += map.get(author);
		}
		map.put(author, count);
	}
	
	protected void initAuthors() {
		int size = this.getBlameSize();
		this.authors = new GitAuthor[size];
		for (int i = 0; i < size; i++) {
			GitAuthor a = BlameResultContainer.super.getAuthorAtLine(i);
			this.authors[i] = a;
			this.countLine(a);
		}
	}
	
	public static LineAuthorship calculate(
			String sha1, BlameResult blameResult) {
		LineAuthorship authorship = new LineAuthorship(sha1, blameResult);
		authorship.initAuthors();
		return authorship;
	}
	
	public static LineAuthorship calculate(BlameResultContainer container) {
		return calculate(container.getSha1(), container.getBlameResult());
	}
	
	public static LineAuthorship calculate(Git git, AnyObjectId commitId,
			String filePath) throws GitAPIException {
		BlameResult blameResult = BlameUtils.getBlameResult(git, commitId,
				filePath);
		return calculate(commitId.getName(), blameResult);
	}
	
	public static LineAuthorship calculate(Git git, Repository repo,
			String revstr, String filePath) throws RevisionSyntaxException,
	AmbiguousObjectException, IncorrectObjectTypeException, GitAPIException,
	IOException {
		BlameResult blameResult = BlameUtils.getBlameResult(git, repo,
				revstr, filePath);
		return calculate(repo.resolve(revstr).getName(), blameResult);
	}
	
	public static LineAuthorship calculate(Git git, Repository repo,
			RevCommit commit, String filePath) throws RevisionSyntaxException,
	AmbiguousObjectException, IncorrectObjectTypeException, GitAPIException,
	IOException {
		BlameResult blameResult = BlameUtils.getBlameResult(git, repo,
				commit, filePath);
		return calculate(commit.getName(), blameResult);
	}
	
	public static List<LineAuthorship> calculateMultiple(Git git,
			Repository repo, Iterable<RevCommit> commits, String filePath)
					throws RevisionSyntaxException, AmbiguousObjectException,
					IncorrectObjectTypeException, GitAPIException,
					IOException {
		List<LineAuthorship> list = new ArrayList<LineAuthorship>();
		for (RevCommit commit : commits) {
			list.add(calculate(git, repo, commit, filePath));
		}
		return list;
	}
	
	public static List<LineAuthorship> calculateMultiple(
			Iterable<BlameResultContainer> containers) {
		List<LineAuthorship> list = new ArrayList<LineAuthorship>();
		for (BlameResultContainer container : containers) {
			list.add(calculate(container));
		}
		return list;
	}
	
}
