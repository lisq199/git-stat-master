package io.ologn.gitstat.akka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Inbox;
import io.ologn.common.collect.OlognMaps;
import io.ologn.gitstat.akka.msg.AuthorshipParamMessage;
import io.ologn.gitstat.akka.msg.FileAgeMessage;
import io.ologn.gitstat.jgit.RevCommitUtils;
import io.ologn.gitstat.stat.FileAge;

public class FileAgeMaster {
	
	private final ActorSystem actorSystem;
	private final Inbox inbox;
	
	private FileAgeMaster() {
		actorSystem = ActorSystem.create(this.getClass().getSimpleName());
		inbox = Inbox.create(actorSystem);
	}
	
	public List<FileAge> calculateFileAgeList(Git git,
			Repository repo, Iterable<RevCommit> commits, String filePath) {
		
		List<FileAge> list = Collections.synchronizedList(
				new ArrayList<FileAge>());
		
		Map<RevCommit, Integer> commitTasks = Collections.synchronizedMap(
				new HashMap<RevCommit, Integer>());
		
		StreamSupport.stream(commits.spliterator(), true)
				.forEach(commit -> commitTasks.put(commit, 0));
		
		while (!commitTasks.isEmpty()) {
			
			Set<RevCommit> unfinishedCommits = new HashSet<RevCommit>(
					commitTasks.keySet());
			
			unfinishedCommits.parallelStream().forEach(commit -> {
				OlognMaps.increment(commitTasks, commit, 1);
				if (commitTasks.get(commit) > AkkaUtils.MAX_RETRY) {
					commitTasks.remove(commit);
					return;
				}

				ActorRef actor = AkkaUtils.spawnActor(actorSystem,
						FileAgeActor.class, null);

				AuthorshipParamMessage msg = AuthorshipParamMessage
						.init(git, repo, commit, filePath);
				
				synchronized(inbox) {
					inbox.send(actor, msg);
				}
			});
			
			IntStream.range(0, commitTasks.size()).parallel().forEach(i -> {
				Object rcv = null;
				try {
					rcv = inbox.receive(AkkaUtils.TIMEOUT);
				} catch (TimeoutException e) {
					e.printStackTrace();
					return; 
				}
				
				FileAgeMessage fam = (FileAgeMessage) rcv;
				FileAge fa = fam.getFileAge();
				
				if (fa == null) {
					return;
				}
				
				RevCommit commit = null;
				try {
					commit = RevCommitUtils.fromSha1(repo, fa.getSha1());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				if (commit == null) {
					return;
				}
				
				if (!commitTasks.containsKey(commit)) {
					System.out.println("Result for " + commit + " discarded "
							+ "because it has already been received");
					return;
				}
				
				list.add(fa);
				
				commitTasks.remove(commit);
			});
			
		}
		
		return list;
	}
	
	public static FileAgeMaster init() {
		return new FileAgeMaster();
	}
	
}
