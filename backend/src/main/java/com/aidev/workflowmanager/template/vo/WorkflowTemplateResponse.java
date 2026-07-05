package com.aidev.workflowmanager.template.vo;

import com.aidev.workflowmanager.common.enums.Complexity;
import com.aidev.workflowmanager.common.enums.RiskTag;
import com.aidev.workflowmanager.common.enums.TaskType;
import com.aidev.workflowmanager.template.entity.WorkflowTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorkflowTemplateResponse {

    private Long id;

    private String name;

    private String description;

    private List<TaskType> applicableTaskTypes = new ArrayList<TaskType>();

    private List<Complexity> applicableComplexities = new ArrayList<Complexity>();

    private List<RiskTag> riskTags = new ArrayList<RiskTag>();

    private Integer priority;

    private String version;

    private Boolean enabled;

    private List<WorkflowTemplateStageResponse> stages = new ArrayList<WorkflowTemplateStageResponse>();

    public static WorkflowTemplateResponse from(WorkflowTemplate template, List<WorkflowTemplateStageResponse> stages) {
        WorkflowTemplateResponse response = new WorkflowTemplateResponse();
        response.setId(template.getId());
        response.setName(template.getName());
        response.setDescription(template.getDescription());
        response.setApplicableTaskTypes(template.getTaskType() == null
                ? Collections.<TaskType>emptyList()
                : Collections.singletonList(template.getTaskType()));
        response.setApplicableComplexities(template.getComplexity() == null
                ? Collections.<Complexity>emptyList()
                : Collections.singletonList(template.getComplexity()));
        response.setRiskTags(template.getRiskTags() == null ? Collections.<RiskTag>emptyList() : template.getRiskTags());
        response.setPriority(template.getPriority());
        response.setVersion(template.getVersion() == null ? "" : String.valueOf(template.getVersion()));
        response.setEnabled(template.getEnabled());
        response.setStages(stages == null ? Collections.<WorkflowTemplateStageResponse>emptyList() : stages);
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TaskType> getApplicableTaskTypes() {
        return applicableTaskTypes;
    }

    public void setApplicableTaskTypes(List<TaskType> applicableTaskTypes) {
        this.applicableTaskTypes = applicableTaskTypes;
    }

    public List<Complexity> getApplicableComplexities() {
        return applicableComplexities;
    }

    public void setApplicableComplexities(List<Complexity> applicableComplexities) {
        this.applicableComplexities = applicableComplexities;
    }

    public List<RiskTag> getRiskTags() {
        return riskTags;
    }

    public void setRiskTags(List<RiskTag> riskTags) {
        this.riskTags = riskTags;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public List<WorkflowTemplateStageResponse> getStages() {
        return stages;
    }

    public void setStages(List<WorkflowTemplateStageResponse> stages) {
        this.stages = stages;
    }
}
