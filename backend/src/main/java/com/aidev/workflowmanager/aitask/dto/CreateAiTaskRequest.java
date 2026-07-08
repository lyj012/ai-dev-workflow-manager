package com.aidev.workflowmanager.aitask.dto;

import com.aidev.workflowmanager.aitask.domain.AiTaskType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = false)
public class CreateAiTaskRequest {

    @NotBlank(message = "任务标题不能为空")
    @Size(max = 200, message = "任务标题长度不能超过 200 个字符")
    private String title;

    @NotBlank(message = "需求描述不能为空")
    @Size(max = 5000, message = "需求描述长度不能超过 5000 个字符")
    private String requirement;

    private AiTaskType taskType;

    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

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

    @JsonProperty("status")
    public void rejectStatus(Object ignored) {
        throw new IllegalArgumentException("创建 AI 任务时不能传入状态，任务状态由系统自动流转。");
    }
}
