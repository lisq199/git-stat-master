package io.ologn.gitstat.vis.chartjs;

/**
 * Class for easily generating Chart.js code for data
 * @author lisq199
 *
 */
public class ChartJsData {

	private int length;
	private String[] labels;
	private ChartJsDataset[] datasets;
	
	public ChartJsData(String[] labels, ChartJsDataset[] datasets) {
		this.labels = labels;
		this.length = labels.length;
		this.datasets = datasets;
	}
	
	public int getLength() {
		return length;
	}
	
	@Override
	public String toString() {
		String labelsString = "[";
		for (String i : labels) {
			labelsString += "\"" + i + "\", ";
		}
		labelsString = labelsString.substring(0, labelsString.length() - 2)
				+ "]";
		String datasetsString = "[";
		for (ChartJsDataset i : datasets) {
			datasetsString += i;
		}
		datasetsString += "]";
		return "{\n"
				+ "labels: " + labelsString + ",\n"
				+ "datasets: " + datasetsString
				+ "\n}";
	}
	
}
