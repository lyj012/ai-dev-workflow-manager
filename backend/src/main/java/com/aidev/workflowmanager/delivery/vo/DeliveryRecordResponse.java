package com.aidev.workflowmanager.delivery.vo;

import com.aidev.workflowmanager.delivery.entity.DeliveryRecord;

public class DeliveryRecordResponse {

    private String testChecklist;

    private String testResult;

    private String riskNotes;

    private String unverifiedScope;

    private String summary;

    private String markdown;

    public static DeliveryRecordResponse from(DeliveryRecord record, String unverifiedScope) {
        DeliveryRecordResponse response = new DeliveryRecordResponse();
        if (record != null) {
            response.setTestChecklist(record.getTestChecklist());
            response.setTestResult(record.getTestResult());
            response.setRiskNotes(record.getRiskNotes());
            response.setSummary(record.getSummary());
            response.setMarkdown(record.getMarkdownContent());
        }
        response.setUnverifiedScope(unverifiedScope);
        return response;
    }

    public String getTestChecklist() {
        return testChecklist;
    }

    public void setTestChecklist(String testChecklist) {
        this.testChecklist = testChecklist;
    }

    public String getTestResult() {
        return testResult;
    }

    public void setTestResult(String testResult) {
        this.testResult = testResult;
    }

    public String getRiskNotes() {
        return riskNotes;
    }

    public void setRiskNotes(String riskNotes) {
        this.riskNotes = riskNotes;
    }

    public String getUnverifiedScope() {
        return unverifiedScope;
    }

    public void setUnverifiedScope(String unverifiedScope) {
        this.unverifiedScope = unverifiedScope;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getMarkdown() {
        return markdown;
    }

    public void setMarkdown(String markdown) {
        this.markdown = markdown;
    }
}
