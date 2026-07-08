package com.aidev.workflowmanager.aitask.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("ai_task_result")
public class AiTaskResult {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String summary;
    private String riskPoints;
    private String suggestedSteps;
    private String testSuggestions;
    private String rawResult;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
