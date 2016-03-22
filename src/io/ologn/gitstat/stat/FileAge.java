package io.ologn.gitstat.stat;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import io.ologn.common.collect.OlognArrays;
import io.ologn.common.type.OlognDurations;
import io.ologn.gitstat.jgit.BlameUtils;

/**
 * BlameResultContainer already has some basic functionality of calculating 
 * the age of each line in a file at a commit. This class makes it more 
 * straightforward and it should be faster, because everything is stored in 
 * an array. To initialize an object, use FileAge.calculate().<br>
 * Note: Everything is accurate to milliseconds at most.
 * @author lisq199
 *
 */
public class FileAge implements BlameResultContainer {
	
	/**
	 * Array storing the ages of the lines
	 */
	protected Duration[] ages;
	protected Duration totalAge;
	protected String sha1;
	
	protected BlameResult blameResult;
	
	/**
	 * Disable the default constructor
	 */
	protected FileAge() {
		this.ages = new Duration[0];
		this.totalAge = Duration.ZERO;
	}
	
	protected FileAge(String sha1, BlameResult blameResult) {
		this();
		this.sha1 = sha1;
		this.blameResult = blameResult;
	}
	
	@Override
	public String getSha1() {
		return sha1;
	}
	
	@Override
	public BlameResult getBlameResult() {
		return blameResult;
	}
	
	/**
	 * Get the sum of the ages of every line. The sum is not going to be as 
	 * accurate, because it will be based on seconds instead of milliseconds 
	 * to avoid overflow.
	 * @return
	 */
	public Duration getTotalAge() {
		return totalAge;
	}
	
	/**
	 * Get a copy of the array representing the age of each line in Duration. 
	 * Modifying the returning array will not affect the original object.
	 * @return
	 */
	public Duration[] getAgesOfLines() {
		return ages.clone();
	}
	
	/**
	 * Get an array representing the age of each line in milliseconds.
	 * @return
	 */
	public long[] getAgesOfLinesInMillis() {
		int len = ages.length;
		long[] ages = new long[len];
		for (int i = 0; i < len; i++) {
			ages[i] = getAgeOfLine(i).toMillis();
		}
		return ages;
	}
	
	/**
	 * Get an array representing the age of each line in days.
	 * @return
	 */
	public long[] getAgesOfLinesInDays() {
		int len = ages.length;
		long[] ages = new long[len];
		for (int i = 0; i < len; i++) {
			ages[i] = getAgeOfLine(i).toDays();
		}
		return ages;
	}

	/**
	 * Access the array directly instead of calculating it again
	 */
	@Override
	public Duration getAgeOfLine(int i) {
		return ages[i];
	}
	
	/**
	 * Get the average (mean)
	 * @return
	 */
	public Duration getAverage() {
		return OlognDurations.average(ages);
	}
	
	/**
	 * Get the median
	 * @return
	 */
	public Duration getMedian() {
		return OlognDurations.median(ages, false);
	}
	
	/**
	 * Get the mode. Notice that there can be multiple modes.
	 * @return
	 */
	public List<Duration> getMode() {
		return OlognArrays.mode(getAgesOfLines());
	}
	
	/**
	 * Get the minimum (newest) in Duration
	 * @return
	 */
	public Duration getMin() {
		return OlognArrays.min(ages);
	}
	
	/**
	 * Get the maximum (oldest) in Durations
	 * @return
	 */
	public Duration getMax() {
		return OlognArrays.max(ages);
	}
	
	@Override
	public String toString() {
		Function<Duration, String> dToS =
				d -> d.getSeconds() + " seconds ≈ " + d.toDays() + " days";
		StringBuilder builder = new StringBuilder("LineAge[\n");
		builder.append("\tSHA-1[").append(getSha1()).append("],\n");
		builder.append("\tfilePath[").append(getFilePath()).append("],\n");
		for (int i = 0; i < ages.length; i++) {
			builder.append("\tlineNo[").append(i).append("]: ")
					.append(dToS.apply(getAgeOfLine(i)))
					.append(",\n");
		}
		builder.append("\ttotalAge: ").append(dToS.apply(getTotalAge()))
				.append(",\n");
		builder.append("\tmean: ").append(dToS.apply(getAverage()))
				.append(",\n");
		builder.append("\tmedian: ").append(dToS.apply(getMedian()))
				.append(",\n");
		builder.append("\tmode[");
		for (Duration d : getMode()) {
			builder.append(dToS.apply(d)).append(", ");
		}
		builder.append("]\n");
		builder.append("]");
		return builder.toString();
	}
	
	/**
	 * Used internally for calculating the ages and total age.
	 */
	protected void initAges() {
		int size = this.getBlameSize();
		this.ages = new Duration[size];
		for (int i = 0; i < size; i++) {
			this.ages[i] = BlameResultContainer.super.getAgeOfLine(i);
			totalAge = totalAge.plus(this.ages[i]);
		}
	}

	/**
	 * Calculate a FileAge object
	 * @param sha1
	 * @param blameResult
	 * @return
	 */
	private static FileAge calculate(String sha1, BlameResult blameResult) {
		FileAge lineAge = new FileAge(sha1, blameResult);
		lineAge.initAges();
		return lineAge;
	}

	/**
	 * Calculate a FileAge object
	 * @param container
	 * @return
	 */
	public static FileAge calculate(BlameResultContainer container) {
		return calculate(container.getSha1(), container.getBlameResult());
	}
	
	/**
	 * Calculate a FileAge object
	 * @param git
	 * @param commitId
	 * @param filePath
	 * @return
	 * @throws GitAPIException
	 */
	public static FileAge calculate(Git git, AnyObjectId commitId,
			String filePath) throws GitAPIException {
		BlameResult blameResult = BlameUtils.getBlameResult(git, commitId,
				filePath);
		return calculate(commitId.getName(), blameResult);
	}
	
	/**
	 * Calculate a FileAge object
	 * @param git
	 * @param repo
	 * @param revstr
	 * @param filePath
	 * @return
	 * @throws RevisionSyntaxException
	 * @throws AmbiguousObjectException
	 * @throws IncorrectObjectTypeException
	 * @throws GitAPIException
	 * @throws IOException
	 */
	public static FileAge calculate(Git git, Repository repo, String revstr,
			String filePath) throws RevisionSyntaxException,
	AmbiguousObjectException, IncorrectObjectTypeException, GitAPIException,
	IOException {
		BlameResult blameResult = BlameUtils.getBlameResult(git, repo, revstr,
				filePath);
		return calculate(repo.resolve(revstr).getName(), blameResult);
	}
	
	/**
	 * Calculate a FileAge object
	 * @param git
	 * @param repo
	 * @param commit
	 * @param filePath
	 * @return
	 * @throws RevisionSyntaxException
	 * @throws AmbiguousObjectException
	 * @throws IncorrectObjectTypeException
	 * @throws GitAPIException
	 * @throws IOException
	 */
	public static FileAge calculate(Git git, Repository repo, RevCommit commit,
			String filePath) throws RevisionSyntaxException,
	AmbiguousObjectException, IncorrectObjectTypeException, GitAPIException,
	IOException {
		BlameResult blameResult = BlameUtils.getBlameResult(git, repo, commit,
				filePath);
		return calculate(commit.getName(), blameResult);
	}
	
	/**
	 * Calculate multiple FileAge objects for one file over multiple commits
	 * @param git
	 * @param repo
	 * @param commits
	 * @param filePath
	 * @return
	 * @throws RevisionSyntaxException
	 * @throws AmbiguousObjectException
	 * @throws IncorrectObjectTypeException
	 * @throws GitAPIException
	 * @throws IOException
	 */
	public static List<FileAge> calculateMultiple(Git git, Repository repo,
			Iterable<RevCommit> commits, String filePath)
					throws RevisionSyntaxException, AmbiguousObjectException,
					IncorrectObjectTypeException, GitAPIException,
					IOException {
		List<FileAge> list = new ArrayList<FileAge>();
		for (RevCommit commit : commits) {
			list.add(calculate(git, repo, commit, filePath));
		}
		return list;
	}
	
	/**
	 * Calculate multiple FileAge objects for one file over multiple commits
	 * @param containerss
	 * @return
	 */
	public static List<FileAge> calculateMultiple(
			Iterable<BlameResultContainer> containerss) {
		List<FileAge> list = new ArrayList<FileAge>();
		for (BlameResultContainer container : containerss) {
			list.add(calculate(container));
		}
		return list;
	}

}
