package io.ologn.gitstat.akka.msg;

import java.io.Serializable;

import io.ologn.gitstat.stat.LineAuthorship;

/**
 * Immutable class for passing LineAuthorship objects as messages 
 * with Akka.
 * @author lisq199
 */
public class LineAuthorshipMessage implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final transient LineAuthorship la;
	
	private LineAuthorshipMessage() {
		la = null;
	}
	
	private LineAuthorshipMessage(LineAuthorship la) {
		this.la = la;
	}
	
	public LineAuthorship getLineAuthorship() {
		return la;
	}
	
	public static LineAuthorshipMessage init(LineAuthorship la) {
		return new LineAuthorshipMessage(la);
	}

}
