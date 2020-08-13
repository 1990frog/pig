package com.clinbrain.dip.rest.request;

public class ProjectRequest {
    private String formerProjectName;

    private String projectDescData;

    public ProjectRequest() {
    }

    public String getProjectDescData() {
        return projectDescData;
    }

    public void setProjectDescData(String projectDescData) {
        this.projectDescData = projectDescData;
    }

    public String getFormerProjectName() {
        return formerProjectName;
    }

    public void setFormerProjectName(String formerProjectName) {
        this.formerProjectName = formerProjectName;
    }
}
