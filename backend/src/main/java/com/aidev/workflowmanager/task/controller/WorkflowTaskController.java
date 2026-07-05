package com.aidev.workflowmanager.task.controller;

import com.aidev.workflowmanager.common.api.ApiResponse;
import com.aidev.workflowmanager.common.page.PageResult;
import com.aidev.workflowmanager.task.dto.CreateTaskRequest;
import com.aidev.workflowmanager.task.dto.TaskPageQuery;
import com.aidev.workflowmanager.task.service.WorkflowTaskService;
import com.aidev.workflowmanager.task.vo.TaskDetailResponse;
import com.aidev.workflowmanager.task.vo.TaskResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/tasks")
public class WorkflowTaskController {

    private final WorkflowTaskService workflowTaskService;

    public WorkflowTaskController(WorkflowTaskService workflowTaskService) {
        this.workflowTaskService = workflowTaskService;
    }

    @PostMapping
    public ApiResponse<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        return ApiResponse.success(workflowTaskService.create(request));
    }

    @GetMapping
    public ApiResponse<PageResult<TaskResponse>> page(@Valid TaskPageQuery query) {
        return ApiResponse.success(workflowTaskService.page(query));
    }

    @GetMapping("/{taskId}")
    public ApiResponse<TaskDetailResponse> detail(@PathVariable Long taskId) {
        return ApiResponse.success(workflowTaskService.detail(taskId));
    }
}
