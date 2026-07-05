package com.aidev.workflowmanager.task.vo;

import com.aidev.workflowmanager.common.enums.RiskTag;
import com.aidev.workflowmanager.stage.vo.WorkflowStageResponse;
import com.aidev.workflowmanager.task.entity.WorkflowTask;

import java.util.ArrayList;
import java.util.List;

public class TaskDetailResponse extends TaskResponse {

    private Long matchedTemplateId;
    private String matchedTemplateName;
    private Long deliveryRecordId;
    private Boolean testChecklistGenerated;
    private List<WorkflowStageResponse> stages = new ArrayList<WorkflowStageResponse>();

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

    public String getMatchedTemplateName() {
        return matchedTemplateName;
    }

    public void setMatchedTemplateName(String matchedTemplateName) {
        this.matchedTemplateName = matchedTemplateName;
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

    public List<WorkflowStageResponse> getStages() {
        return stages;
    }

    public void setStages(List<WorkflowStageResponse> stages) {
        this.stages = stages;
    }
}
