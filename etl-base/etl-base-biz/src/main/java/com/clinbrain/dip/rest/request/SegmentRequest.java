package com.clinbrain.dip.rest.request;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SegmentRequest {

    public enum BuildType {
        BUILD,MERGE,REFRESH
    }

    private long startTime;
    private long endTime;
    private BuildType buildType;
    private String cubeName;

    public SegmentRequest(long startTime, long endTime,BuildType buildType,String cubeName) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.buildType = buildType;
        this.cubeName = cubeName;
    }

    public long getStartTime() {
        return startTime;
    }


    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }


    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public BuildType getBuildType(){
        return buildType;
    }

    public void setBuildType(BuildType buildType){
        this.buildType= buildType;
    }
    @JsonIgnore
    public String getCubeName(){
        return cubeName;
    }

    public void setCubeName(String cubeName){
        this.cubeName= cubeName;
    }
}
