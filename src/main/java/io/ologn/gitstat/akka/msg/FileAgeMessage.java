package io.ologn.gitstat.akka.msg;

import java.io.Serializable;

import io.ologn.gitstat.stat.FileAge;

/**
 * Immutable class for passing FileAge objects as messages 
 * with Akka.
 * @author lisq199
 */
public class FileAgeMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final transient FileAge fa;
	
	private FileAgeMessage() {
		fa = null;
	}
	
	private FileAgeMessage(FileAge fa) {
		this.fa = fa;
	}
	
	public FileAge getFileAge() {
		return fa;
	}
	
	public static FileAgeMessage init(FileAge fa) {
		return new FileAgeMessage(fa);
	}

}
