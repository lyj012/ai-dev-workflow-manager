package com.aidev.workflowmanager.task.service.impl;

import com.aidev.workflowmanager.common.enums.RiskTag;
import com.aidev.workflowmanager.common.enums.TaskStatus;
import com.aidev.workflowmanager.common.exception.BusinessException;
import com.aidev.workflowmanager.common.exception.ErrorCode;
import com.aidev.workflowmanager.common.page.PageResult;
import com.aidev.workflowmanager.stage.entity.WorkflowStage;
import com.aidev.workflowmanager.stage.mapper.WorkflowStageMapper;
import com.aidev.workflowmanager.stage.vo.WorkflowStageResponse;
import com.aidev.workflowmanager.task.dto.CreateTaskRequest;
import com.aidev.workflowmanager.task.dto.TaskPageQuery;
import com.aidev.workflowmanager.task.entity.WorkflowTask;
import com.aidev.workflowmanager.task.mapper.WorkflowTaskMapper;
import com.aidev.workflowmanager.task.service.WorkflowTaskService;
import com.aidev.workflowmanager.task.vo.TaskDetailResponse;
import com.aidev.workflowmanager.task.vo.TaskResponse;
import com.aidev.workflowmanager.template.entity.WorkflowTemplate;
import com.aidev.workflowmanager.template.mapper.WorkflowTemplateMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkflowTaskServiceImpl implements WorkflowTaskService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowTaskServiceImpl.class);

    private final WorkflowTaskMapper workflowTaskMapper;
    private final WorkflowTemplateMapper workflowTemplateMapper;
    private final WorkflowStageMapper workflowStageMapper;

    public WorkflowTaskServiceImpl(WorkflowTaskMapper workflowTaskMapper,
                                   WorkflowTemplateMapper workflowTemplateMapper,
                                   WorkflowStageMapper workflowStageMapper) {
        this.workflowTaskMapper = workflowTaskMapper;
        this.workflowTemplateMapper = workflowTemplateMapper;
        this.workflowStageMapper = workflowStageMapper;
    }

    @Override
    public TaskResponse create(CreateTaskRequest request) {
        validateTitle(request.getTitle());

        WorkflowTask task = new WorkflowTask();
        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        task.setTaskType(request.getTaskType());
        task.setComplexity(request.getComplexity());
        task.setRiskTags(normalizeRiskTags(request.getRiskTags()));
        task.setStatus(TaskStatus.DRAFT);
        task.setTestChecklistGenerated(false);
        task.setDeleted(0);

        workflowTaskMapper.insert(task);
        log.info("[TASK] created taskId={} title={} taskType={} complexity={} riskTags={}",
                task.getId(), task.getTitle(), task.getTaskType(), task.getComplexity(), task.getRiskTags());
        return TaskResponse.from(task);
    }

    @Override
    public PageResult<TaskResponse> page(TaskPageQuery query) {
        long pageNo = query.normalizedPageNo();
        long pageSize = query.normalizedPageSize();

        LambdaQueryWrapper<WorkflowTask> wrapper = buildQueryWrapper(query);
        Page<WorkflowTask> page = workflowTaskMapper.selectPage(new Page<WorkflowTask>(pageNo, pageSize), wrapper);
        log.info("[TASK] page queried pageNo={} pageSize={} total={} status={} taskType={} complexity={} riskTags={}",
                pageNo, pageSize, page.getTotal(), query.getStatus(), query.getTaskType(), query.getComplexity(), query.getRiskTags());
        List<TaskResponse> records = page.getRecords().stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
        return new PageResult<TaskResponse>(records, page.getTotal(), pageNo, pageSize);
    }

    @Override
    public TaskDetailResponse detail(Long taskId) {
        if (taskId == null || taskId < 1) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "taskId must be greater than or equal to 1");
        }
        WorkflowTask task = workflowTaskMapper.selectOne(new LambdaQueryWrapper<WorkflowTask>()
                .eq(WorkflowTask::getId, taskId));
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Task not found: " + taskId);
        }
        log.info("[TASK] detail requested taskId={} status={} matchedTemplateId={} deliveryRecordId={}",
                task.getId(), task.getStatus(), task.getMatchedTemplateId(), task.getDeliveryRecordId());
        TaskDetailResponse response = TaskDetailResponse.from(task);
        if (task.getMatchedTemplateId() != null) {
            WorkflowTemplate template = workflowTemplateMapper.selectOne(new LambdaQueryWrapper<WorkflowTemplate>()
                    .eq(WorkflowTemplate::getId, task.getMatchedTemplateId()));
            if (template != null) {
                response.setMatchedTemplateName(template.getName());
            }
        }
        List<WorkflowStage> stages = workflowStageMapper.selectList(new LambdaQueryWrapper<WorkflowStage>()
                .eq(WorkflowStage::getTaskId, taskId)
                .orderByAsc(WorkflowStage::getStageOrder));
        response.setStages(stages.stream().map(WorkflowStageResponse::from).collect(Collectors.toList()));
        log.info("[TASK] detail loaded taskId={} matchedTemplateName={} stageCount={}",
                task.getId(), response.getMatchedTemplateName(), response.getStages().size());
        return response;
    }

    private LambdaQueryWrapper<WorkflowTask> buildQueryWrapper(TaskPageQuery query) {
        LambdaQueryWrapper<WorkflowTask> wrapper = new LambdaQueryWrapper<WorkflowTask>()
                .orderByDesc(WorkflowTask::getCreatedAt)
                .orderByDesc(WorkflowTask::getId);
        if (query.getStatus() != null) {
            wrapper.eq(WorkflowTask::getStatus, query.getStatus());
        }
        if (query.getTaskType() != null) {
            wrapper.eq(WorkflowTask::getTaskType, query.getTaskType());
        }
        if (query.getComplexity() != null) {
            wrapper.eq(WorkflowTask::getComplexity, query.getComplexity());
        }
        if (query.getRiskTags() != null) {
            for (RiskTag riskTag : query.getRiskTags()) {
                if (riskTag != null) {
                    wrapper.apply("JSON_CONTAINS(risk_tags, JSON_QUOTE({0}))", riskTag.getCode());
                }
            }
        }
        return wrapper;
    }

    private List<RiskTag> normalizeRiskTags(List<RiskTag> riskTags) {
        if (riskTags == null) {
            return new ArrayList<RiskTag>();
        }
        return riskTags.stream()
                .filter(tag -> tag != null)
                .distinct()
                .collect(Collectors.toList());
    }

    private void validateTitle(String title) {
        if (!StringUtils.hasText(title)) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "title must not be blank");
        }
        if (title.trim().length() > 200) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "title length must be less than or equal to 200");
        }
    }
}
