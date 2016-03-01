package io.ologn.gitstat.stat;

import java.time.Duration;

import org.eclipse.jgit.blame.BlameResult;

/**
 * Classes that contain a BlameResult object. This abstract class contains 
 * methods that take advantage of the BlameResult object.
 * @author lisq199
 *
 */
public interface BlameResultContainer {
	
	/**
	 * Get the BlameResult object
	 * @return
	 */
	public BlameResult getBlameResult();
	
	public String getSha1();
	
	/**
	 * Get the path of the file the current BlameResult points to
	 * @return
	 */
	public default String getFilePath() {
		return getBlameResult().getResultPath();
	}
	
	/**
	 * Get the number of lines of the file
	 * @return
	 */
	public default int getBlameSize() {
		return getBlameResult().getResultContents().size();
	}
	
	/**
	 * Get the GitAuthor representing the author at a line
	 * @param i the line number
	 * @return
	 */
	public default GitAuthor getAuthorAtLine(int i) {
		return new GitAuthor(getBlameResult().getSourceAuthor(i));
	}
	
	/**
	 * Get the GitAuthor representing the commiter at a line. Note: the 
	 * author time in this case may not be meaningful.
	 * @param i
	 * @return
	 */
	public default GitAuthor getCommiterAtLine(int i) {
		return new GitAuthor(getBlameResult().getSourceCommitter(i));
	}
	
	/**
	 * Get the content of the entire file stored in a String
	 * @return
	 */
	public default String getSource() {
		return getBlameResult().getResultContents().toString();
	}
	
	/**
	 * Get the source code at a line
	 * @param i
	 * @return
	 */
	public default String getSourceLine(int i) {
		return getBlameResult().getResultContents().getString(i);
	}
	
	/**
	 * Get the age of a line in a Duration object.<br>
	 * Note: the result is accurate to milliseconds.
	 * @param i
	 * @return
	 */
	public default Duration getAgeOfLine(int i) {
		long authorMillis = getAuthorAtLine(i).getAuthorTime()
				.getTime();
		return Duration.ofMillis(System.currentTimeMillis() - authorMillis);
	}

}
