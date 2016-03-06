package io.ologn.gitstat.vis.chartjs;

import java.util.HashMap;
import java.util.Map;

import io.ologn.common.color.ColorCategory;
import io.ologn.gitstat.vis.VelocityHtmlGenerator;

public class ChartJsPie implements VelocityHtmlGenerator {
	
	// Default colors
	public static final String DEF_COLOR_1 = "#F7464A",
			DEF_HIGHLIGHT_1 = "#FF5A5E",
			DEF_COLOR_2 = "#46BFBD",
			DEF_HIGHLIGHT_2 = "#5AD3D1",
			DEF_COLOR_3 = "#FDB45C",
			DEF_HIGHLIGHT_3 = "#FFC870";
	
	public static final String TEMPLATE_PATH =
			VelocityHtmlGenerator.TEMPLATE_DIR + "ChartJsPie.html";
	
	public static final String REPLACE_DATA = "data";
	
	protected Map<String, String> replaceMap;
	
	protected ChartJsPie() {
		this.replaceMap = new HashMap<String, String>();
	}
	
	@Override
	public String getTemplatePath() {
		return TEMPLATE_PATH;
	}

	@Override
	public Map<String, String> getReplaceMap() {
		return new HashMap<String, String>(replaceMap);
	}
	
	public ChartJsPie parseMap(Map<?, ?> map) {
		this.replaceMap.put(REPLACE_DATA, getDataFromMap(map));
		return this;
	}
	
	public static ChartJsPie init() {
		return new ChartJsPie();
	}
	
	public static String getDataFromMap(Map<?, ?> map) {
		final String t = "\t";
		final String tt = t + t;
		StringBuilder builder = new StringBuilder("[\n");
		int i = 0;
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			Object k = entry.getKey();
			Object v = entry.getValue();
			builder.append(t).append("{\n");
			builder.append(tt).append("value: ").append(v).append(",\n");
			builder.append(tt).append("color: '")
					.append(ColorCategory.D3_CATEGORY10.getColor(i))
					.append("',\n");
			builder.append(tt).append("label: ").append("'").append(k)
					.append("'").append("\n");
			builder.append(t).append("},\n");
			i++;
		}
		builder.append("]");
		return builder.toString();
	}

}
