package com.aidev.workflowmanager.aitask.service;

import com.aidev.workflowmanager.aitask.domain.AiTaskLogLevel;
import com.aidev.workflowmanager.aitask.domain.AiTaskStatus;
import com.aidev.workflowmanager.aitask.domain.AiTaskType;
import com.aidev.workflowmanager.aitask.dto.CreateAiTaskRequest;
import com.aidev.workflowmanager.aitask.entity.AiTask;
import com.aidev.workflowmanager.aitask.entity.AiTaskLog;
import com.aidev.workflowmanager.aitask.mapper.AiTaskLogMapper;
import com.aidev.workflowmanager.aitask.mapper.AiTaskMapper;
import com.aidev.workflowmanager.aitask.mapper.AiTaskResultMapper;
import com.aidev.workflowmanager.aitask.service.impl.AiTaskServiceImpl;
import com.aidev.workflowmanager.aitask.vo.AiTaskResponse;
import com.aidev.workflowmanager.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiTaskServiceImplTest {

    @Mock
    private AiTaskMapper aiTaskMapper;

    @Mock
    private AiTaskLogMapper aiTaskLogMapper;

    @Mock
    private AiTaskResultMapper aiTaskResultMapper;

    private AtomicReference<Runnable> scheduledTask;
    private AiTaskServiceImpl service;

    @BeforeAll
    static void initMybatisPlusTableInfo() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), AiTask.class);
    }

    @BeforeEach
    void setUp() {
        scheduledTask = new AtomicReference<Runnable>();
        service = new AiTaskServiceImpl(aiTaskMapper, aiTaskLogMapper, aiTaskResultMapper, scheduledTask::set,
                testTransactions());
    }

    @Test
    void createStoresCreatedTaskAndSchedulesExecution() {
        ArgumentCaptor<AiTask> taskCaptor = ArgumentCaptor.forClass(AiTask.class);
        ArgumentCaptor<AiTaskLog> logCaptor = ArgumentCaptor.forClass(AiTaskLog.class);
        when(aiTaskMapper.insert(taskCaptor.capture())).thenAnswer(invocation -> {
            AiTask task = invocation.getArgument(0);
            task.setId(12L);
            return 1;
        });
        when(aiTaskLogMapper.insert(logCaptor.capture())).thenReturn(1);

        AiTaskResponse response = service.create(validCreateRequest());

        assertThat(response.getId()).isEqualTo(12L);
        assertThat(response.getStatus()).isEqualTo(AiTaskStatus.CREATED);
        assertThat(taskCaptor.getValue().getTitle()).isEqualTo("分析异步任务");
        assertThat(taskCaptor.getValue().getStatus()).isEqualTo(AiTaskStatus.CREATED);
        assertThat(taskCaptor.getValue().getRetryCount()).isZero();
        assertThat(taskCaptor.getValue().getDeleted()).isZero();
        assertThat(logCaptor.getValue().getTaskId()).isEqualTo(12L);
        assertThat(logCaptor.getValue().getLogLevel()).isEqualTo(AiTaskLogLevel.INFO);
        assertThat(logCaptor.getValue().getLogNode()).isEqualTo("TASK_CREATED");
        assertThat(scheduledTask.get()).isNotNull();
    }

    @Test
    void retryRejectsNonFailedTask() {
        AiTask task = new AiTask();
        task.setId(5L);
        task.setStatus(AiTaskStatus.SUCCESS);
        when(aiTaskMapper.selectById(5L)).thenReturn(task);

        assertThatThrownBy(() -> service.retry(5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("只有执行失败");
    }

    @Test
    void retryRejectsConcurrentStateChange() {
        AiTask task = new AiTask();
        task.setId(5L);
        task.setStatus(AiTaskStatus.FAILED);
        task.setRetryCount(1);
        when(aiTaskMapper.selectById(5L)).thenReturn(task);
        when(aiTaskMapper.update(isNull(), any())).thenReturn(0);

        assertThatThrownBy(() -> service.retry(5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("任务状态已变化");
        assertThat(scheduledTask.get()).isNull();
    }

    @Test
    void createMarksFailedWhenExecutorRejectsTask() {
        ArgumentCaptor<AiTask> taskCaptor = ArgumentCaptor.forClass(AiTask.class);
        when(aiTaskMapper.insert(taskCaptor.capture())).thenAnswer(invocation -> {
            AiTask task = invocation.getArgument(0);
            task.setId(13L);
            return 1;
        });
        when(aiTaskLogMapper.insert(any(AiTaskLog.class))).thenReturn(1);
        when(aiTaskMapper.update(isNull(), any())).thenReturn(1);
        service = new AiTaskServiceImpl(aiTaskMapper, aiTaskLogMapper, aiTaskResultMapper, runnable -> {
            throw new java.util.concurrent.RejectedExecutionException("full");
        }, testTransactions());

        assertThatThrownBy(() -> service.create(validCreateRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("队列已满");
        verify(aiTaskMapper).update(isNull(), any());
    }

    @Test
    void logsRequireExistingTask() {
        when(aiTaskMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> service.logs(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("AI 任务不存在");
        verify(aiTaskMapper).selectById(999L);
    }

    private CreateAiTaskRequest validCreateRequest() {
        CreateAiTaskRequest request = new CreateAiTaskRequest();
        request.setTitle("  分析异步任务  ");
        request.setRequirement("验证异步任务状态流转、日志记录和结果保存。");
        request.setTaskType(AiTaskType.REQUIREMENT_ANALYSIS);
        request.setRemark("  mock 版本  ");
        return request;
    }

    private TransactionOperations testTransactions() {
        return new TransactionOperations() {
            @Override
            public <T> T execute(TransactionCallback<T> action) {
                return action.doInTransaction(null);
            }
        };
    }
}
