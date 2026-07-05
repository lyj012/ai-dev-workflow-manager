package com.aidev.workflowmanager.stage.controller;

import com.aidev.workflowmanager.common.api.ApiResponse;
import com.aidev.workflowmanager.stage.dto.StageOutputRequest;
import com.aidev.workflowmanager.stage.service.WorkflowStageService;
import com.aidev.workflowmanager.stage.vo.StageInitResponse;
import com.aidev.workflowmanager.stage.vo.WorkflowStageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/{stageId}/start")
    public ApiResponse<WorkflowStageResponse> startStage(@PathVariable Long taskId, @PathVariable Long stageId) {
        return ApiResponse.success(workflowStageService.startStage(taskId, stageId));
    }

    @PostMapping("/{stageId}/complete")
    public ApiResponse<WorkflowStageResponse> completeStage(@PathVariable Long taskId, @PathVariable Long stageId,
                                                            @RequestBody(required = false) StageOutputRequest request) {
        return ApiResponse.success(workflowStageService.completeStage(taskId, stageId, request));
    }

    @PostMapping("/{stageId}/skip")
    public ApiResponse<WorkflowStageResponse> skipStage(@PathVariable Long taskId, @PathVariable Long stageId) {
        return ApiResponse.success(workflowStageService.skipStage(taskId, stageId));
    }

    @PostMapping("/{stageId}/fail")
    public ApiResponse<WorkflowStageResponse> failStage(@PathVariable Long taskId, @PathVariable Long stageId,
                                                        @RequestBody(required = false) StageOutputRequest request) {
        return ApiResponse.success(workflowStageService.failStage(taskId, stageId, request));
    }

    @PostMapping("/{stageId}/outputs")
    public ApiResponse<StageOutputRequest> saveOutput(@PathVariable Long taskId, @PathVariable Long stageId,
                                                      @RequestBody StageOutputRequest request) {
        return ApiResponse.success(workflowStageService.saveOutput(taskId, stageId, request));
    }

    @GetMapping("/{stageId}/outputs")
    public ApiResponse<StageOutputRequest> getOutput(@PathVariable Long taskId, @PathVariable Long stageId) {
        return ApiResponse.success(workflowStageService.getOutput(taskId, stageId));
    }
}
