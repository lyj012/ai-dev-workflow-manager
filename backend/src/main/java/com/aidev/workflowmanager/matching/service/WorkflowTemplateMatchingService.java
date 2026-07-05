package com.aidev.workflowmanager.matching.service;

import com.aidev.workflowmanager.matching.vo.TemplateMatchResponse;

public interface WorkflowTemplateMatchingService {

    TemplateMatchResponse matchTemplate(Long taskId);
}
