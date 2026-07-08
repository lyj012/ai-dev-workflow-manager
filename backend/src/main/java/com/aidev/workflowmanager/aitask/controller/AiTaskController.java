package com.aidev.workflowmanager.aitask.controller;

import com.aidev.workflowmanager.aitask.dto.AiTaskPageQuery;
import com.aidev.workflowmanager.aitask.dto.CreateAiTaskRequest;
import com.aidev.workflowmanager.aitask.service.AiTaskService;
import com.aidev.workflowmanager.aitask.vo.AiTaskLogResponse;
import com.aidev.workflowmanager.aitask.vo.AiTaskResponse;
import com.aidev.workflowmanager.aitask.vo.AiTaskResultResponse;
import com.aidev.workflowmanager.common.api.ApiResponse;
import com.aidev.workflowmanager.common.page.PageResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/ai-tasks")
public class AiTaskController {

    private final AiTaskService aiTaskService;

    public AiTaskController(AiTaskService aiTaskService) {
        this.aiTaskService = aiTaskService;
    }

    @PostMapping
    public ApiResponse<AiTaskResponse> create(@Valid @RequestBody CreateAiTaskRequest request) {
        return ApiResponse.success(aiTaskService.create(request));
    }

    @GetMapping
    public ApiResponse<PageResult<AiTaskResponse>> page(@Valid AiTaskPageQuery query) {
        return ApiResponse.success(aiTaskService.page(query));
    }

    @GetMapping("/{taskId}")
    public ApiResponse<AiTaskResponse> detail(@PathVariable Long taskId) {
        return ApiResponse.success(aiTaskService.detail(taskId));
    }

    @GetMapping("/{taskId}/logs")
    public ApiResponse<List<AiTaskLogResponse>> logs(@PathVariable Long taskId) {
        return ApiResponse.success(aiTaskService.logs(taskId));
    }

    @GetMapping("/{taskId}/result")
    public ApiResponse<AiTaskResultResponse> result(@PathVariable Long taskId) {
        return ApiResponse.success(aiTaskService.result(taskId));
    }

    @PostMapping("/{taskId}/retry")
    public ApiResponse<AiTaskResponse> retry(@PathVariable Long taskId) {
        return ApiResponse.success(aiTaskService.retry(taskId));
    }
}
