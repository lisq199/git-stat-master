package io.ologn.gitstat.stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import io.ologn.gitstat.jgit.BlameUtils;

/**
 * Statistics for a commit. 
 * Under normal circumstances, objects of this class 
 * should not be modified once they are created, although they can be just 
 * in case. To create an object, use FileCommitStat.calculate() instead of the 
 * default constructor.
 * @author lisq199
 *
 */
public class FileCommitStat implements BlameResultContainer {
	
	protected Map<GitAuthor, TokenCounter> map;
	protected String sha1;
	
	protected BlameResult blameResult;
	
	/**
	 * Disable default constructor
	 */
	protected FileCommitStat() {
		blameResult = null;
	}
	
	protected FileCommitStat(String sha1) {
		this();
		map = new HashMap<GitAuthor, TokenCounter>();
		this.sha1 = sha1;
	}
	
	public Map<GitAuthor, TokenCounter> getMap() {
		return new HashMap<GitAuthor, TokenCounter>(map);
	}
	
	/**
	 * Get the SHA-1 of the commit
	 * @return
	 */
	@Override
	public String getSha1() {
		return sha1;
	}
	
	@Override
	public BlameResult getBlameResult() {
		return blameResult;
	}
	
	protected void setBlameResult(BlameResult blameResult) {
		this.blameResult = blameResult;
	}

	/**
	 * Check if an author is already present
	 * @param author
	 * @return
	 */
	public boolean containsAuthor(GitAuthor author) {
		return map.containsKey(author);
	}
	
	/**
	 * Get all the authors in the FileCommitStat.
	 * Note: The author time for these authors may not be meaningful.
	 * @return a Set storing all the GitAuthor objects
	 */
	public Set<GitAuthor> getAuthors() {
		return map.keySet();
	}
	
	public void forEach(BiConsumer<GitAuthor, TokenCounter> action) {
		map.forEach(action);
	}
	
	/**
	 * Add a new author. If the author is already present, this method 
	 * will do nothing.
	 * @param author
	 */
	public void addAuthor(GitAuthor author) {
		if (!containsAuthor(author)) {
			map.put(author, new TokenCounter());
		}
	}
	
	/**
	 * Get the TokenCounter for an author
	 * @param author
	 * @return
	 */
	public TokenCounter getTokenCounter(GitAuthor author) {
		return map.get(author);
	}
	
	/**
	 * Increase the count of a token by 1 for an author. If the author/token 
	 * does not exist, it will be created.
	 * @param author
	 * @param token
	 */
	public void countToken(GitAuthor author, String token) {
		addAuthor(author);
		getTokenCounter(author).countToken(token);
	}
	
	/**
	 * Reset the TokenCounter for one or more authors. Authors that don't 
	 * exist will be skipped.
	 * @param author
	 */
	public void resetAuthor(GitAuthor... authors) {
		Arrays.stream(authors).filter(a -> containsAuthor(a))
				.forEach(a -> map.replace(a, new TokenCounter()));
	}
	
	/**
	 * Reset the TokenCounter for each author
	 */
	public void resetAll() {
		forEach((author, v) -> map.replace(author, new TokenCounter()));
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("FileCommitStat[\n");
		builder.append("\tSHA-1[").append(getSha1()).append("],\n");
		builder.append("\tfilePath[").append(getFilePath()).append("],\n");
		forEach((author, counter) -> builder.append("\t").append(author)
				.append(": ").append(counter).append(",\n"));
		builder.append("]");
		return builder.toString();
	}
	
	/**
	 * Calculate the FileCommitStat for an author and one file in a commit.
	 * @param git
	 * @param commitId
	 * @param filePath
	 * @param parseToken a function that takes a String and returns a String. 
	 * The parameter is a line in the source file, and the return value is 
	 * the extracted token.
	 * @return the resulting FileCommitStat
	 * @throws GitAPIException
	 */
	public static FileCommitStat calculate(Git git,
			AnyObjectId commitId, String filePath,
			Function<String, String> parseToken) throws GitAPIException {
		FileCommitStat stat = new FileCommitStat(commitId.getName());
		BlameResult blameResult = BlameUtils.getBlameResult(
				git, commitId, filePath);
		if (blameResult == null) {
			return stat;
		}
		stat.setBlameResult(blameResult);
		RawText rawText = blameResult.getResultContents();
		int size = rawText.size();
		for (int i = 0; i < size; i++) {
			GitAuthor author = new GitAuthor(blameResult.getSourceAuthor(i));
			String line = rawText.getString(i);
			String token = parseToken.apply(line);
			stat.countToken(author, token);
		}
		return stat;
	}
	
	public static FileCommitStat calculate(Git git, Repository repo,
			String revstr, String filePath,
			Function<String, String> parseToken)
					throws RevisionSyntaxException, AmbiguousObjectException,
					IncorrectObjectTypeException, IOException,
					GitAPIException {
		return calculate(git, repo.resolve(revstr), filePath,
				parseToken);
	}
	
	public static FileCommitStat calculate(Git git, Repository repo,
			RevCommit commit, String filePath,
			Function<String, String> parseToken)
					throws RevisionSyntaxException, AmbiguousObjectException,
					IncorrectObjectTypeException, IOException,
					GitAPIException {
		return calculate(git, repo, commit.getName(), filePath, parseToken);
	}
	
	/**
	 * Calculate the FileCommitStat objects for an author and a file in a list 
	 * (Iterable) of commits.
	 * @param git
	 * @param repo
	 * @param commits
	 * @param filePath
	 * @param parseToken
	 * @return
	 * @throws RevisionSyntaxException
	 * @throws AmbiguousObjectException
	 * @throws IncorrectObjectTypeException
	 * @throws IOException
	 * @throws GitAPIException
	 */
	public static List<FileCommitStat> calculateMultiple(Git git,
			Repository repo, Iterable<RevCommit> commits, String filePath,
			Function<String, String> parseToken)
					throws RevisionSyntaxException, AmbiguousObjectException,
					IncorrectObjectTypeException, IOException,
					GitAPIException {
		List<FileCommitStat> stats = new ArrayList<FileCommitStat>();
		for (RevCommit commit : commits) {
			stats.add(calculate(git, repo, commit, filePath, parseToken));
		}
		return stats;
	}
	
	/**
	 * Filter a list (Iterable) of FileCommitStat objects by a Predicate
	 * @param stats
	 * @param predicate
	 * @return the resulting List of FileCommitStat
	 */
	public static List<FileCommitStat> filter(Iterable<FileCommitStat> stats,
			Predicate<FileCommitStat> predicate) {
		return StreamSupport.stream(stats.spliterator(), false)
				.filter(predicate)
				.collect(Collectors.toList());
	}
	
	/**
	 * Filter a list (Iterable) of FileCommitStat objects so the result only 
	 * contains ones that contain at least one of the specified authors.
	 * @param stats
	 * @param authors
	 * @return
	 */
	public static List<FileCommitStat> filterByAuthor(
			Iterable<FileCommitStat> stats, GitAuthor... authors) {
		return filter(stats, s -> Arrays.stream(authors)
				.anyMatch(a -> s.containsAuthor(a)));
	}

}
