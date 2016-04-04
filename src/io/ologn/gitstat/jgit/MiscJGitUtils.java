package io.ologn.gitstat.jgit;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

/**
 * Other miscellaneous JUnit utilities
 * @author lisq199
 */
public class MiscJGitUtils {
	
	/**
	 * Get a Git object by the .git file.
	 * @param gitDir a File object pointing to the .git file.
	 * @return the Git object
	 */
	public static Git getGit(File gitDir) {
		Git git = null;
		try {
			git = Git.open(gitDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return git;
	}
	
	/**
	 * Convert a RawText object to String. (The RawText class has no toString)
	 * @param rawText
	 * @return
	 */
	public static String rawTextToString(RawText rawText) {
		StringBuilder stringBuilder = new StringBuilder();
		int size = rawText.size();
		for (int i = 0; i < size; i++) {
			stringBuilder.append(rawText.getString(i));
		}
		return stringBuilder.toString();
	}
	
	/**
	 * Get the commit time of a commit as a Unix time stamp from an SHA-1 code
	 * @param repo
	 * @param sha1
	 * @return
	 * @throws MissingObjectException
	 * @throws IncorrectObjectTypeException
	 * @throws IOException
	 */
	public static long getCommitTimeFromSha1(Repository repo, String sha1)
			throws MissingObjectException, IncorrectObjectTypeException,
			IOException {
		ObjectId id = ObjectId.fromString(sha1);
		RevWalk revWalk = new RevWalk(repo);
		RevCommit commit = revWalk.parseCommit(id);
		revWalk.dispose();
		revWalk.close();
		return commit.getCommitTime();
	}
	
	/**
	 * Get the author time of a commit as a Date from an SHA-1 code
	 * @param repo
	 * @param sha1
	 * @return
	 * @throws MissingObjectException
	 * @throws IncorrectObjectTypeException
	 * @throws IOException
	 */
	public static Date getAuthorTimeFromSha1(Repository repo, String sha1)
			throws MissingObjectException, IncorrectObjectTypeException,
			IOException {
		ObjectId id = ObjectId.fromString(sha1);
		RevWalk revWalk = new RevWalk(repo);
		RevCommit commit = revWalk.parseCommit(id);
		revWalk.dispose();
		revWalk.close();
		return commit.getAuthorIdent().getWhen();
	}
	
	/**
	 * Get the Comparator of SHA-1 strings by author time
	 * @param repo
	 * @param ascending
	 * @return
	 */
	public static Comparator<String> getSha1ComparatorByAuthorTime(
			Repository repo, boolean ascending) {
		Function<String, Long> getT = x -> {
			try {
				return RevCommitUtils.fromSha1(repo, x).getAuthorIdent()
						.getWhen().getTime();
			} catch (IOException e) {
				/*
				 * This should never happen if the SHA-1 strings are 
				 * obtained from the repository. 
				 */
				e.printStackTrace();
			}
			return 0l;
		};
		return (x, y) -> {
			Long t1 = getT.apply(x);
			Long t2 = getT.apply(y);
			if (ascending) {
				return t1.compareTo(t2);
			} else {
				return t2.compareTo(t1);
			}
		};
	}
	
	/**
	 * Get a Map that sorts the keys (SHA-1) by author time.
	 * @param repo
	 * @param ascending
	 * @return
	 */
	public static <T> Map<String, T> getMapSortedByAuthorTime(
			Repository repo, boolean ascending) {
		return new TreeMap<String, T>(
				getSha1ComparatorByAuthorTime(repo, ascending));
	}
	
	public static String[] getTags(Git git) throws GitAPIException {
		List<Ref> tagList = git.tagList().call();
		return tagList.stream()
				.map(t -> t.getName())
				.toArray(String[]::new);
	}

}
