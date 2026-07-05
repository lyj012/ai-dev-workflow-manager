package com.aidev.workflowmanager.stage.vo;

import org.springframework.util.StringUtils;

public class StageOutputParts {

    private static final String OUTPUT_LABEL = "输出摘要";
    private static final String RISK_LABEL = "风险点";
    private static final String NEXT_LABEL = "后续动作";
    private static final String UNVERIFIED_LABEL = "未验证范围";

    private String outputSummary = "";
    private String riskPoints = "";
    private String nextActions = "";
    private String unverifiedScope = "";

    public static StageOutputParts of(String outputSummary, String riskPoints, String nextActions, String unverifiedScope) {
        StageOutputParts parts = new StageOutputParts();
        parts.setOutputSummary(trimToEmpty(outputSummary));
        parts.setRiskPoints(trimToEmpty(riskPoints));
        parts.setNextActions(trimToEmpty(nextActions));
        parts.setUnverifiedScope(trimToEmpty(unverifiedScope));
        return parts;
    }

    public static StageOutputParts parse(String text) {
        StageOutputParts parts = new StageOutputParts();
        if (!StringUtils.hasText(text)) {
            return parts;
        }
        if (!text.contains(OUTPUT_LABEL + ":")) {
            parts.setOutputSummary(text);
            return parts;
        }
        parts.setOutputSummary(extract(text, OUTPUT_LABEL, RISK_LABEL));
        parts.setRiskPoints(extract(text, RISK_LABEL, NEXT_LABEL));
        parts.setNextActions(extract(text, NEXT_LABEL, UNVERIFIED_LABEL));
        parts.setUnverifiedScope(extract(text, UNVERIFIED_LABEL, null));
        return parts;
    }

    public String format() {
        return OUTPUT_LABEL + ":\n" + outputSummary
                + "\n\n" + RISK_LABEL + ":\n" + riskPoints
                + "\n\n" + NEXT_LABEL + ":\n" + nextActions
                + "\n\n" + UNVERIFIED_LABEL + ":\n" + unverifiedScope;
    }

    private static String extract(String text, String currentLabel, String nextLabel) {
        String startMarker = currentLabel + ":";
        int start = text.indexOf(startMarker);
        if (start < 0) {
            return "";
        }
        start += startMarker.length();
        int end = nextLabel == null ? text.length() : text.indexOf("\n\n" + nextLabel + ":", start);
        if (end < 0) {
            end = text.indexOf(nextLabel + ":", start);
        }
        if (end < 0) {
            end = text.length();
        }
        return text.substring(start, end).trim();
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    public String getOutputSummary() {
        return outputSummary;
    }

    public void setOutputSummary(String outputSummary) {
        this.outputSummary = trimToEmpty(outputSummary);
    }

    public String getRiskPoints() {
        return riskPoints;
    }

    public void setRiskPoints(String riskPoints) {
        this.riskPoints = trimToEmpty(riskPoints);
    }

    public String getNextActions() {
        return nextActions;
    }

    public void setNextActions(String nextActions) {
        this.nextActions = trimToEmpty(nextActions);
    }

    public String getUnverifiedScope() {
        return unverifiedScope;
    }

    public void setUnverifiedScope(String unverifiedScope) {
        this.unverifiedScope = trimToEmpty(unverifiedScope);
    }
}
