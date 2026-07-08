package com.aidev.workflowmanager.aitask.vo;

import com.aidev.workflowmanager.aitask.domain.AiTaskLogLevel;
import com.aidev.workflowmanager.aitask.entity.AiTaskLog;

import java.time.LocalDateTime;

public class AiTaskLogResponse {

    private Long id;
    private Long taskId;
    private AiTaskLogLevel logLevel;
    private String logNode;
    private String message;
    private LocalDateTime createdAt;

    public static AiTaskLogResponse from(AiTaskLog log) {
        AiTaskLogResponse response = new AiTaskLogResponse();
        response.setId(log.getId());
        response.setTaskId(log.getTaskId());
        response.setLogLevel(log.getLogLevel());
        response.setLogNode(log.getLogNode());
        response.setMessage(log.getMessage());
        response.setCreatedAt(log.getCreatedAt());
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

    public AiTaskLogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(AiTaskLogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public String getLogNode() {
        return logNode;
    }

    public void setLogNode(String logNode) {
        this.logNode = logNode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
