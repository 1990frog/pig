package com.clinbrain.dip.rest.request;

public class CubeRequest {
    private String uuid;
    private String cubeName;
    private String cubeDescData;
    private String streamingData;
    private String kafkaData;
    private boolean successful;
    private String message;
    private String project;
    private String streamingCube;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message
     *            the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the status
     */
    public boolean getSuccessful() {
        return successful;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setSuccessful(boolean status) {
        this.successful = status;
    }

    public CubeRequest() {
    }

    public CubeRequest(String cubeName, String cubeDescData) {
        this.cubeName = cubeName;
        this.cubeDescData = cubeDescData;
    }

    public String getCubeDescData() {
        return cubeDescData;
    }

    public void setCubeDescData(String cubeDescData) {
        this.cubeDescData = cubeDescData;
    }

    /**
     * @return the cubeDescName
     */
    public String getCubeName() {
        return cubeName;
    }

    /**
     * @param cubeName
     *            the cubeDescName to set
     */
    public void setCubeName(String cubeName) {
        this.cubeName = cubeName;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getStreamingCube() {
        return streamingCube;
    }

    public void setStreamingCube(String streamingCube) {
        this.streamingCube = streamingCube;
    }

    public String getStreamingData() {
        return streamingData;
    }

    public void setStreamingData(String streamingData) {
        this.streamingData = streamingData;
    }

    public String getKafkaData() {
        return kafkaData;
    }

    public void setKafkaData(String kafkaData) {
        this.kafkaData = kafkaData;
    }
}
