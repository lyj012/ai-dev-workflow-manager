package com.aidev.workflowmanager.task.vo;

import com.aidev.workflowmanager.common.enums.RiskTag;
import com.aidev.workflowmanager.task.entity.WorkflowTask;

import java.util.ArrayList;

public class TaskDetailResponse extends TaskResponse {

    private Long matchedTemplateId;
    private Long deliveryRecordId;
    private Boolean testChecklistGenerated;

    public static TaskDetailResponse from(WorkflowTask task) {
        TaskDetailResponse response = new TaskDetailResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setTaskType(task.getTaskType());
        response.setComplexity(task.getComplexity());
        response.setRiskTags(task.getRiskTags() == null ? new ArrayList<RiskTag>() : task.getRiskTags());
        response.setStatus(task.getStatus());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        response.setMatchedTemplateId(task.getMatchedTemplateId());
        response.setDeliveryRecordId(task.getDeliveryRecordId());
        response.setTestChecklistGenerated(task.getTestChecklistGenerated());
        return response;
    }

    public Long getMatchedTemplateId() {
        return matchedTemplateId;
    }

    public void setMatchedTemplateId(Long matchedTemplateId) {
        this.matchedTemplateId = matchedTemplateId;
    }

    public Long getDeliveryRecordId() {
        return deliveryRecordId;
    }

    public void setDeliveryRecordId(Long deliveryRecordId) {
        this.deliveryRecordId = deliveryRecordId;
    }

    public Boolean getTestChecklistGenerated() {
        return testChecklistGenerated;
    }

    public void setTestChecklistGenerated(Boolean testChecklistGenerated) {
        this.testChecklistGenerated = testChecklistGenerated;
    }
}
