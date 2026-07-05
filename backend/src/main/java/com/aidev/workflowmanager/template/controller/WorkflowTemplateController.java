package com.aidev.workflowmanager.template.controller;

import com.aidev.workflowmanager.common.api.ApiResponse;
import com.aidev.workflowmanager.template.service.WorkflowTemplateService;
import com.aidev.workflowmanager.template.vo.WorkflowTemplateResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/workflow-templates")
public class WorkflowTemplateController {

    private final WorkflowTemplateService workflowTemplateService;

    public WorkflowTemplateController(WorkflowTemplateService workflowTemplateService) {
        this.workflowTemplateService = workflowTemplateService;
    }

    @GetMapping
    public ApiResponse<List<WorkflowTemplateResponse>> list() {
        return ApiResponse.success(workflowTemplateService.listEnabledTemplates());
    }

    @GetMapping("/{templateId}")
    public ApiResponse<WorkflowTemplateResponse> detail(@PathVariable Long templateId) {
        return ApiResponse.success(workflowTemplateService.detail(templateId));
    }
}
