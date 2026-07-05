package com.aidev.workflowmanager.stage.dto;

public class StageOutputRequest {

    private String outputSummary;

    private String riskPoints;

    private String nextActions;

    private String unverifiedScope;

    public String getOutputSummary() {
        return outputSummary;
    }

    public void setOutputSummary(String outputSummary) {
        this.outputSummary = outputSummary;
    }

    public String getRiskPoints() {
        return riskPoints;
    }

    public void setRiskPoints(String riskPoints) {
        this.riskPoints = riskPoints;
    }

    public String getNextActions() {
        return nextActions;
    }

    public void setNextActions(String nextActions) {
        this.nextActions = nextActions;
    }

    public String getUnverifiedScope() {
        return unverifiedScope;
    }

    public void setUnverifiedScope(String unverifiedScope) {
        this.unverifiedScope = unverifiedScope;
    }
}
