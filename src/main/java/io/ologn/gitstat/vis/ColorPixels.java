package io.ologn.gitstat.vis;

import java.util.ArrayList;
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
 * .setPixelWidth(3).parse(dataArrays, titleMap, true).createHtmlString()}
 * @author lisq199
 */
public class ColorPixels implements VelocityHtmlGenerator {
	
	public static final String TEMPLATE_PATH =
			VelocityHtmlGenerator.TEMPLATE_DIR + "ColorPixels.html";
	
	public static final String HTML_LF = "&#10;";
	
	public static final int PIXEL_WITDH = 5;
	public static final int PIXEL_HEIGHT = 5;
	
	public static final int LEGEND_SIZE = 16,
			LEGEND_SPACE = 5,
			BOOKMARK_SIZE = 16;
	
	public static final String REPLACE_SVG = "svgTags",
			REPLACE_TOTAL_WIDTH = "totalWidth",
			REPLACE_TOTAL_HEIGHT = "totalHeight",
			REPLACE_LEGEND = "legendTags",
			REPLACE_LEGEND_HEIGHT = "legendHeight";
	
	protected Map<String, String> replaceMap;
	protected int pixelWidth;
	protected int pixelHeight;
	protected ColorCategory colorCategory;
	protected boolean displayLegend;
	protected boolean vertical;
	protected boolean scaleColors;
	
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
	 * @param dataArrays All the data to be displayed. Each 
	 * array represents a column of data 
	 * @param titleMap A map that maps the data to its title/label
	 * @param datasetDescription A list of Strings where each String 
	 * is a description of the corresponding dataset
	 * @param bookmarkMap
	 * @param vertical
	 * @param scaleColors
	 * @return
	 */
	public ColorPixels parse(List<long[]> dataArrays,
			Map<Long, String> titleMap, List<String> datasetDescriptions,
			Map<Integer, String> bookmarkMap, boolean displayLegend,
			boolean vertical, boolean scaleColors) {
		this.displayLegend = displayLegend;
		this.vertical = vertical;
		this.scaleColors = scaleColors;
		
		if (titleMap == null) {
			titleMap = new HashMap<Long, String>();
		}
		if (datasetDescriptions == null) {
			datasetDescriptions = new ArrayList<String>();
		}
		if (bookmarkMap == null) {
			bookmarkMap = new HashMap<Integer, String>();
		}
		
		String rectTags = getRectTagsFromDataAndTitle(dataArrays, titleMap,
				datasetDescriptions, bookmarkMap);
		this.replaceMap.put(REPLACE_SVG, rectTags);
		
		// calculate total width
		int totalWidth = pixelWidth * dataArrays.size();
		totalWidth += bookmarkMap.size() * BOOKMARK_SIZE;
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
		
		String legendTags = getLegendTags(dataArrays, titleMap);
		this.replaceMap.put(REPLACE_LEGEND, legendTags);
		// Set legend height
		int legendHeight = 0;
		if (displayLegend) {
			legendHeight = (LEGEND_SIZE + LEGEND_SPACE) * titleMap.size();
		}
		this.replaceMap.put(REPLACE_LEGEND_HEIGHT, "" + legendHeight);
		
		return this;
	}
	
	protected String getBookmarkTag(Map<Integer, String> bookmarkMap,
			int index, int xOffset) {
		if (!bookmarkMap.containsKey(index)) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		String textTag;
		if (vertical) {
			textTag = getTextTag(xOffset - BOOKMARK_SIZE + (BOOKMARK_SIZE) / 3,
					2, BOOKMARK_SIZE, "writing-mode:tb",
					bookmarkMap.get(index));
		} else {
			textTag = getTextTag(2, xOffset - 1, BOOKMARK_SIZE, null,
					bookmarkMap.get(index));
		}
		builder.append("\t\t").append(textTag).append("\n");
		return builder.toString();
	}

	/**
	 * Get (a lot of) SVG rect tags for visualization.
	 * @param dataArrays
	 * @param titleMap
	 * @param datasetDescriptions
	 * @param bookmarkMap
	 * @return
	 */
	protected String getRectTagsFromDataAndTitle(
			List<long[]> dataArrays, Map<Long, String> titleMap,
			List<String> datasetDescriptions,
			Map<Integer, String> bookmarkMap) {
		final String tt = "\t\t";
		
		LinearScale colorScale = getColorScale(dataArrays, colorCategory);
		
		StringBuilder builder = new StringBuilder();
		
		int xOffset = 0;
		for (int i = 0; i < dataArrays.size(); i++) {
			if (bookmarkMap.containsKey(i)) {
				xOffset += BOOKMARK_SIZE;
				builder.append(getBookmarkTag(bookmarkMap, i, xOffset));
			}
			
			long[] dataArray = dataArrays.get(i);
			String datasetDescription = "";
			if (i < datasetDescriptions.size()) {
				datasetDescription = datasetDescriptions.get(i);
			}
			
			StringBuilder columnBuilder = new StringBuilder();
			int yOffset = 0;
			for (int j = 0; j < dataArray.length; j++) {
				String color = colorCategory.getColor(
						dataArray[j], colorScale);
				String style = "fill:" + color;
				String title = "";
				if (!datasetDescription.isEmpty()) {
					title += datasetDescription;
				}
				if (titleMap.containsKey(dataArray[j])) {
					if (!title.isEmpty()) {
						title += HTML_LF;
					}
					title += titleMap.get(dataArray[j]);
				}
				String rectTag;
				if (vertical) {
					rectTag = getRectTag(xOffset, yOffset,
							pixelWidth, pixelHeight,
							style, title);
				} else {
					rectTag = getRectTag(yOffset, xOffset,
							pixelHeight, pixelWidth,
							style, title);
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

	protected String getLegendTags(List<long[]> dataArrays,
			Map<Long, String> titleMap) {
		if (!displayLegend) {
			return "";
		}
		final String tt = "\t\t";
		LinearScale colorScale = getColorScale(dataArrays, colorCategory);
		StringBuilder builder = new StringBuilder();
		
		final int step = LEGEND_SIZE + LEGEND_SPACE;
		final int rectXOffset = LEGEND_SPACE;
		final int textXOffset = rectXOffset + step;
		final int textYOffset = LEGEND_SIZE - 2;
		
		int yOffset = 5;
		
		for (Map.Entry<Long, String> entry : titleMap.entrySet()) {
			String rectFill = colorCategory.getColor(entry.getKey(),
					colorScale);
			String title = entry.getValue();
			String rectTag = getRectTag(rectXOffset, yOffset,
					LEGEND_SIZE, LEGEND_SIZE, "fill:" + rectFill, title);
			String textTag = getTextTag(textXOffset, yOffset + textYOffset,
					LEGEND_SIZE, null, title);
			builder.append(tt).append(rectTag).append("\n")
					.append(tt).append(textTag).append("\n");
			
			yOffset += step;
		}
		
		return builder.toString();
	}

	protected LinearScale getColorScale(List<long[]> dataArrays,
			ColorCategory colorCategory) {
		if (!scaleColors) {
			return null;
		}
		long min = dataArrays.stream()
				.map(a -> Arrays.stream(a).min().getAsLong())
				.min(Long::compare)
				.get();
		long max = dataArrays.stream()
				.map(a -> Arrays.stream(a).max().getAsLong())
				.max(Long::compare)
				.get();
		return colorCategory.getLinearScale(min, max);
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
			String style, String title) {
		String result = "<rect x='" + x + "' y='" + y + "' width='" + width
				+ "' height='" + height + "' style='" + style + "'>";
		if (title != null && !title.isEmpty()) {
			result += "<title>" + title + "</title>";
		}
		result += "</rect>";
		return result;
	}
	
	/**
	 * Get a svg text tag
	 * @param x
	 * @param y
	 * @param fontSize
	 * @param style
	 * @param text
	 * @return
	 */
	protected static String getTextTag(int x, int y, int fontSize,
			String style, String text) {
		String textTag = "<text x='" + x + "' y='" + y + "' font-size='"
			+ fontSize + "'";
		if (style != null && !style.isEmpty()) {
			textTag += " style='" + style + "'";
		}
		textTag += ">" + text + "</text>";
		return textTag;
	}
	
	/**
	 * Get an integer attribute from an HTML tag
	 * @param tag
	 * @param attr
	 * @return
	 */
	protected static int getIntAttr(String tag, String attr) {
		return Integer.parseInt(getStringAttr(tag, attr));
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
			String currentFill = getStringAttr(tags[i], "style");
			String nextFill = getStringAttr(tags[i + 1], "style");
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
				tags[i + 1] = tags[i].replace(tagName + "='" + currentVal,
						tagName + "='" + newVal);
				tags[i] = "";
			}
		}
		return Arrays.stream(tags)
				.filter(s -> !s.isEmpty())
				.collect(Collectors.joining("\n"));
	}
	
}
