package com.aidev.workflowmanager.stage.vo;

import java.util.ArrayList;
import java.util.List;

public class StageInitResponse {

    private Long taskId;

    private Long matchedTemplateId;

    private List<WorkflowStageResponse> stages = new ArrayList<WorkflowStageResponse>();

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getMatchedTemplateId() {
        return matchedTemplateId;
    }

    public void setMatchedTemplateId(Long matchedTemplateId) {
        this.matchedTemplateId = matchedTemplateId;
    }

    public List<WorkflowStageResponse> getStages() {
        return stages;
    }

    public void setStages(List<WorkflowStageResponse> stages) {
        this.stages = stages;
    }
}
