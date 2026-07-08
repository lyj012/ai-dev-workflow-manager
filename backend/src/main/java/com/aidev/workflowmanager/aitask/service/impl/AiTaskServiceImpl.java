package com.aidev.workflowmanager.aitask.service.impl;

import com.aidev.workflowmanager.aitask.domain.AiTaskLogLevel;
import com.aidev.workflowmanager.aitask.domain.AiTaskStatus;
import com.aidev.workflowmanager.aitask.dto.AiTaskPageQuery;
import com.aidev.workflowmanager.aitask.dto.CreateAiTaskRequest;
import com.aidev.workflowmanager.aitask.entity.AiTask;
import com.aidev.workflowmanager.aitask.entity.AiTaskLog;
import com.aidev.workflowmanager.aitask.entity.AiTaskResult;
import com.aidev.workflowmanager.aitask.mapper.AiTaskLogMapper;
import com.aidev.workflowmanager.aitask.mapper.AiTaskMapper;
import com.aidev.workflowmanager.aitask.mapper.AiTaskResultMapper;
import com.aidev.workflowmanager.aitask.service.AiTaskService;
import com.aidev.workflowmanager.aitask.vo.AiTaskLogResponse;
import com.aidev.workflowmanager.aitask.vo.AiTaskResponse;
import com.aidev.workflowmanager.aitask.vo.AiTaskResultResponse;
import com.aidev.workflowmanager.common.exception.BusinessException;
import com.aidev.workflowmanager.common.exception.ErrorCode;
import com.aidev.workflowmanager.common.page.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiTaskServiceImpl implements AiTaskService {

    private static final Logger log = LoggerFactory.getLogger(AiTaskServiceImpl.class);
    private static final long MOCK_ANALYSIS_DELAY_MILLIS = 3000L;

    private final AiTaskMapper aiTaskMapper;
    private final AiTaskLogMapper aiTaskLogMapper;
    private final AiTaskResultMapper aiTaskResultMapper;
    private final TaskExecutor aiTaskExecutor;

    public AiTaskServiceImpl(AiTaskMapper aiTaskMapper,
                             AiTaskLogMapper aiTaskLogMapper,
                             AiTaskResultMapper aiTaskResultMapper,
                             @Qualifier("aiTaskExecutor") TaskExecutor aiTaskExecutor) {
        this.aiTaskMapper = aiTaskMapper;
        this.aiTaskLogMapper = aiTaskLogMapper;
        this.aiTaskResultMapper = aiTaskResultMapper;
        this.aiTaskExecutor = aiTaskExecutor;
    }

    @Override
    public AiTaskResponse create(CreateAiTaskRequest request) {
        AiTask task = new AiTask();
        task.setTitle(normalizeRequired(request.getTitle(), "任务标题不能为空。"));
        task.setRequirement(normalizeRequired(request.getRequirement(), "需求描述不能为空。"));
        task.setTaskType(request.getTaskType());
        task.setRemark(trimToNull(request.getRemark()));
        task.setStatus(AiTaskStatus.CREATED);
        task.setRetryCount(0);
        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        task.setDeleted(0);
        aiTaskMapper.insert(task);
        appendLog(task.getId(), AiTaskLogLevel.INFO, "TASK_CREATED", "任务已创建。");
        log.info("[AI_TASK] created taskId={} title={} type={}", task.getId(), task.getTitle(), task.getTaskType());
        submitExecution(task.getId());
        return AiTaskResponse.from(task);
    }

    @Override
    public PageResult<AiTaskResponse> page(AiTaskPageQuery query) {
        long pageNo = query.normalizedPageNo();
        long pageSize = query.normalizedPageSize();
        LambdaQueryWrapper<AiTask> wrapper = new LambdaQueryWrapper<AiTask>()
                .orderByDesc(AiTask::getCreatedAt)
                .orderByDesc(AiTask::getId);
        if (query.getStatus() != null) {
            wrapper.eq(AiTask::getStatus, query.getStatus());
        }
        Page<AiTask> page = aiTaskMapper.selectPage(new Page<AiTask>(pageNo, pageSize), wrapper);
        List<AiTaskResponse> records = page.getRecords().stream()
                .map(AiTaskResponse::from)
                .collect(Collectors.toList());
        return new PageResult<AiTaskResponse>(records, page.getTotal(), pageNo, pageSize);
    }

    @Override
    public AiTaskResponse detail(Long taskId) {
        return AiTaskResponse.from(requireTask(taskId));
    }

    @Override
    public List<AiTaskLogResponse> logs(Long taskId) {
        requireTask(taskId);
        return aiTaskLogMapper.selectList(new LambdaQueryWrapper<AiTaskLog>()
                        .eq(AiTaskLog::getTaskId, taskId)
                        .orderByAsc(AiTaskLog::getCreatedAt)
                        .orderByAsc(AiTaskLog::getId))
                .stream()
                .map(AiTaskLogResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public AiTaskResultResponse result(Long taskId) {
        requireTask(taskId);
        AiTaskResult result = aiTaskResultMapper.selectOne(new LambdaQueryWrapper<AiTaskResult>()
                .eq(AiTaskResult::getTaskId, taskId));
        return AiTaskResultResponse.from(result);
    }

    @Override
    public AiTaskResponse retry(Long taskId) {
        AiTask task = requireTask(taskId);
        if (task.getStatus() != AiTaskStatus.FAILED) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "只有执行失败的 AI 任务可以重试。");
        }
        int retryCount = task.getRetryCount() == null ? 1 : task.getRetryCount() + 1;
        aiTaskMapper.update(null, new LambdaUpdateWrapper<AiTask>()
                .eq(AiTask::getId, taskId)
                .eq(AiTask::getStatus, AiTaskStatus.FAILED)
                .set(AiTask::getStatus, AiTaskStatus.CREATED)
                .set(AiTask::getRetryCount, retryCount)
                .set(AiTask::getErrorMessage, null));
        appendLog(taskId, AiTaskLogLevel.INFO, "TASK_RETRY", "用户触发失败任务重试。");
        log.info("[AI_TASK] retry submitted taskId={} retryCount={}", taskId, retryCount);
        submitExecution(taskId);
        return detail(taskId);
    }

    private void submitExecution(Long taskId) {
        aiTaskExecutor.execute(() -> runMockAnalysis(taskId));
    }

    private void runMockAnalysis(Long taskId) {
        try {
            boolean started = aiTaskMapper.update(null, new LambdaUpdateWrapper<AiTask>()
                    .eq(AiTask::getId, taskId)
                    .eq(AiTask::getStatus, AiTaskStatus.CREATED)
                    .set(AiTask::getStatus, AiTaskStatus.ANALYZING)
                    .set(AiTask::getErrorMessage, null)) > 0;
            if (!started) {
                log.info("[AI_TASK] execution skipped taskId={} reason=status-not-created", taskId);
                return;
            }
            appendLog(taskId, AiTaskLogLevel.INFO, "TASK_ANALYZING", "开始模拟 AI 分析。");
            Thread.sleep(MOCK_ANALYSIS_DELAY_MILLIS);

            AiTask task = requireTask(taskId);
            if (shouldMockFail(task)) {
                throw new IllegalStateException("模拟分析失败：需求描述触发失败场景。");
            }
            saveResult(task);
            aiTaskMapper.update(null, new LambdaUpdateWrapper<AiTask>()
                    .eq(AiTask::getId, taskId)
                    .eq(AiTask::getStatus, AiTaskStatus.ANALYZING)
                    .set(AiTask::getStatus, AiTaskStatus.SUCCESS)
                    .set(AiTask::getErrorMessage, null));
            appendLog(taskId, AiTaskLogLevel.INFO, "TASK_SUCCESS", "模拟分析完成。");
            log.info("[AI_TASK] execution success taskId={}", taskId);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            markFailed(taskId, "模拟分析被中断。");
        } catch (Exception ex) {
            markFailed(taskId, ex.getMessage());
        }
    }

    private void saveResult(AiTask task) {
        AiTaskResult existing = aiTaskResultMapper.selectOne(new LambdaQueryWrapper<AiTaskResult>()
                .eq(AiTaskResult::getTaskId, task.getId()));
        AiTaskResult result = existing == null ? new AiTaskResult() : existing;
        result.setTaskId(task.getId());
        result.setSummary("已完成对「" + task.getTitle() + "」的模拟分析，建议先确认需求边界，再拆分执行和验证步骤。");
        result.setRiskPoints("当前为 mock 分析结果，真实风险需要后续接入 AI 或人工补充。");
        result.setSuggestedSteps("1. 明确输入需求；\n2. 拆分执行步骤；\n3. 记录关键日志；\n4. 输出验证建议。");
        result.setTestSuggestions("建议覆盖任务创建、状态刷新、日志展示、结果展示和失败重试。");
        result.setRawResult("mock ai result");
        if (existing == null) {
            aiTaskResultMapper.insert(result);
        } else {
            aiTaskResultMapper.updateById(result);
        }
    }

    private void markFailed(Long taskId, String message) {
        String errorMessage = StringUtils.hasText(message) ? message : "模拟分析失败。";
        aiTaskMapper.update(null, new LambdaUpdateWrapper<AiTask>()
                .eq(AiTask::getId, taskId)
                .eq(AiTask::getStatus, AiTaskStatus.ANALYZING)
                .set(AiTask::getStatus, AiTaskStatus.FAILED)
                .set(AiTask::getErrorMessage, errorMessage));
        appendLog(taskId, AiTaskLogLevel.ERROR, "TASK_FAILED", errorMessage);
        log.warn("[AI_TASK] execution failed taskId={} message={}", taskId, errorMessage);
    }

    private boolean shouldMockFail(AiTask task) {
        String text = (task.getTitle() + " " + task.getRequirement()).toLowerCase();
        return text.contains("mock_fail") || text.contains("模拟失败");
    }

    private AiTask requireTask(Long taskId) {
        if (taskId == null || taskId < 1) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "AI 任务 ID 必须大于等于 1。");
        }
        AiTask task = aiTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "AI 任务不存在：" + taskId);
        }
        return task;
    }

    private void appendLog(Long taskId, AiTaskLogLevel level, String node, String message) {
        AiTaskLog taskLog = new AiTaskLog();
        taskLog.setTaskId(taskId);
        taskLog.setLogLevel(level);
        taskLog.setLogNode(node);
        taskLog.setMessage(message);
        taskLog.setCreatedAt(LocalDateTime.now());
        aiTaskLogMapper.insert(taskLog);
    }

    private String normalizeRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, message);
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
