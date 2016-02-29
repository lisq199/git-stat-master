package io.ologn.gitstat.vis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.ologn.common.color.ColorCategory;
import io.ologn.common.math.LinearScale;
import io.ologn.gitstat.utils.MyUtils;

/**
 * For creating visualization with color pixels, where each block 
 * (or color pixel) represents some data, and each column represents 
 * a data set. Ultimately it will generate an HTML string.<br>
 * Note: parseMap() must be called after all the attributes are set.<br>
 * Typical usage: {@code ColorPixels.init().setPixelHeight(2)
 * .setPixelWidth(3).parseMap(map, true).createHtmlString()}
 * @author lisq199
 */
public class ColorPixels implements VelocityHtmlGenerator {
	
	public static final String TEMPLATE_PATH = MyUtils.HTML_DIR
			+ "ColorPixels.html";
	
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
	 * Parse a Map and get the data from it
	 * @param map
	 * @param vertical
	 * @return
	 */
	public ColorPixels parseMap(Map<String, long[]> map, boolean vertical) {
		String svgTags = getSvgTagsFromMap(map,
				pixelWidth, pixelHeight, colorCategory, vertical);
		this.replaceMap.put(REPLACE_SVG, svgTags);
		// Calculate total width
		int totalWidth = pixelWidth * map.size();
		// Calculate total height
		int maxLen = -1;
		for (long[] arr : map.values()) {
			if (arr.length > maxLen) {
				maxLen = arr.length;
			}
		}
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
	 * @return
	 */
	public static String getRectTag(int x, int y, int width, int height,
			String fill) {
		return "<rect x=" + x + " y=" + y + " width=" + width + " height="
				+ height + " fill='" + fill + "' />";
	}
	
	/**
	 * For internal use only. 
	 * Get SVG tags from a Map.
	 * @param map
	 * @param pixelWidth
	 * @param pixelHeight
	 * @param colorCategory
	 * @param vertical
	 * @return
	 */
	protected static String getSvgTagsFromMap(Map<String, long[]> map,
			int pixelWidth, int pixelHeight, ColorCategory colorCategory,
			boolean vertical) {
		final String t = "\t";
		final String tt = t + t;
		
		long min = map.values().stream()
				.map(v -> Arrays.stream(v).min().getAsLong())
				.min(Long::compare)
				.get();
		long max = map.values().stream()
				.map(v -> Arrays.stream(v).max().getAsLong())
				.max(Long::compare)
				.get();
		
		LinearScale colorScale = colorCategory.getLinearScale(min, max);
		
		StringBuilder builder = new StringBuilder();
		
		int xOffset = 0;
		for (long[] arr : map.values()) {
			int yOffset = 0;
			for (long l : arr) {
				String color = colorCategory.getColor(l, colorScale);
				String rectTag = null;
				if (vertical) {
					rectTag = getRectTag(xOffset, yOffset,
							pixelWidth, pixelHeight, color);
				} else {
					rectTag = getRectTag(yOffset, xOffset,
							pixelHeight, pixelWidth, color);
				}
				builder.append(tt).append(rectTag).append("\n");
				yOffset += pixelHeight;
			}
			xOffset += pixelWidth;
		}
		return builder.toString();
	}
	
}
