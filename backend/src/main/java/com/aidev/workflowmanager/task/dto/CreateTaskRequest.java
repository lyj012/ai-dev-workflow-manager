package com.aidev.workflowmanager.task.dto;

import com.aidev.workflowmanager.common.enums.Complexity;
import com.aidev.workflowmanager.common.enums.RiskTag;
import com.aidev.workflowmanager.common.enums.TaskType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = false)
public class CreateTaskRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    private String description;

    @NotNull
    private TaskType taskType;

    @NotNull
    private Complexity complexity;

    private List<RiskTag> riskTags = new ArrayList<RiskTag>();

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

    @JsonProperty("status")
    public void rejectStatus(Object ignored) {
        throw new IllegalArgumentException("status is not accepted; task status is forced to DRAFT");
    }
}
