package com.aidev.workflowmanager.stage.service.impl;

import com.aidev.workflowmanager.common.enums.StageStatus;
import com.aidev.workflowmanager.common.enums.TaskStatus;
import com.aidev.workflowmanager.common.exception.BusinessException;
import com.aidev.workflowmanager.common.exception.ErrorCode;
import com.aidev.workflowmanager.stage.entity.WorkflowStage;
import com.aidev.workflowmanager.stage.mapper.WorkflowStageMapper;
import com.aidev.workflowmanager.stage.service.WorkflowStageService;
import com.aidev.workflowmanager.stage.vo.StageInitResponse;
import com.aidev.workflowmanager.stage.vo.WorkflowStageResponse;
import com.aidev.workflowmanager.task.entity.WorkflowTask;
import com.aidev.workflowmanager.task.mapper.WorkflowTaskMapper;
import com.aidev.workflowmanager.template.entity.WorkflowTemplate;
import com.aidev.workflowmanager.template.entity.WorkflowTemplateStage;
import com.aidev.workflowmanager.template.mapper.WorkflowTemplateMapper;
import com.aidev.workflowmanager.template.mapper.WorkflowTemplateStageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class WorkflowStageServiceImpl implements WorkflowStageService {

    private final Map<Long, Object> taskInitLocks = new ConcurrentHashMap<Long, Object>();

    private final WorkflowTaskMapper workflowTaskMapper;
    private final WorkflowTemplateMapper workflowTemplateMapper;
    private final WorkflowTemplateStageMapper workflowTemplateStageMapper;
    private final WorkflowStageMapper workflowStageMapper;

    public WorkflowStageServiceImpl(WorkflowTaskMapper workflowTaskMapper,
                                    WorkflowTemplateMapper workflowTemplateMapper,
                                    WorkflowTemplateStageMapper workflowTemplateStageMapper,
                                    WorkflowStageMapper workflowStageMapper) {
        this.workflowTaskMapper = workflowTaskMapper;
        this.workflowTemplateMapper = workflowTemplateMapper;
        this.workflowTemplateStageMapper = workflowTemplateStageMapper;
        this.workflowStageMapper = workflowStageMapper;
    }

    @Override
    @Transactional
    public StageInitResponse initializeStages(Long taskId) {
        if (taskId == null || taskId < 1) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "taskId must be greater than or equal to 1");
        }

        WorkflowTask task = workflowTaskMapper.selectOne(new LambdaQueryWrapper<WorkflowTask>()
                .eq(WorkflowTask::getId, taskId));
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Task not found: " + taskId);
        }
        if (TaskStatus.ARCHIVED.equals(task.getStatus()) || TaskStatus.CANCELED.equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "Task status does not allow stage initialization: " + task.getStatus());
        }
        if (task.getMatchedTemplateId() == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "Task has no matched template");
        }

        Long templateId = task.getMatchedTemplateId();
        WorkflowTemplate template = workflowTemplateMapper.selectOne(new LambdaQueryWrapper<WorkflowTemplate>()
                .eq(WorkflowTemplate::getId, templateId)
                .eq(WorkflowTemplate::getEnabled, true));
        if (template == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Workflow template not found or disabled: " + templateId);
        }

        Object lock = taskInitLocks.computeIfAbsent(taskId, key -> new Object());
        synchronized (lock) {
            return initializeStagesWithLock(taskId, templateId);
        }
    }

    private StageInitResponse initializeStagesWithLock(Long taskId, Long templateId) {
        List<WorkflowTemplateStage> templateStages = workflowTemplateStageMapper.selectList(new LambdaQueryWrapper<WorkflowTemplateStage>()
                .eq(WorkflowTemplateStage::getTemplateId, templateId)
                .orderByAsc(WorkflowTemplateStage::getStageOrder));
        if (templateStages == null || templateStages.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "Workflow template has no stages: " + templateId);
        }

        List<WorkflowStage> existingStages = selectTaskStages(taskId);
        validateStageKeys(taskId, templateId, existingStages, templateStages);
        if (hasAllTemplateStages(existingStages, templateStages)) {
            return buildResponse(taskId, templateId, existingStages);
        }

        try {
            for (WorkflowTemplateStage templateStage : templateStages) {
                if (findStageByOrder(existingStages, templateStage.getStageOrder()) != null) {
                    continue;
                }
                workflowStageMapper.insert(buildPendingStage(taskId, templateStage));
            }
        } catch (DuplicateKeyException ex) {
            List<WorkflowStage> stagesAfterConflict = selectTaskStages(taskId);
            validateStageKeys(taskId, templateId, stagesAfterConflict, templateStages);
            if (hasAllTemplateStages(stagesAfterConflict, templateStages)) {
                return buildResponse(taskId, templateId, stagesAfterConflict);
            }
            throw new BusinessException(ErrorCode.INVALID_PARAM,
                    "Workflow stages initialization conflict for task " + taskId + "; retry after existing stages are completed");
        }

        List<WorkflowStage> initializedStages = selectTaskStages(taskId);
        validateStageKeys(taskId, templateId, initializedStages, templateStages);
        if (!hasAllTemplateStages(initializedStages, templateStages)) {
            throw new BusinessException(ErrorCode.INVALID_PARAM,
                    "Workflow stages initialization incomplete for task " + taskId);
        }
        return buildResponse(taskId, templateId, initializedStages);
    }

    private List<WorkflowStage> selectTaskStages(Long taskId) {
        return workflowStageMapper.selectList(new LambdaQueryWrapper<WorkflowStage>()
                .eq(WorkflowStage::getTaskId, taskId)
                .orderByAsc(WorkflowStage::getStageOrder));
    }

    private WorkflowStage buildPendingStage(Long taskId, WorkflowTemplateStage templateStage) {
        WorkflowStage stage = new WorkflowStage();
        stage.setTaskId(taskId);
        stage.setTemplateStageId(templateStage.getId());
        stage.setStageKey(templateStage.getStageKey());
        stage.setStageName(templateStage.getStageName());
        stage.setStageOrder(templateStage.getStageOrder());
        stage.setStatus(StageStatus.PENDING);
        stage.setStartedAt(null);
        stage.setCompletedAt(null);
        stage.setDeleted(0);
        return stage;
    }

    private void validateStageKeys(Long taskId, Long templateId, List<WorkflowStage> existingStages,
                                   List<WorkflowTemplateStage> templateStages) {
        for (WorkflowTemplateStage templateStage : templateStages) {
            WorkflowStage existingStage = findStageByOrder(existingStages, templateStage.getStageOrder());
            if (existingStage != null && !templateStage.getStageKey().equals(existingStage.getStageKey())) {
                throw new BusinessException(ErrorCode.INVALID_PARAM,
                        "Workflow stage config/data conflict for task " + taskId + ", template " + templateId
                                + ": stage order " + templateStage.getStageOrder() + " expected key "
                                + templateStage.getStageKey() + " but found " + existingStage.getStageKey());
            }
        }
    }

    private boolean hasAllTemplateStages(List<WorkflowStage> existingStages, List<WorkflowTemplateStage> templateStages) {
        for (WorkflowTemplateStage templateStage : templateStages) {
            if (findStageByOrder(existingStages, templateStage.getStageOrder()) == null) {
                return false;
            }
        }
        return true;
    }

    private WorkflowStage findStageByOrder(List<WorkflowStage> stages, Integer stageOrder) {
        for (WorkflowStage stage : stages) {
            if (stageOrder != null && stageOrder.equals(stage.getStageOrder())) {
                return stage;
            }
        }
        return null;
    }

    private StageInitResponse buildResponse(Long taskId, Long templateId, List<WorkflowStage> stages) {
        StageInitResponse response = new StageInitResponse();
        response.setTaskId(taskId);
        response.setMatchedTemplateId(templateId);
        response.setStages(stages.stream()
                .sorted((left, right) -> left.getStageOrder().compareTo(right.getStageOrder()))
                .map(WorkflowStageResponse::from)
                .collect(Collectors.toList()));
        return response;
    }
}
