package io.ologn.gitstat.akka.msg;

import java.io.Serializable;

import io.ologn.gitstat.stat.BlameResultContainer;

/**
 * Immutable class for passing BlameResultContainer objects as messages 
 * with Akka.
 * @author lisq199
 */
public class BlameResultContainerMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final transient BlameResultContainer container;
	
	private BlameResultContainerMessage() {
		container = null;
	}
	
	private BlameResultContainerMessage(BlameResultContainer container) {
		this.container = container;
	}
	
	public BlameResultContainer getBlameResultContainer() {
		return container;
	}
	
	public static BlameResultContainerMessage init(
			BlameResultContainer container) {
		return new BlameResultContainerMessage(container);
	}

}
