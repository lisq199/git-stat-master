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
import io.ologn.gitstat.akka.msg.LineAuthorshipMessage;
import io.ologn.gitstat.jgit.RevCommitUtils;
import io.ologn.gitstat.stat.LineAuthorship;

public class LineAuthorshipMaster {
	
	private final ActorSystem actorSystem;
	private final Inbox inbox;
	
	private LineAuthorshipMaster() {
		actorSystem = ActorSystem.create(this.getClass().getSimpleName());
		inbox = Inbox.create(actorSystem);
	}
	
	public List<LineAuthorship> calculateLineAuthorshipList(Git git,
			Repository repo, Iterable<RevCommit> commits, String filePath) {
		
		/**
		 * List storing the results to be returned
		 */
		List<LineAuthorship> list = Collections.synchronizedList(
				new ArrayList<LineAuthorship>());
		
		/**
		 * Map for keeping track of commits that hasn't been processed. 
		 * The keys are commits themselves, and the values are the 
		 * number of retry attemps.
		 */
		Map<RevCommit, Integer> commitTasks = Collections.synchronizedMap(
				new HashMap<RevCommit, Integer>());
		
		// Initialize the map.
		StreamSupport.stream(commits.spliterator(), true)
				.forEach(commit -> commitTasks.put(commit, 0));
		
		while (!commitTasks.isEmpty()) {
			
			// Copy of the key set.
			Set<RevCommit> unfinishedCommits = new HashSet<RevCommit>(
					commitTasks.keySet());
			
			// For each unfinished commit
			unfinishedCommits.parallelStream().forEach(commit -> {
				// Increase the retry count
				OlognMaps.increment(commitTasks, commit, 1);
				/*
				 * If the retry count exceeds MAX_RETRY, then there's 
				 * probably something wrong with the commit itself.
				 */
				if (commitTasks.get(commit) > AkkaUtils.MAX_RETRY) {
					commitTasks.remove(commit);
					return;
				}
				
				// Spawn an actor to work on the commit
				ActorRef actor = AkkaUtils.spawnActor(actorSystem,
						LineAuthorshipActor.class, null);

				AuthorshipParamMessage msg = AuthorshipParamMessage
						.init(git, repo, commit, filePath);
				
				synchronized(inbox) {
					inbox.send(actor, msg);
				}
			});
			
			// The size of commitTasks is the number of commits being worked on
			IntStream.range(0, commitTasks.size()).parallel().forEach(i -> {
				// Wait for messages
				Object rcv = null;
				try {
					rcv = inbox.receive(AkkaUtils.TIMEOUT);
				} catch (TimeoutException e) {
					// Timeout
					e.printStackTrace();
					return; 
				}
				
				LineAuthorshipMessage lam = (LineAuthorshipMessage) rcv;
				LineAuthorship la = lam.getLineAuthorship();
				
				if (la == null) {
					return;
				}
				
				RevCommit commit = null;
				try {
					commit = RevCommitUtils.fromSha1(repo, la.getSha1());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				if (commit == null) {
					return;
				}
				
				if (!commitTasks.containsKey(commit)) {
					/*
					 * If the commit is not present in commitTasks, then 
					 * it means the result has already been received and 
					 * therefore it's been removed.
					 */
					System.out.println("Result for " + commit + " discarded "
							+ "because it has already been received");
					return;
				}
				
				list.add(la);
				
				commitTasks.remove(commit);
			});
			
		}
		
		return list;
	}
	
	public static LineAuthorshipMaster init() {
		return new LineAuthorshipMaster();
	}

}
