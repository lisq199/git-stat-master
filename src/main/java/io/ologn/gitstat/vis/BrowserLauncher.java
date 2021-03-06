package io.ologn.gitstat.vis;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;

/**
 * Used for launching the default browser to browse a URI.<br>
 * Typical usage: {@code BrowserLauncher.init().launchWithUri(uri);}
 * @author lisq199
 *
 */
public class BrowserLauncher {
	
	public static final String HTML_NAME = BrowserLauncher.class.getName()
			+ ".html";
	
	protected Desktop desktop;
	
	private BrowserLauncher() {
		if (!Desktop.isDesktopSupported()) {
			throw new UnsupportedOperationException(
					"Desktop API is not supported on the current platform");
		}
		desktop = Desktop.getDesktop();
	}
	
	public void launchWithUri(URI uri) throws IOException {
		desktop.browse(uri);
	}
	
	/**
	 * Dump the html text into an html file in the current working 
	 * directory and launch it 
	 * from there. The name of the html file will be the full class 
	 * name of this class. If the file already exists, it will be 
	 * overwritten.
	 * @param htmlText
	 * @throws IOException
	 */
	public void launchWithHtmlText(String htmlText) throws IOException {
		File html = new File(HTML_NAME);
		FileUtils.writeStringToFile(html, htmlText,
				Charset.defaultCharset(), false);
		launchWithUri(html.toURI());
	}
	
	public static BrowserLauncher init() {
		return new BrowserLauncher();
	}

}
