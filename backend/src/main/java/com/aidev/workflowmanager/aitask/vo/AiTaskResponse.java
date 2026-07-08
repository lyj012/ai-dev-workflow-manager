package com.aidev.workflowmanager.aitask.vo;

import com.aidev.workflowmanager.aitask.domain.AiTaskStatus;
import com.aidev.workflowmanager.aitask.domain.AiTaskType;
import com.aidev.workflowmanager.aitask.entity.AiTask;

import java.time.LocalDateTime;

public class AiTaskResponse {

    private Long id;
    private String title;
    private String requirement;
    private AiTaskType taskType;
    private String remark;
    private AiTaskStatus status;
    private Integer retryCount;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AiTaskResponse from(AiTask task) {
        AiTaskResponse response = new AiTaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setRequirement(task.getRequirement());
        response.setTaskType(task.getTaskType());
        response.setRemark(task.getRemark());
        response.setStatus(task.getStatus());
        response.setRetryCount(task.getRetryCount());
        response.setErrorMessage(task.getErrorMessage());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public AiTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(AiTaskType taskType) {
        this.taskType = taskType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public AiTaskStatus getStatus() {
        return status;
    }

    public void setStatus(AiTaskStatus status) {
        this.status = status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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
