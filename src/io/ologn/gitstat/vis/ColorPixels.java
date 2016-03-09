package io.ologn.gitstat.vis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.ologn.common.color.ColorCategory;
import io.ologn.common.math.LinearScale;

/**
 * For creating visualization with color pixels, where each block 
 * (or color pixel) represents some data, and each column represents 
 * a data set. Ultimately it will generate an HTML string.<br>
 * Note: parseMap() must be called after all the attributes are set.<br>
 * Typical usage: {@code ColorPixels.init().setPixelHeight(2)
 * .setPixelWidth(3).parse(dataArrays, titleArrays, true).createHtmlString()}
 * @author lisq199
 */
public class ColorPixels implements VelocityHtmlGenerator {
	
	public static final String TEMPLATE_PATH =
			VelocityHtmlGenerator.TEMPLATE_DIR + "ColorPixels.html";
	
	public static final int PIXEL_WITDH = 5;
	public static final int PIXEL_HEIGHT = 5;
	
	public static final String REPLACE_SVG = "svgTags",
			REPLACE_TOTAL_WIDTH = "totalWidth",
			REPLACE_TOTAL_HEIGHT = "totalHeight";
	
	protected Map<String, String> replaceMap;
	protected int pixelWidth;
	protected int pixelHeight;
	protected ColorCategory colorCategory;
	
	protected ColorPixels() {
		replaceMap = new HashMap<String, String>();
		pixelWidth = PIXEL_WITDH;
		pixelHeight = PIXEL_HEIGHT;
		colorCategory = ColorCategory.D3_CATEGORY10;
	}
	
	/**
	 * Get the pixel width
	 * @return
	 */
	public int getPixelWidth() {
		return pixelWidth;
	}
	
	/**
	 * Set the width of each pixel (block)
	 * @param pixelWidth
	 * @return
	 */
	public ColorPixels setPixelWidth(int pixelWidth) {
		this.pixelWidth = pixelWidth;
		return this;
	}
	
	/**
	 * Get the pixel height
	 * @return
	 */
	public int getPixelHeight() {
		return pixelHeight;
	}
	
	/**
	 * Set the height of each pixel (block)
	 * @param pixelHeight
	 * @return
	 */
	public ColorPixels setPixelHeight(int pixelHeight) {
		this.pixelHeight = pixelHeight;
		return this;
	}
	
	/**
	 * Get the ColorCategory
	 * @return
	 */
	public ColorCategory getColorCategory() {
		return colorCategory;
	}
	
	/**
	 * Set the ColorCategory for the color pixels
	 * @param colorCategory
	 * @return
	 */
	public ColorPixels setColorCategory(ColorCategory colorCategory) {
		this.colorCategory = colorCategory;
		return this;
	}

	@Override
	public String getTemplatePath() {
		return TEMPLATE_PATH;
	}

	@Override
	public Map<String, String> getReplaceMap() {
		return new HashMap<String, String>(replaceMap);
	}
	
	/**
	 * Parse the data
	 * @param map
	 * @param vertical
	 * @return
	 */
	public ColorPixels parse(List<long[]> dataArrays,
			List<String[]> titleArrays, boolean vertical) {
		String rectTags = getRectTagsFromDataAndTitle(dataArrays, titleArrays,
				pixelWidth, pixelHeight, colorCategory, vertical);
		this.replaceMap.put(REPLACE_SVG, rectTags);
		// calculate total width
		int totalWidth = pixelWidth * dataArrays.size();
		// calculate total Height
		int maxLen = dataArrays.stream()
				.map(a -> a.length)
				.max(Integer::compare)
				.get();
		int totalHeight = pixelHeight * maxLen;
		// Set the total width and height of the svg tag
		this.replaceMap.put(REPLACE_TOTAL_WIDTH,
				"" + (vertical ? totalWidth : totalHeight));
		this.replaceMap.put(REPLACE_TOTAL_HEIGHT,
				"" + (vertical ? totalHeight : totalWidth));
		return this;
	}

	/**
	 * Initialize
	 * @return
	 */
	public static ColorPixels init() {
		return new ColorPixels();
	}
	
	/**
	 * Get a svg rect tag
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param fill
	 * @param title if the rect tag is not supposed to have a 
	 * title, assign this parameter to an empty String or null.
	 * @return
	 */
	protected static String getRectTag(int x, int y, int width, int height,
			String fill, String title) {
		String result = "<rect x=" + x + " y=" + y + " width=" + width
				+ " height=" + height + " fill='" + fill + "'";
		if (title == null || title.isEmpty()) {
			result += " />";
		} else {
			result += "><title>" + title + "</title></rect>";
		}
		return result;
	}
	
	/**
	 * Get an integer attribute from an HTML tag
	 * @param tag
	 * @param attr
	 * @return
	 */
	protected static int getIntAttr(String tag, String attr) {
		int attrIndex = tag.toLowerCase().indexOf(attr);
		int equalsIndex = tag.indexOf('=', attrIndex);
		int spaceIndex = tag.indexOf(" ", equalsIndex);
		return Integer.parseInt(tag.substring(equalsIndex + 1, spaceIndex));
	}
	
	/**
	 * Get a String attribute from an HTML tag
	 * @param tag
	 * @param attr
	 * @return
	 */
	protected static String getStringAttr(String tag, String attr) {
		int attrIndex = tag.toLowerCase().indexOf(attr);
		if (attrIndex < 0) {
			return "";
		}
		int equalsIndex = tag.indexOf('=', attrIndex);
		int leftQuoteIndex = tag.indexOf("'", equalsIndex);
		int rightQuoteIndex = tag.indexOf("'", leftQuoteIndex + 1);
		return tag.substring(leftQuoteIndex + 1, rightQuoteIndex);
	}
	
	/**
	 * Get a tag within an HTML tag
	 * @param tag
	 * @param innerTag
	 * @return
	 */
	protected static String getInnerTag(String tag, String innerTag) {
		int leftTagIndex = tag.toLowerCase().indexOf("<" + innerTag + ">");
		if (leftTagIndex < 0) {
			return "";
		}
		int rightTagIndex = tag.toLowerCase().indexOf("</" + innerTag + ">",
				leftTagIndex + 1);
		int endIndex = tag.indexOf('>', rightTagIndex);
		return tag.substring(leftTagIndex, endIndex + 1);
	}
	
	protected static String getRectTagsFromDataAndTitle(
			List<long[]> dataArrays, List<String[]> titleArrays,
			int pixelWidth, int pixelHeight,
			ColorCategory colorCategory, boolean vertical) {
		final String tt = "\t\t";
		
		long min = dataArrays.stream()
				.map(a -> Arrays.stream(a).min().getAsLong())
				.min(Long::compare)
				.get();
		long max = dataArrays.stream()
				.map(a -> Arrays.stream(a).max().getAsLong())
				.max(Long::compare)
				.get();
		
		LinearScale colorScale = colorCategory.getLinearScale(min, max);
		
		StringBuilder builder = new StringBuilder();
		
		int xOffset = 0;
		for (int i = 0; i < dataArrays.size(); i++) {
			long[] dataArray = dataArrays.get(i);
			String[] titleArray = new String[0];
			if (i < titleArrays.size()) {
				titleArray = titleArrays.get(i);
			}
			
			StringBuilder columnBuilder = new StringBuilder();
			int yOffset = 0;
			for (int j = 0; j < dataArray.length; j++) {
				String color = colorCategory.getColor(
						dataArray[j], colorScale);
				String title = "";
				if (j < titleArray.length) {
					title = titleArray[j];
				}
				String rectTag;
				if (vertical) {
					rectTag = getRectTag(xOffset, yOffset,
							pixelWidth, pixelHeight,
							color, title);
				} else {
					rectTag = getRectTag(yOffset, xOffset,
							pixelHeight, pixelWidth,
							color, title);
				}
				
				columnBuilder.append(tt).append(rectTag).append("\n");
				
				yOffset += pixelHeight;
			}
			builder.append(optimizeRectTags(columnBuilder.toString(),
					vertical)).append("\n");
			
			xOffset += pixelWidth;
		}
		
		return builder.toString();
	}
	
	/**
	 * Optimize the rect tags, so that multiple color pixels 
	 * of the same color are combined into one.
	 * @param rectTags
	 * @param vertical
	 * @return
	 */
	protected static String optimizeRectTags(String rectTags,
			boolean vertical) {
		String[] tags = rectTags.split("\n");
		for (int i = 0; i < tags.length - 1; i++) {
			if (tags[i].isEmpty()) {
				continue;
			}
			String currentFill = getStringAttr(tags[i], "fill");
			String nextFill = getStringAttr(tags[i + 1], "fill");
			String positionAttr = vertical ? "x" : "y";
			int currentPos = getIntAttr(tags[i], positionAttr);
			int nextPos = getIntAttr(tags[i + 1], positionAttr);
			String currentTitle = getInnerTag(tags[i], "title");
			String nextTitle = getInnerTag(tags[i + 1], "title");
			
			if (currentFill.equalsIgnoreCase(nextFill)
					&& currentPos == nextPos
					&& currentTitle.equalsIgnoreCase(nextTitle)) {
				String tagName = vertical ? "height" : "width";
				int currentVal = getIntAttr(tags[i], tagName);
				int nextVal = getIntAttr(tags[i + 1], tagName);
				int newVal = currentVal + nextVal;
				tags[i + 1] = tags[i].replace(tagName + '=' + currentVal,
						tagName + '=' + newVal);
				tags[i] = "";
			}
		}
		return Arrays.stream(tags)
				.filter(s -> !s.isEmpty())
				.collect(Collectors.joining("\n"));
	}
	
}
