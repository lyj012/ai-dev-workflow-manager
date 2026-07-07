package com.aidev.workflowmanager.stage.service.impl;

import com.aidev.workflowmanager.common.enums.StageStatus;
import com.aidev.workflowmanager.common.enums.TaskStatus;
import com.aidev.workflowmanager.common.exception.BusinessException;
import com.aidev.workflowmanager.common.exception.ErrorCode;
import com.aidev.workflowmanager.stage.dto.StageOutputRequest;
import com.aidev.workflowmanager.stage.entity.WorkflowStage;
import com.aidev.workflowmanager.stage.mapper.WorkflowStageMapper;
import com.aidev.workflowmanager.stage.service.WorkflowStageService;
import com.aidev.workflowmanager.stage.vo.StageInitResponse;
import com.aidev.workflowmanager.stage.vo.StageOutputParts;
import com.aidev.workflowmanager.stage.vo.WorkflowStageResponse;
import com.aidev.workflowmanager.task.entity.WorkflowTask;
import com.aidev.workflowmanager.task.mapper.WorkflowTaskMapper;
import com.aidev.workflowmanager.template.entity.WorkflowTemplate;
import com.aidev.workflowmanager.template.entity.WorkflowTemplateStage;
import com.aidev.workflowmanager.template.mapper.WorkflowTemplateMapper;
import com.aidev.workflowmanager.template.mapper.WorkflowTemplateStageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.dao.DuplicateKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class WorkflowStageServiceImpl implements WorkflowStageService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowStageServiceImpl.class);

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
        log.info("[STAGE] init requested taskId={} matchedTemplateId={} taskStatus={}",
                taskId, templateId, task.getStatus());
        WorkflowTemplate template = workflowTemplateMapper.selectOne(new LambdaQueryWrapper<WorkflowTemplate>()
                .eq(WorkflowTemplate::getId, templateId)
                .eq(WorkflowTemplate::getEnabled, true));
        if (template == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Workflow template not found or disabled: " + templateId);
        }

        Object lock = taskInitLocks.computeIfAbsent(taskId, key -> new Object());
        synchronized (lock) {
            StageInitResponse response = initializeStagesWithLock(taskId, templateId);
            advanceTaskToExecuting(task);
            return response;
        }
    }

    private void advanceTaskToExecuting(WorkflowTask task) {
        if (TaskStatus.DRAFT.equals(task.getStatus()) || TaskStatus.ANALYZING.equals(task.getStatus())) {
            task.setStatus(TaskStatus.EXECUTING);
            workflowTaskMapper.updateById(task);
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
            log.info("[STAGE] init skipped existing taskId={} templateId={} stageCount={}",
                    taskId, templateId, existingStages.size());
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
                log.info("[STAGE] init completed after duplicate conflict taskId={} templateId={} stageCount={}",
                        taskId, templateId, stagesAfterConflict.size());
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
        log.info("[STAGE] init completed taskId={} templateId={} stageCount={}",
                taskId, templateId, initializedStages.size());
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
        stage.setInputSummary(templateStage.getDescription());
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

    @Override
    @Transactional
    public WorkflowStageResponse startStage(Long taskId, Long stageId) {
        WorkflowTask task = loadMutableTask(taskId, "stage start");
        WorkflowStage stage = loadTaskStage(task.getId(), stageId);
        requireStatus(stage, StageStatus.PENDING, "Only PENDING stage can be started");
        stage.setStatus(StageStatus.RUNNING);
        stage.setStartedAt(LocalDateTime.now());
        workflowStageMapper.updateById(stage);
        log.info("[STAGE] started taskId={} stageId={} stageKey={} status={}",
                taskId, stageId, stage.getStageKey(), stage.getStatus());
        return WorkflowStageResponse.from(stage);
    }

    @Override
    @Transactional
    public WorkflowStageResponse completeStage(Long taskId, Long stageId, StageOutputRequest request) {
        WorkflowTask task = loadMutableTask(taskId, "stage complete");
        WorkflowStage stage = loadTaskStage(task.getId(), stageId);
        requireStatus(stage, StageStatus.RUNNING, "Only RUNNING stage can be completed");
        if (request != null && hasAnyOutput(request)) {
            stage.setOutputSummary(toOutputParts(request).format());
        }
        stage.setStatus(StageStatus.COMPLETED);
        stage.setCompletedAt(LocalDateTime.now());
        workflowStageMapper.updateById(stage);
        log.info("[STAGE] completed taskId={} stageId={} stageKey={} hasOutput={}",
                taskId, stageId, stage.getStageKey(), StringUtils.hasText(stage.getOutputSummary()));
        return WorkflowStageResponse.from(stage);
    }

    @Override
    @Transactional
    public WorkflowStageResponse skipStage(Long taskId, Long stageId) {
        WorkflowTask task = loadMutableTask(taskId, "stage skip");
        WorkflowStage stage = loadTaskStage(task.getId(), stageId);
        requireStatus(stage, StageStatus.PENDING, "Only PENDING stage can be skipped");
        WorkflowTemplateStage templateStage = loadTemplateStage(stage);
        if (isHighRiskTask(task) && ("analysis".equals(stage.getStageKey()) || "risk_review".equals(stage.getStageKey()))) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "高风险任务不能跳过关键阶段：" + stageDisplayName(stage));
        }
        if (templateStage != null && Boolean.TRUE.equals(templateStage.getRequired())) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "必需阶段不能跳过：" + stageDisplayName(stage));
        }
        stage.setStatus(StageStatus.SKIPPED);
        stage.setCompletedAt(LocalDateTime.now());
        workflowStageMapper.updateById(stage);
        log.info("[STAGE] skipped taskId={} stageId={} stageKey={}", taskId, stageId, stage.getStageKey());
        return WorkflowStageResponse.from(stage);
    }

    @Override
    @Transactional
    public WorkflowStageResponse failStage(Long taskId, Long stageId, StageOutputRequest request) {
        WorkflowTask task = loadMutableTask(taskId, "stage fail");
        WorkflowStage stage = loadTaskStage(task.getId(), stageId);
        requireStatus(stage, StageStatus.RUNNING, "Only RUNNING stage can be marked as failed");
        if (request != null && hasAnyOutput(request)) {
            stage.setOutputSummary(toOutputParts(request).format());
        }
        stage.setStatus(StageStatus.FAILED);
        stage.setCompletedAt(LocalDateTime.now());
        workflowStageMapper.updateById(stage);
        log.info("[STAGE] failed taskId={} stageId={} stageKey={} hasOutput={}",
                taskId, stageId, stage.getStageKey(), StringUtils.hasText(stage.getOutputSummary()));
        return WorkflowStageResponse.from(stage);
    }

    @Override
    @Transactional
    public StageOutputRequest saveOutput(Long taskId, Long stageId, StageOutputRequest request) {
        WorkflowTask task = loadMutableTask(taskId, "stage output update");
        WorkflowStage stage = loadTaskStage(task.getId(), stageId);
        if (request == null || !hasAnyOutput(request)) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "Stage output must not be blank");
        }
        StageOutputParts outputParts = toOutputParts(request);
        stage.setOutputSummary(outputParts.format());
        workflowStageMapper.updateById(stage);
        log.info("[STAGE] output saved taskId={} stageId={} stageKey={} outputLength={} riskLength={} nextLength={} unverifiedLength={}",
                taskId, stageId, stage.getStageKey(), length(outputParts.getOutputSummary()),
                length(outputParts.getRiskPoints()), length(outputParts.getNextActions()),
                length(outputParts.getUnverifiedScope()));
        return toOutputRequest(outputParts);
    }

    @Override
    public StageOutputRequest getOutput(Long taskId, Long stageId) {
        WorkflowTask task = loadExistingTask(taskId);
        WorkflowStage stage = loadTaskStage(task.getId(), stageId);
        log.info("[STAGE] output fetched taskId={} stageId={} stageKey={} hasOutput={}",
                taskId, stageId, stage.getStageKey(), StringUtils.hasText(stage.getOutputSummary()));
        return toOutputRequest(StageOutputParts.parse(stage.getOutputSummary()));
    }

    private WorkflowTask loadMutableTask(Long taskId, String action) {
        WorkflowTask task = loadExistingTask(taskId);
        if (TaskStatus.ARCHIVED.equals(task.getStatus()) || TaskStatus.CANCELED.equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PARAM,
                    "Task status does not allow " + action + ": " + task.getStatus());
        }
        return task;
    }

    private WorkflowTask loadExistingTask(Long taskId) {
        if (taskId == null || taskId < 1) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "taskId must be greater than or equal to 1");
        }
        WorkflowTask task = workflowTaskMapper.selectOne(new LambdaQueryWrapper<WorkflowTask>()
                .eq(WorkflowTask::getId, taskId));
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Task not found: " + taskId);
        }
        return task;
    }

    private WorkflowStage loadTaskStage(Long taskId, Long stageId) {
        if (stageId == null || stageId < 1) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "stageId must be greater than or equal to 1");
        }
        WorkflowStage stage = workflowStageMapper.selectOne(new LambdaQueryWrapper<WorkflowStage>()
                .eq(WorkflowStage::getId, stageId)
                .eq(WorkflowStage::getTaskId, taskId));
        if (stage == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Workflow stage not found: " + stageId);
        }
        return stage;
    }

    private WorkflowTemplateStage loadTemplateStage(WorkflowStage stage) {
        if (stage.getTemplateStageId() == null) {
            return null;
        }
        return workflowTemplateStageMapper.selectOne(new LambdaQueryWrapper<WorkflowTemplateStage>()
                .eq(WorkflowTemplateStage::getId, stage.getTemplateStageId()));
    }

    private void requireStatus(WorkflowStage stage, StageStatus expectedStatus, String message) {
        if (!expectedStatus.equals(stage.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, message + ", current status: " + stage.getStatus());
        }
    }

    private boolean isHighRiskTask(WorkflowTask task) {
        return com.aidev.workflowmanager.common.enums.Complexity.COMPLEX.equals(task.getComplexity())
                || (task.getRiskTags() != null && !task.getRiskTags().isEmpty());
    }

    private boolean hasAnyOutput(StageOutputRequest request) {
        return request != null
                && (StringUtils.hasText(request.getOutputSummary())
                || StringUtils.hasText(request.getRiskPoints())
                || StringUtils.hasText(request.getNextActions())
                || StringUtils.hasText(request.getUnverifiedScope()));
    }

    private StageOutputParts toOutputParts(StageOutputRequest request) {
        return StageOutputParts.of(request.getOutputSummary(), request.getRiskPoints(),
                request.getNextActions(), request.getUnverifiedScope());
    }

    private StageOutputRequest toOutputRequest(StageOutputParts parts) {
        StageOutputRequest response = new StageOutputRequest();
        response.setOutputSummary(parts.getOutputSummary());
        response.setRiskPoints(parts.getRiskPoints());
        response.setNextActions(parts.getNextActions());
        response.setUnverifiedScope(parts.getUnverifiedScope());
        return response;
    }

    private int length(String value) {
        return value == null ? 0 : value.length();
    }

    private String stageDisplayName(WorkflowStage stage) {
        return StringUtils.hasText(stage.getStageName()) ? stage.getStageName() : stage.getStageKey();
    }
}
