package io.ologn.gitstat.akka;

import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import akka.actor.UntypedActor;
import io.ologn.gitstat.akka.msg.AuthorshipParamMessage;
import io.ologn.gitstat.akka.msg.BlameResultContainerMessage;
import io.ologn.gitstat.akka.msg.FileAgeMessage;
import io.ologn.gitstat.stat.BlameResultContainer;
import io.ologn.gitstat.stat.FileAge;

public class FileAgeActor extends UntypedActor {

	@Override
	public void onReceive(Object m) {
		FileAge fa = null;
		String sha1 = null;
		
		if (m instanceof AuthorshipParamMessage) {
			AuthorshipParamMessage msg = (AuthorshipParamMessage) m;
			Git git = msg.getGit();
			Repository repo = msg.getRepository();
			RevCommit commit = msg.getCommit();
			String filePath = msg.getFilePath();
			
			System.out.println("Received AuthorshipParamMessage with "
					+ commit);
			sha1 = commit.getName();
			
			fa = calculate(git, repo, commit, filePath);
		} else if (m instanceof BlameResultContainerMessage) {
			BlameResultContainerMessage msg = (BlameResultContainerMessage) m;
			
			System.out.println("Reveived BlameResultContainerMessage"
					+ msg.getBlameResultContainer());
			sha1 = msg.getBlameResultContainer().getSha1();
			
			fa = calculate(msg.getBlameResultContainer());
		} else {
			unhandled(m);
		}
		
		FileAgeMessage lam = FileAgeMessage.init(fa);
		getSender().tell(lam, getSelf());
		System.out.println("Sent reply for " + sha1);
	}
	
	private static FileAge calculate(Git git, Repository repo,
			RevCommit commit, String filePath) {
		FileAge fa = null;
		try {
			fa = FileAge.calculate(git, repo, commit, filePath);
		} catch (RevisionSyntaxException e) {
			e.printStackTrace();
		} catch (AmbiguousObjectException e) {
			e.printStackTrace();
		} catch (IncorrectObjectTypeException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fa;
	}
	
	private static FileAge calculate(BlameResultContainer container) {
		return FileAge.calculate(container);
	}

}
