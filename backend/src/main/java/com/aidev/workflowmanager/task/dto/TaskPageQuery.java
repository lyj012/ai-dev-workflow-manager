package com.aidev.workflowmanager.task.dto;

import com.aidev.workflowmanager.common.enums.Complexity;
import com.aidev.workflowmanager.common.enums.RiskTag;
import com.aidev.workflowmanager.common.enums.TaskStatus;
import com.aidev.workflowmanager.common.enums.TaskType;
import com.aidev.workflowmanager.common.page.PageQuery;

import java.util.ArrayList;
import java.util.List;

public class TaskPageQuery extends PageQuery {

    private TaskStatus status;
    private TaskType taskType;
    private Complexity complexity;
    private List<RiskTag> riskTags = new ArrayList<RiskTag>();

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
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
}
