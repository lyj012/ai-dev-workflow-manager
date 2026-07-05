package com.aidev.workflowmanager.delivery.vo;

import com.aidev.workflowmanager.stage.vo.WorkflowStageResponse;
import com.aidev.workflowmanager.task.vo.TaskDetailResponse;

import java.util.List;

public class DeliveryPreviewResponse {

    private TaskDetailResponse task;

    private String workflowName;

    private List<WorkflowStageResponse> stages;

    private DeliveryRecordResponse record;

    private String markdown;

    public TaskDetailResponse getTask() {
        return task;
    }

    public void setTask(TaskDetailResponse task) {
        this.task = task;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public List<WorkflowStageResponse> getStages() {
        return stages;
    }

    public void setStages(List<WorkflowStageResponse> stages) {
        this.stages = stages;
    }

    public DeliveryRecordResponse getRecord() {
        return record;
    }

    public void setRecord(DeliveryRecordResponse record) {
        this.record = record;
    }

    public String getMarkdown() {
        return markdown;
    }

    public void setMarkdown(String markdown) {
        this.markdown = markdown;
    }
}
