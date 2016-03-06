package io.ologn.gitstat.vis;

import java.io.File;
import java.io.StringWriter;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * Interface for classes that generate HTML strings with Apache Velocity.
 * @author lisq199
 *
 */
public interface VelocityHtmlGenerator {
	
	public static final String TEMPLATE_DIR = "res" + File.separator
			+ "velocity_templates" + File.separator;
	
	/**
	 * Get the path of the template relative to the current working 
	 * directory
	 * @return
	 */
	public String getTemplatePath();
	
	/**
	 * @return a Map where the keys are all the strings that needs to be 
	 * replaced, and the values are the corresponding replacement strings.
	 */
	public Map<String, String> getReplaceMap();
	
	/**
	 * Create a String storing the resulting HTML text.
	 * @return
	 */
	public default String createHtmlString() {
		return replace(getTemplatePath(), getReplaceMap());
	}
	
	/**
	 * Intended for internal use, but it's probably reusable.
	 * @param templatePath
	 * @param replaceMap
	 * @return
	 */
	public static String replace(String templatePath,
			Map<String, String> replaceMap) {
		VelocityEngine ve = new VelocityEngine();
		ve.init();
		Template t = ve.getTemplate(templatePath);
		VelocityContext context = new VelocityContext();
		replaceMap.forEach((s1, s2) -> context.put(s1, s2));
		StringWriter writer = new StringWriter();
		t.merge(context, writer);
		return writer.toString();
	}
	
}
