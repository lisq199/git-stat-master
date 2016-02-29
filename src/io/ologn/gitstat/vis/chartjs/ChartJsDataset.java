package io.ologn.gitstat.vis.chartjs;

/**
 * Class for easily generating Chart.js code for datasets 
 * @author lisq199
 *
 */
public class ChartJsDataset {
	
	// default colors used in examples in the official documentation
	public static final int DEF_COLOR_1_RGB = 220,
			DEF_COLOR_2_R = 151,
			DEF_COLOR_2_G = 187,
			DEF_COLOR_2_B = 205;
	
	private String label,
	fillColor,
	strokeColor,
	pointColor,
	pointStrokeColor,
	pointHighlightFill,
	pointHighlightStroke;
	private float[] data;
	private int length;
	
	/**
	 * Creates a ChartJsDataset that has randomly generated data and colors
	 */
	public ChartJsDataset() {
		String color = "rgba(" + (int)(Math.random()*255) + ","
				+ (int)(Math.random()*255) + ","
				+ (int)(Math.random()*255) + ",";
		this.label = "Random dataset";
		this.fillColor = color + "0.2)";
		this.strokeColor = color + "1)";
		this.pointColor = color + "1)";
		this.pointStrokeColor = "#fff";
		this.pointHighlightFill = "#fff";
		this.pointHighlightStroke = color + "1)";
		this.data = new float[] {(int)(Math.random()*100),
				(int)(Math.random()*100), (int)(Math.random()*100),
				(int)(Math.random()*100), (int)(Math.random()*100)};
		this.length = data.length;
	}
	
	/**
	 * Creates a ChartJsDataset that contains a custom label, data and colors
	 * with default opacity
	 * @param label
	 * @param r
	 * @param g
	 * @param b
	 * @param data
	 */
	public ChartJsDataset(String label, int r, int g, int b, float[] data) {
		String color = "rgba(" + r + "," + g + "," + b + ",";
		this.label = label;
		this.fillColor = color + "0.2)";
		this.strokeColor = color + "1)";
		this.pointColor = color + "1)";
		this.pointStrokeColor = "#fff";
		this.pointHighlightFill = "#fff";
		this.pointHighlightStroke = color + "1)";
		this.data = data;
		this.length = data.length;
	}

	/**
	 * A constructor that allows maximum customization
	 * @param label
	 * @param fillColor
	 * @param strokeColor
	 * @param pointColor
	 * @param pointStrokeColor
	 * @param pointHighlightFill
	 * @param pointHighlightStroke
	 * @param data
	 */
	public ChartJsDataset(String label,
			String fillColor,
			String strokeColor,
			String pointColor,
			String pointStrokeColor,
			String pointHighlightFill,
			String pointHighlightStroke,
			float[] data) {
		this.label = label;
		this.fillColor = fillColor;
		this.strokeColor = strokeColor;
		this.pointColor = pointColor;
		this.pointStrokeColor = pointStrokeColor;
		this.pointHighlightFill = pointHighlightFill;
		this.pointHighlightStroke = pointHighlightStroke;
		this.data = data;
		this.length = data.length;
	}
	
	public int getLength() {
		return length;
	}
	
	@Override
	public String toString() {
		String dataString = "[";
		for (float i : data) {
			dataString += i + ", ";
		}
		dataString = dataString.substring(0, dataString.length() - 2) + "]";
		return "{\n"
				+ "label: \"" + label + "\",\n"
				+ "title: \"" + label + "\",\n"
				+ "fillColor: \"" + fillColor + "\",\n"
				+ "strokeColor: \"" + strokeColor + "\",\n"
				+ "pointColor: \"" + pointColor + "\",\n"
				+ "pointStrokeColor: \"" + pointStrokeColor + "\",\n"
				+ "pointHighlightFill: \"" + pointHighlightFill + "\",\n"
				+ "pointHighlightStroke: \"" + pointHighlightStroke + "\",\n"
				+ "data: " + dataString
				+ "\n},";
	}

}
