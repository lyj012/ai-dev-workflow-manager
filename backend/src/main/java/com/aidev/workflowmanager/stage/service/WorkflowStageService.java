package com.aidev.workflowmanager.stage.service;

import com.aidev.workflowmanager.stage.vo.StageInitResponse;

public interface WorkflowStageService {

    StageInitResponse initializeStages(Long taskId);
}
