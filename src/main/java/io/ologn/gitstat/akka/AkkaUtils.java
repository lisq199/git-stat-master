package io.ologn.gitstat.akka;

import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

/**
 * Utilities for Akka
 * @author lisq199
 */
public class AkkaUtils {
	
	/**
	 * The maximum times the master will retry
	 */
	public static final int MAX_RETRY = 10;
	
	/**
	 * The default timeout for the master
	 */
	public static final FiniteDuration TIMEOUT =
			Duration.create(30, TimeUnit.SECONDS);
	
	/**
	 * Spawn a new actor
	 * @param system
	 * @param actorClass The class of the actor to be spawned
	 * @param name The name of the new actor. If it's set to null or 
	 * empty string, the actor won't have a name
	 * @return The spawned actor as ActorRef
	 */
	public static ActorRef spawnActor(
			ActorSystem system, Class<?> actorClass, String name) {
		if (name == null || name.isEmpty()) {
			return system.actorOf(Props.create(actorClass));
		} else {
			return system.actorOf(Props.create(actorClass), name);
		}
	}

}
