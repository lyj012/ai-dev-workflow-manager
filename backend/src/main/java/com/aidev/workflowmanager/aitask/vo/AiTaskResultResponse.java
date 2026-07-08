package com.aidev.workflowmanager.aitask.vo;

import com.aidev.workflowmanager.aitask.entity.AiTaskResult;

import java.time.LocalDateTime;

public class AiTaskResultResponse {

    private Long id;
    private Long taskId;
    private String summary;
    private String riskPoints;
    private String suggestedSteps;
    private String testSuggestions;
    private String rawResult;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AiTaskResultResponse from(AiTaskResult result) {
        if (result == null) {
            return null;
        }
        AiTaskResultResponse response = new AiTaskResultResponse();
        response.setId(result.getId());
        response.setTaskId(result.getTaskId());
        response.setSummary(result.getSummary());
        response.setRiskPoints(result.getRiskPoints());
        response.setSuggestedSteps(result.getSuggestedSteps());
        response.setTestSuggestions(result.getTestSuggestions());
        response.setRawResult(result.getRawResult());
        response.setCreatedAt(result.getCreatedAt());
        response.setUpdatedAt(result.getUpdatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getRiskPoints() {
        return riskPoints;
    }

    public void setRiskPoints(String riskPoints) {
        this.riskPoints = riskPoints;
    }

    public String getSuggestedSteps() {
        return suggestedSteps;
    }

    public void setSuggestedSteps(String suggestedSteps) {
        this.suggestedSteps = suggestedSteps;
    }

    public String getTestSuggestions() {
        return testSuggestions;
    }

    public void setTestSuggestions(String testSuggestions) {
        this.testSuggestions = testSuggestions;
    }

    public String getRawResult() {
        return rawResult;
    }

    public void setRawResult(String rawResult) {
        this.rawResult = rawResult;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
