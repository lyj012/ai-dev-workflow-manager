package com.aidev.workflowmanager.prompt.service;

import com.aidev.workflowmanager.prompt.vo.GeneratePromptResponse;

public interface PromptService {

    GeneratePromptResponse generatePrompt(Long taskId, Long stageId);
}
