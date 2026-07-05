package com.aidev.workflowmanager.prompt.controller;

import com.aidev.workflowmanager.common.api.ApiResponse;
import com.aidev.workflowmanager.prompt.service.PromptService;
import com.aidev.workflowmanager.prompt.vo.GeneratePromptResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/tasks/{taskId}/stages/{stageId}")
public class PromptController {

    private final PromptService promptService;

    public PromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    @PostMapping("/generate-prompt")
    public ApiResponse<GeneratePromptResponse> generatePrompt(@PathVariable Long taskId, @PathVariable Long stageId) {
        return ApiResponse.success(promptService.generatePrompt(taskId, stageId));
    }
}
