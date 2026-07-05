package com.aidev.workflowmanager.template.vo;

import com.aidev.workflowmanager.template.entity.WorkflowTemplateStage;

public class WorkflowTemplateStageResponse {

    private Long id;

    private Long templateId;

    private Integer stageOrder;

    private String stageKey;

    private String stageName;

    private Boolean required;

    private String stageGoal;

    public static WorkflowTemplateStageResponse from(WorkflowTemplateStage stage) {
        WorkflowTemplateStageResponse response = new WorkflowTemplateStageResponse();
        response.setId(stage.getId());
        response.setTemplateId(stage.getTemplateId());
        response.setStageOrder(stage.getStageOrder());
        response.setStageKey(stage.getStageKey());
        response.setStageName(stage.getStageName());
        response.setRequired(stage.getRequired());
        response.setStageGoal(stage.getDescription());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Integer getStageOrder() {
        return stageOrder;
    }

    public void setStageOrder(Integer stageOrder) {
        this.stageOrder = stageOrder;
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

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getStageGoal() {
        return stageGoal;
    }

    public void setStageGoal(String stageGoal) {
        this.stageGoal = stageGoal;
    }
}
