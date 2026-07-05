package com.aidev.workflowmanager.matching.controller;

import com.aidev.workflowmanager.common.api.ApiResponse;
import com.aidev.workflowmanager.matching.service.WorkflowTemplateMatchingService;
import com.aidev.workflowmanager.matching.vo.TemplateMatchResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/tasks/{taskId}")
public class WorkflowTemplateMatchingController {

    private final WorkflowTemplateMatchingService workflowTemplateMatchingService;

    public WorkflowTemplateMatchingController(WorkflowTemplateMatchingService workflowTemplateMatchingService) {
        this.workflowTemplateMatchingService = workflowTemplateMatchingService;
    }

    @PostMapping("/match-template")
    public ApiResponse<TemplateMatchResponse> matchTemplate(@PathVariable Long taskId) {
        return ApiResponse.success(workflowTemplateMatchingService.matchTemplate(taskId));
    }
}
