package io.ologn.gitstat.jgit;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import com.google.common.collect.Lists;

import io.ologn.common.math.OlognMath;

/**
 * Utilities for RevCommit objects
 * @author lisq199
 *
 */
public class RevCommitUtils {
	
	public static RevCommit fromSha1(Repository repo, String sha1)
			throws MissingObjectException, IncorrectObjectTypeException,
			IOException {
		ObjectId id = ObjectId.fromString(sha1);
		RevWalk revWalk = new RevWalk(repo);
		RevCommit commit = revWalk.parseCommit(id);
		revWalk.dispose();
		revWalk.close();
		return commit;
	}
	
	/**
	 * Get all the RevCommit objects from a Git object.
	 * @param git
	 * @return a List of RevCommit objects
	 * @throws NoHeadException
	 * @throws GitAPIException
	 * @throws IOException
	 */
	public static List<RevCommit> getAllCommits(Git git)
			throws NoHeadException, GitAPIException, IOException {
		Iterable<RevCommit> commits = git.log().all().call();
		// Return it as a List to make it reusable
		List<RevCommit> commitsList = Lists.newArrayList(commits);
		System.out.println("Number of commits: " + commitsList.size());
		return commitsList;
	}
	
	/**
	 * Get a List of RevCommit objects that contains certain paths
	 * @param git
	 * @param paths
	 * @return a List of RevCommit objects
	 * @throws NoHeadException
	 * @throws GitAPIException
	 */
	public static List<RevCommit> getCommitsWithPath(Git git,
			String... paths) throws NoHeadException, GitAPIException {
		LogCommand logCommand = git.log();
		for (String path : paths) {
			logCommand.addPath(path);
		}
		Iterable<RevCommit> commits = logCommand.call();
		// Return it as a List to make it reusable
		List<RevCommit> commitsList = Lists.newArrayList(commits);
		System.out.println("Number of commits: " + commitsList.size());
		return commitsList;
	}
	
	/**
	 * Filter a list (Iterable) of RevCommit objects by a Predicate.
	 * @param commits
	 * @param predicate
	 * @return the filtered List of RevCommit objects
	 */
	public static List<RevCommit> filter(
			Iterable<RevCommit> commits, Predicate<RevCommit> predicate) {
		return StreamSupport.stream(commits.spliterator(), false)
				.filter(predicate)
				.collect(Collectors.toList());
	}
	
	/**
	 * Filter a list (Iterable) of RevCommit objects between 2 time points. 
	 * The 2 time points do not have to be in order.
	 * @param commits
	 * @param time1 the first time in UNIX time
	 * @param time2 the second time in UNIX time
	 * @return the filtered List of RevCommit objects
	 */
	public static List<RevCommit> filterBetweenTime(
			Iterable<RevCommit> commits, long time1, long time2) {
		return filter(commits, c -> OlognMath.isBetween(
				c.getCommitTime(), time1, time2, true));
	}
	
	/**
	 * Filter a list (Iterable) of RevCommit objects before a certain time.
	 * @param commits
	 * @param time UNIX timestamp
	 * @return the filtered List of RevComit objects
	 */
	public static List<RevCommit> filterBeforeTime(
			Iterable<RevCommit> commits, long time) {
		return filter(commits, c -> c.getCommitTime() <= time);
	}
	
	/**
	 * Filter a list (Iterable) of RevCommit objects before a certain time.
	 * @param commits
	 * @param time UNIX timestamp
	 * @return the filtered List of RevComit objects
	 */
	public static List<RevCommit> filterAfterTime(
			Iterable<RevCommit> commits, long time) {
		return filter(commits, c -> c.getCommitTime() >= time);
	}

}
