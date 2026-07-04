package com.aidev.workflowmanager.task.entity;

import com.aidev.workflowmanager.common.enums.Complexity;
import com.aidev.workflowmanager.common.enums.RiskTag;
import com.aidev.workflowmanager.common.enums.TaskStatus;
import com.aidev.workflowmanager.common.enums.TaskType;
import com.aidev.workflowmanager.common.mybatis.RiskTagListTypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@TableName(value = "workflow_task", autoResultMap = true)
public class WorkflowTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private TaskType taskType;

    private Complexity complexity;

    @TableField(typeHandler = RiskTagListTypeHandler.class)
    private List<RiskTag> riskTags = new ArrayList<RiskTag>();

    private TaskStatus status;

    private Long matchedTemplateId;

    private Boolean testChecklistGenerated;

    private Long deliveryRecordId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public Complexity getComplexity() {
        return complexity;
    }

    public void setComplexity(Complexity complexity) {
        this.complexity = complexity;
    }

    public List<RiskTag> getRiskTags() {
        return riskTags;
    }

    public void setRiskTags(List<RiskTag> riskTags) {
        this.riskTags = riskTags;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Long getMatchedTemplateId() {
        return matchedTemplateId;
    }

    public void setMatchedTemplateId(Long matchedTemplateId) {
        this.matchedTemplateId = matchedTemplateId;
    }

    public Boolean getTestChecklistGenerated() {
        return testChecklistGenerated;
    }

    public void setTestChecklistGenerated(Boolean testChecklistGenerated) {
        this.testChecklistGenerated = testChecklistGenerated;
    }

    public Long getDeliveryRecordId() {
        return deliveryRecordId;
    }

    public void setDeliveryRecordId(Long deliveryRecordId) {
        this.deliveryRecordId = deliveryRecordId;
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

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
