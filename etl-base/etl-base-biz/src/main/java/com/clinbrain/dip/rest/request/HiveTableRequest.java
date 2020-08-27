package com.clinbrain.dip.rest.request;

public class HiveTableRequest {
    public HiveTableRequest(boolean calculate) {
        this.calculate = calculate;
    }

    public HiveTableRequest() {
    }

    public boolean isCalculate() {
        return calculate;
    }

    public void setCalculate(boolean calculate) {
        this.calculate = calculate;
    }

    private boolean calculate;
}
