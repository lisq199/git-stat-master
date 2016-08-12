package io.ologn.gitstat.akka.msg;

import java.io.Serializable;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * Immutable class for passing parameters for calculating LineAuthorship 
 * or FileAge as messages with Akka.
 * @author lisq199
 */
public class AuthorshipParamMessage implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final transient Git git;
	private final transient Repository repo;
	private final transient RevCommit commit;
	private final String filePath;
	
	private AuthorshipParamMessage() {
		git = null;
		repo = null;
		commit = null;
		filePath = null;
	}
	
	private AuthorshipParamMessage(Git git, Repository repo, RevCommit commit,
			String filePath) {
		this.git = git;
		this.repo = repo;
		this.commit = commit;
		this.filePath = filePath;
	}
	
	public Git getGit() {
		return git;
	}
	
	public Repository getRepository() {
		return repo;
	}
	
	public RevCommit getCommit() {
		return commit;
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public static AuthorshipParamMessage init(Git git, Repository repo, 
			RevCommit commit, String filePath) {
		return new AuthorshipParamMessage(git, repo, commit, filePath);
	}
	
}
