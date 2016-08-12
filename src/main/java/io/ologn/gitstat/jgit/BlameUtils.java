package io.ologn.gitstat.jgit;

import java.io.IOException;

import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * Utilities for Git blame
 * @author lisq199
 */
public class BlameUtils {
	
	/**
	 * Get a BlameResult for a file at a specified commit
	 * @param git
	 * @param filePath
	 * @param commitId
	 * @return
	 * @throws GitAPIException
	 */
	public static BlameResult getBlameResult(Git git, AnyObjectId commitId,
			String filePath) throws GitAPIException {
		BlameCommand blameCommand = git.blame().setFilePath(filePath)
				.setStartCommit(commitId);
		return blameCommand.call();
	}
	
	/**
	 * Get a BlameResult for a file at a specified commit
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
	public static BlameResult getBlameResult(Git git, Repository repo,
			String revstr, String filePath)
					throws RevisionSyntaxException, AmbiguousObjectException,
					IncorrectObjectTypeException, GitAPIException,
					IOException {
		return getBlameResult(git, repo.resolve(revstr), filePath);
	}
	
	/**
	 * Get a BlameResult for a file at a specified commit
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
	public static BlameResult getBlameResult(Git git, Repository repo,
			RevCommit commit, String filePath)
					throws RevisionSyntaxException, AmbiguousObjectException,
					IncorrectObjectTypeException, GitAPIException,
					IOException {
		return getBlameResult(git, repo, commit.getName(), filePath);
	}

}
