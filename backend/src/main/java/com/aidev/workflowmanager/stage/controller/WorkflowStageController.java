package com.aidev.workflowmanager.stage.controller;

import com.aidev.workflowmanager.common.api.ApiResponse;
import com.aidev.workflowmanager.stage.service.WorkflowStageService;
import com.aidev.workflowmanager.stage.vo.StageInitResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/tasks/{taskId}/stages")
public class WorkflowStageController {

    private final WorkflowStageService workflowStageService;

    public WorkflowStageController(WorkflowStageService workflowStageService) {
        this.workflowStageService = workflowStageService;
    }

    @PostMapping("/init")
    public ApiResponse<StageInitResponse> initializeStages(@PathVariable Long taskId) {
        return ApiResponse.success(workflowStageService.initializeStages(taskId));
    }
}
