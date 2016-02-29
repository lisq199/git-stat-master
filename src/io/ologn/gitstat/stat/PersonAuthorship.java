package io.ologn.gitstat.stat;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;

import io.ologn.gitstat.jgit.MiscJGitUtils;

/**
 * Data for a person's authorship of a file over time. Objects of this class 
 * should not be modified once they are created. PersonAuthorship.calculate() 
 * should be used instead of the default constructor.
 * @author lisq199
 *
 */
public class PersonAuthorship {
	
	protected GitAuthor author;
	/**
	 * The key is the commit's SHA-1, and the value is the percentage of how 
	 * much the author owns the file.
	 */
	protected Map<String, Double> map;
	
	/**
	 * Disable default constructor
	 */
	protected PersonAuthorship() {}
	
	protected PersonAuthorship(GitAuthor author, Repository repo,
			boolean ascending) {
		this();
		this.author = author;
		this.map = MiscJGitUtils.getMapSortedByAuthorTime(repo, true);
	}
	
	/**
	 * Get the GitAuthor of the current PersonAuthorship
	 * @return
	 */
	public GitAuthor getAuthor() {
		return author;
	}
	
	public Map<String, Double> getMap() {
		// Use LinkedHashMap to preserve the ordering
		return new LinkedHashMap<String, Double>(map);
	}
	
	/**
	 * Whether the current object contains a commit represented by an 
	 * SHA-1 string
	 * @param sha1
	 * @return
	 */
	public boolean containsSha1(String sha1) {
		return map.containsKey(sha1);
	}
	
	/**
	 * Get all the SHA-1 strings
	 * @return
	 */
	public Set<String> getSha1s() {
		return map.keySet();
	}
	
	public void forEach(BiConsumer<String, Double> action) {
		getMap().forEach(action);
	}
	
	public double getPercentage(String sha1) {
		return map.get(sha1);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("PersonAuthorship[\n");
		builder.append("\t").append(author).append(",\n");
		forEach((sha1, percentage) -> builder.append("\t").append(sha1)
				.append(": ").append(percentage).append("%,\n"));
		builder.append("]");
		return builder.toString();
	}
	
	/**
	 * Calculate a person's authorship of a file over multiple commits, sorted 
	 * by either ascending or descending order.
	 * @param repo
	 * @param author
	 * @param fAuthorships a list (Iterable) of FileAuthorship objects
	 * @param ascending true for ascending order, and false for descending
	 * @return
	 * @throws MissingObjectException
	 * @throws IncorrectObjectTypeException
	 * @throws IOException
	 */
	public static PersonAuthorship calculate(Repository repo,
			GitAuthor author, Iterable<FileAuthorship> fAuthorships,
			boolean ascending) throws MissingObjectException,
					IncorrectObjectTypeException, IOException {
		PersonAuthorship pAuthorship = new PersonAuthorship(
				author, repo, ascending);
		for (FileAuthorship fAuthorship : fAuthorships) {
			String sha1 = fAuthorship.getSha1();
			double percentage = fAuthorship.getPercentage(author);
			pAuthorship.map.put(sha1, percentage);
		}
		return pAuthorship;
	}
	
	/**
	 * Calculate a person's authorship of a file over multiple commits, sorted 
	 * by either ascending or descending order. 
	 * @param repo
	 * @param author
	 * @param fAuthorships
	 * @return
	 * @throws MissingObjectException
	 * @throws IncorrectObjectTypeException
	 * @throws IOException
	 */
	public static PersonAuthorship calculate(Repository repo,
			GitAuthor author, Iterable<FileAuthorship> fAuthorships)
					throws MissingObjectException,
					IncorrectObjectTypeException, IOException {
		return calculate(repo, author, fAuthorships, true);
	}
	
}
