package io.ologn.gitstat.stat;

import java.util.Date;
import java.util.TimeZone;

import org.eclipse.jgit.lib.PersonIdent;

import io.ologn.common.OlognHashCode;

/**
 * Object representing an author of the code
 * @author lisq199
 *
 */
public class GitAuthor {
	
	protected String name;
	protected String email;
	protected Date authorTime;
	protected TimeZone timeZone;
	
	/**
	 * Disable default constructor
	 */
	protected GitAuthor() {
		this.authorTime = null;
		this.timeZone = null;
	}
	
	public GitAuthor(String name, String email) {
		this();
		this.name = name.trim();
		this.email = email.trim();
	}
	
	public GitAuthor(PersonIdent pi) {
		this(pi.getName(), pi.getEmailAddress());
		this.authorTime = pi.getWhen();
		this.timeZone = pi.getTimeZone();
	}
	
	public String getName() {
		return this.name;
	}
	
	protected void setName(String name) {
		this.name = name;
	}
	
	public String getEmail() {
		return this.email;
	}
	
	protected void setEmail(String email) {
		this.email = email;
	}
	
	public Date getAuthorTime() {
		return this.authorTime;
	}
	
	public TimeZone getTimeZone() {
		return this.timeZone;
	}
	
	/**
	 * Implemented because GitAuthor will be used as a key in a Map
	 */
	@Override
	public int hashCode() {
		return OlognHashCode.init()
				.addObject(getName())
				.addObject(getEmail())
				.get();
	}

	/**
	 * Implemented because GitAuthor will be used as a key in a Map
	 */
	@Override
	public boolean equals(Object o) {
		return OlognHashCode.defaultEquals(this, o,
				(a, b) -> a.getName().compareTo(b.getName()) == 0
				&& a.getEmail().compareTo(b.getEmail()) == 0);
	}
	
	@Override
	public String toString() {
		return "GitAuthor[" + name + ", " + email + "]";
	}
	
	public String toStringBasic() {
		return name + ", " + email;
	}

}
