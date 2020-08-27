package com.clinbrain.dip.rest.response;

import java.io.Serializable;
import java.util.List;

public class SQLResponse implements Serializable {
	protected static final long serialVersionUID = 1L;
	
	protected List<String> columnName;
	
	protected List<List<String>> results;
	
	protected String cube;
	
	protected long duration;
	
	protected long totalScanCount;
	
	public SQLResponse() {
		
	}
	
	public SQLResponse(List<String> columnName, List<List<String>> results) {
		this.columnName = columnName;
		this.results = results;
	}
	
	public List<String> getColumnName() {
		return columnName;
	}
	
	public void setColumnName(List<String> columnName) {
		this.columnName = columnName;
	}
	
	public List<List<String>> getResults() {
		return results;
	}
	
	public void setResults(List<List<String>> results) {
		this.results = results;
	}
	
	public String getCube() {
		return cube;
	}
	
	public void setCube(String cube) {
		this.cube = cube;
	}
	
	public long getDuration() {
		return duration;
	}
	
	public void setDuration(long duration) {
		this.duration = duration;
	}
	
	public long getTotalScanCount() {
		return totalScanCount;
	}
	
	public void setTotalScanCount(long totalScanCount) {
		this.totalScanCount = totalScanCount;
	}
}
