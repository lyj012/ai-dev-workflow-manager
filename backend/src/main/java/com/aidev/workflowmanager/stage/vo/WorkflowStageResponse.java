package com.aidev.workflowmanager.stage.vo;

import com.aidev.workflowmanager.common.enums.StageStatus;
import com.aidev.workflowmanager.stage.entity.WorkflowStage;

import java.time.LocalDateTime;

public class WorkflowStageResponse {

    private Long id;

    private Long taskId;

    private Long templateStageId;

    private String stageKey;

    private String stageName;

    private Integer stageOrder;

    private StageStatus status;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    public static WorkflowStageResponse from(WorkflowStage stage) {
        WorkflowStageResponse response = new WorkflowStageResponse();
        response.setId(stage.getId());
        response.setTaskId(stage.getTaskId());
        response.setTemplateStageId(stage.getTemplateStageId());
        response.setStageKey(stage.getStageKey());
        response.setStageName(stage.getStageName());
        response.setStageOrder(stage.getStageOrder());
        response.setStatus(stage.getStatus());
        response.setStartedAt(stage.getStartedAt());
        response.setCompletedAt(stage.getCompletedAt());
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

    public Long getTemplateStageId() {
        return templateStageId;
    }

    public void setTemplateStageId(Long templateStageId) {
        this.templateStageId = templateStageId;
    }

    public String getStageKey() {
        return stageKey;
    }

    public void setStageKey(String stageKey) {
        this.stageKey = stageKey;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public Integer getStageOrder() {
        return stageOrder;
    }

    public void setStageOrder(Integer stageOrder) {
        this.stageOrder = stageOrder;
    }

    public StageStatus getStatus() {
        return status;
    }

    public void setStatus(StageStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
