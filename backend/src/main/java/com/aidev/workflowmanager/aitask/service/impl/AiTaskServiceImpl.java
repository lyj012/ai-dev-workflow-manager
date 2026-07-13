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
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;

@Service
public class AiTaskServiceImpl implements AiTaskService {

    private static final Logger log = LoggerFactory.getLogger(AiTaskServiceImpl.class);
    private static final long MOCK_ANALYSIS_DELAY_MILLIS = 3000L;
    private static final long STALE_CREATED_SECONDS = 30L;
    private static final long STALE_ANALYZING_MINUTES = 5L;
    private static final int RECOVERY_BATCH_SIZE = 20;
    private static final String DISPATCH_REJECTED_MESSAGE = "AI 任务执行队列已满，请稍后重试。";
    private static final String EXECUTION_TIMEOUT_MESSAGE = "AI 任务执行超时，请重试。";

    private final AiTaskMapper aiTaskMapper;
    private final AiTaskLogMapper aiTaskLogMapper;
    private final AiTaskResultMapper aiTaskResultMapper;
    private final TaskExecutor aiTaskExecutor;
    private final TransactionOperations transactionOperations;

    @Autowired
    public AiTaskServiceImpl(AiTaskMapper aiTaskMapper,
                             AiTaskLogMapper aiTaskLogMapper,
                             AiTaskResultMapper aiTaskResultMapper,
                             @Qualifier("aiTaskExecutor") TaskExecutor aiTaskExecutor,
                             PlatformTransactionManager transactionManager) {
        this(aiTaskMapper, aiTaskLogMapper, aiTaskResultMapper, aiTaskExecutor, new TransactionTemplate(transactionManager));
    }

    public AiTaskServiceImpl(AiTaskMapper aiTaskMapper,
                             AiTaskLogMapper aiTaskLogMapper,
                             AiTaskResultMapper aiTaskResultMapper,
                             TaskExecutor aiTaskExecutor,
                             TransactionOperations transactionOperations) {
        this.aiTaskMapper = aiTaskMapper;
        this.aiTaskLogMapper = aiTaskLogMapper;
        this.aiTaskResultMapper = aiTaskResultMapper;
        this.aiTaskExecutor = aiTaskExecutor;
        this.transactionOperations = transactionOperations;
    }

    @Override
    public AiTaskResponse create(CreateAiTaskRequest request) {
        AiTask task = transactionOperations.execute(status -> {
            AiTask newTask = new AiTask();
            newTask.setTitle(normalizeRequired(request.getTitle(), "任务标题不能为空。"));
            newTask.setRequirement(normalizeRequired(request.getRequirement(), "需求描述不能为空。"));
            newTask.setTaskType(request.getTaskType());
            newTask.setRemark(trimToNull(request.getRemark()));
            newTask.setStatus(AiTaskStatus.CREATED);
            newTask.setRetryCount(0);
            LocalDateTime now = LocalDateTime.now();
            newTask.setCreatedAt(now);
            newTask.setUpdatedAt(now);
            newTask.setDeleted(0);
            aiTaskMapper.insert(newTask);
            appendLog(newTask.getId(), AiTaskLogLevel.INFO, "TASK_CREATED", "任务已创建。");
            return newTask;
        });
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
        boolean updated = Boolean.TRUE.equals(transactionOperations.execute(status -> {
            int rows = aiTaskMapper.update(null, new LambdaUpdateWrapper<AiTask>()
                    .eq(AiTask::getId, taskId)
                    .eq(AiTask::getStatus, AiTaskStatus.FAILED)
                    .set(AiTask::getStatus, AiTaskStatus.CREATED)
                    .set(AiTask::getRetryCount, retryCount)
                    .set(AiTask::getErrorMessage, null)
                    .set(AiTask::getUpdatedAt, LocalDateTime.now()));
            if (rows <= 0) {
                return false;
            }
            appendLog(taskId, AiTaskLogLevel.INFO, "TASK_RETRY", "用户触发失败任务重试。");
            return true;
        }));
        if (!updated) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "任务状态已变化，请刷新后重试。");
        }
        log.info("[AI_TASK] retry submitted taskId={} retryCount={}", taskId, retryCount);
        submitExecution(taskId);
        return detail(taskId);
    }

    @Scheduled(fixedDelay = 60000L, initialDelay = 60000L)
    public void recoverStaleTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<AiTask> staleCreatedTasks = aiTaskMapper.selectList(new LambdaQueryWrapper<AiTask>()
                .eq(AiTask::getStatus, AiTaskStatus.CREATED)
                .lt(AiTask::getUpdatedAt, now.minusSeconds(STALE_CREATED_SECONDS))
                .orderByAsc(AiTask::getUpdatedAt)
                .last("LIMIT " + RECOVERY_BATCH_SIZE));
        for (AiTask task : staleCreatedTasks) {
            log.warn("[AI_TASK] recovering stale created taskId={}", task.getId());
            submitExecution(task.getId());
        }

        List<AiTask> staleAnalyzingTasks = aiTaskMapper.selectList(new LambdaQueryWrapper<AiTask>()
                .eq(AiTask::getStatus, AiTaskStatus.ANALYZING)
                .lt(AiTask::getUpdatedAt, now.minusMinutes(STALE_ANALYZING_MINUTES))
                .orderByAsc(AiTask::getUpdatedAt)
                .last("LIMIT " + RECOVERY_BATCH_SIZE));
        for (AiTask task : staleAnalyzingTasks) {
            markFailed(task.getId(), EXECUTION_TIMEOUT_MESSAGE);
        }
    }

    private void submitExecution(Long taskId) {
        try {
            aiTaskExecutor.execute(() -> runMockAnalysis(taskId));
        } catch (RejectedExecutionException ex) {
            markDispatchRejected(taskId);
            log.warn("[AI_TASK] execution rejected taskId={}", taskId, ex);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, DISPATCH_REJECTED_MESSAGE);
        }
    }

    private void runMockAnalysis(Long taskId) {
        try {
            if (!markAnalyzing(taskId)) {
                log.info("[AI_TASK] execution skipped taskId={} reason=status-not-created", taskId);
                return;
            }
            Thread.sleep(MOCK_ANALYSIS_DELAY_MILLIS);

            AiTask task = requireTask(taskId);
            if (shouldMockFail(task)) {
                throw new IllegalStateException("模拟分析失败：需求描述触发失败场景。");
            }
            if (markSuccess(task)) {
                log.info("[AI_TASK] execution success taskId={}", taskId);
            } else {
                log.warn("[AI_TASK] success skipped taskId={} reason=status-not-analyzing", taskId);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            markFailed(taskId, "模拟分析被中断。");
        } catch (Exception ex) {
            markFailed(taskId, ex.getMessage());
        }
    }

    private boolean markAnalyzing(Long taskId) {
        return Boolean.TRUE.equals(transactionOperations.execute(status -> {
            boolean started = aiTaskMapper.update(null, new LambdaUpdateWrapper<AiTask>()
                    .eq(AiTask::getId, taskId)
                    .eq(AiTask::getStatus, AiTaskStatus.CREATED)
                    .set(AiTask::getStatus, AiTaskStatus.ANALYZING)
                    .set(AiTask::getErrorMessage, null)
                    .set(AiTask::getUpdatedAt, LocalDateTime.now())) > 0;
            if (!started) {
                return false;
            }
            appendLog(taskId, AiTaskLogLevel.INFO, "TASK_ANALYZING", "开始模拟 AI 分析。");
            return true;
        }));
    }

    private boolean markSuccess(AiTask task) {
        return Boolean.TRUE.equals(transactionOperations.execute(status -> {
            int rows = aiTaskMapper.update(null, new LambdaUpdateWrapper<AiTask>()
                    .eq(AiTask::getId, task.getId())
                    .eq(AiTask::getStatus, AiTaskStatus.ANALYZING)
                    .set(AiTask::getStatus, AiTaskStatus.SUCCESS)
                    .set(AiTask::getErrorMessage, null)
                    .set(AiTask::getUpdatedAt, LocalDateTime.now()));
            if (rows <= 0) {
                return false;
            }
            saveResult(task);
            appendLog(task.getId(), AiTaskLogLevel.INFO, "TASK_SUCCESS", "模拟分析完成。");
            return true;
        }));
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
        boolean updated = Boolean.TRUE.equals(transactionOperations.execute(status -> {
            int rows = aiTaskMapper.update(null, new LambdaUpdateWrapper<AiTask>()
                    .eq(AiTask::getId, taskId)
                    .eq(AiTask::getStatus, AiTaskStatus.ANALYZING)
                    .set(AiTask::getStatus, AiTaskStatus.FAILED)
                    .set(AiTask::getErrorMessage, errorMessage)
                    .set(AiTask::getUpdatedAt, LocalDateTime.now()));
            if (rows <= 0) {
                return false;
            }
            appendLog(taskId, AiTaskLogLevel.ERROR, "TASK_FAILED", errorMessage);
            return true;
        }));
        if (updated) {
            log.warn("[AI_TASK] execution failed taskId={} message={}", taskId, errorMessage);
        } else {
            log.warn("[AI_TASK] failure skipped taskId={} reason=status-not-analyzing message={}", taskId, errorMessage);
        }
    }

    private void markDispatchRejected(Long taskId) {
        transactionOperations.executeWithoutResult(status -> {
            int rows = aiTaskMapper.update(null, new LambdaUpdateWrapper<AiTask>()
                    .eq(AiTask::getId, taskId)
                    .eq(AiTask::getStatus, AiTaskStatus.CREATED)
                    .set(AiTask::getStatus, AiTaskStatus.FAILED)
                    .set(AiTask::getErrorMessage, DISPATCH_REJECTED_MESSAGE)
                    .set(AiTask::getUpdatedAt, LocalDateTime.now()));
            if (rows > 0) {
                appendLog(taskId, AiTaskLogLevel.ERROR, "TASK_FAILED", DISPATCH_REJECTED_MESSAGE);
            }
        });
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
