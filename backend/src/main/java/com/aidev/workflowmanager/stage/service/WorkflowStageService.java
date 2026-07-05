package com.aidev.workflowmanager.stage.service;

import com.aidev.workflowmanager.stage.dto.StageOutputRequest;
import com.aidev.workflowmanager.stage.vo.StageInitResponse;
import com.aidev.workflowmanager.stage.vo.WorkflowStageResponse;

public interface WorkflowStageService {

    StageInitResponse initializeStages(Long taskId);

    WorkflowStageResponse startStage(Long taskId, Long stageId);

    WorkflowStageResponse completeStage(Long taskId, Long stageId, StageOutputRequest request);

    WorkflowStageResponse skipStage(Long taskId, Long stageId);

    WorkflowStageResponse failStage(Long taskId, Long stageId, StageOutputRequest request);

    StageOutputRequest saveOutput(Long taskId, Long stageId, StageOutputRequest request);

    StageOutputRequest getOutput(Long taskId, Long stageId);
}
