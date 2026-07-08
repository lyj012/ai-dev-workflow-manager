package com.aidev.workflowmanager.aitask.entity;

import com.aidev.workflowmanager.aitask.domain.AiTaskLogLevel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("ai_task_log")
public class AiTaskLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private AiTaskLogLevel logLevel;
    private String logNode;
    private String message;
    private LocalDateTime createdAt;

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
