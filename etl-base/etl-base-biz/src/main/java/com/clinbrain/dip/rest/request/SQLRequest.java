package com.clinbrain.dip.rest.request;

import java.io.Serializable;

public class SQLRequest implements Serializable {
	protected static final long serialVersionUID = 1L;
	
	private String project;
	private int offset = 0;
	private int limit = 50000;
	private String cube;
	private String mdxDescData;
	
	public SQLRequest() {
	}
	
	public String getProject() {
		return project.toUpperCase();
	}
	
	public void setProject(String project) {
		this.project = project;
	}
	
	public int getOffset() {
		return offset;
	}
	
	public void setOffset(Integer offset) {
		this.offset = offset;
	}
	
	public int getLimit() {
		return limit;
	}
	
	public void setLimit(Integer limit) {
		this.limit = limit;
	}
	
	public String getCube() {
		return cube;
	}
	
	public void setCube(String cube) {
		this.cube = cube;
	}
	
	public String getMdxDescData() {
		return mdxDescData;
	}
	
	public void setMdxDescdata(String mdxDescData) {
		this.mdxDescData = mdxDescData;
	}
}
