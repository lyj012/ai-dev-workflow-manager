package com.aidev.workflowmanager.delivery.service;

import com.aidev.workflowmanager.common.enums.Complexity;
import com.aidev.workflowmanager.common.enums.RiskTag;
import com.aidev.workflowmanager.common.enums.StageStatus;
import com.aidev.workflowmanager.common.enums.TaskStatus;
import com.aidev.workflowmanager.common.enums.TaskType;
import com.aidev.workflowmanager.common.exception.BusinessException;
import com.aidev.workflowmanager.delivery.entity.DeliveryRecord;
import com.aidev.workflowmanager.delivery.mapper.DeliveryRecordMapper;
import com.aidev.workflowmanager.delivery.service.impl.DeliveryServiceImpl;
import com.aidev.workflowmanager.delivery.vo.DeliveryRecordResponse;
import com.aidev.workflowmanager.stage.entity.WorkflowStage;
import com.aidev.workflowmanager.stage.mapper.WorkflowStageMapper;
import com.aidev.workflowmanager.task.entity.WorkflowTask;
import com.aidev.workflowmanager.task.mapper.WorkflowTaskMapper;
import com.aidev.workflowmanager.template.entity.WorkflowTemplateStage;
import com.aidev.workflowmanager.template.mapper.WorkflowTemplateMapper;
import com.aidev.workflowmanager.template.mapper.WorkflowTemplateStageMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceImplTest {

    @Mock
    private WorkflowTaskMapper workflowTaskMapper;

    @Mock
    private WorkflowTemplateMapper workflowTemplateMapper;

    @Mock
    private WorkflowTemplateStageMapper workflowTemplateStageMapper;

    @Mock
    private WorkflowStageMapper workflowStageMapper;

    @Mock
    private DeliveryRecordMapper deliveryRecordMapper;

    private DeliveryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DeliveryServiceImpl(workflowTaskMapper, workflowTemplateMapper, workflowTemplateStageMapper,
                workflowStageMapper, deliveryRecordMapper);
    }

    @Test
    void generateTestChecklistCreatesRecordAndMarksTask() {
        WorkflowTask task = task(false);
        when(workflowTaskMapper.selectOne(any(Wrapper.class))).thenReturn(task);
        when(deliveryRecordMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        when(deliveryRecordMapper.insert(any(DeliveryRecord.class))).thenAnswer(invocation -> {
            DeliveryRecord record = invocation.getArgument(0);
            record.setId(88L);
            return 1;
        });
        when(workflowStageMapper.selectList(any(Wrapper.class))).thenReturn(Collections.emptyList());

        DeliveryRecordResponse response = service.generateTestChecklist(1L);

        assertThat(response.getTestChecklist()).contains("权限成功", "主流程接口参数");
        assertThat(task.getTestChecklistGenerated()).isTrue();
        assertThat(task.getDeliveryRecordId()).isEqualTo(88L);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.TESTING);
        ArgumentCaptor<DeliveryRecord> recordCaptor = ArgumentCaptor.forClass(DeliveryRecord.class);
        verify(deliveryRecordMapper).updateById(recordCaptor.capture());
        assertThat(recordCaptor.getValue().getRiskNotes()).contains("高风险");
    }

    @Test
    void generateDeliverySummaryRejectsMissingChecklist() {
        WorkflowTask task = task(false);
        when(workflowTaskMapper.selectOne(any(Wrapper.class))).thenReturn(task);

        assertThatThrownBy(() -> service.generateDeliverySummary(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("请先生成测试清单，再生成交付总结");
    }

    @Test
    void generateDeliverySummaryMarksTaskDeliveredWhenRequiredStagesCompleted() {
        WorkflowTask task = task(true);
        task.setStatus(TaskStatus.TESTING);
        task.setDeliveryRecordId(88L);
        DeliveryRecord record = record(88L, task.getId());
        when(workflowTaskMapper.selectOne(any(Wrapper.class))).thenReturn(task);
        when(deliveryRecordMapper.selectOne(any(Wrapper.class))).thenReturn(record);
        when(workflowStageMapper.selectList(any(Wrapper.class))).thenReturn(Collections.singletonList(
                stage(10L, "analysis", "需求分析", 101L, StageStatus.COMPLETED)
        ));
        when(workflowTemplateStageMapper.selectOne(any(Wrapper.class))).thenReturn(templateStage(true));

        DeliveryRecordResponse response = service.generateDeliverySummary(1L);

        assertThat(response.getSummary()).contains("权限下载功能", "需求分析");
        assertThat(response.getMarkdown()).contains("AI 开发任务交付记录");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.DELIVERED);
        ArgumentCaptor<WorkflowTask> taskCaptor = ArgumentCaptor.forClass(WorkflowTask.class);
        verify(workflowTaskMapper).updateById(taskCaptor.capture());
        assertThat(taskCaptor.getValue().getDeliveryRecordId()).isEqualTo(88L);
        assertThat(taskCaptor.getValue().getStatus()).isEqualTo(TaskStatus.DELIVERED);
    }

    @Test
    void generateDeliverySummaryRejectsIncompleteRequiredStage() {
        WorkflowTask task = task(true);
        task.setStatus(TaskStatus.TESTING);
        task.setDeliveryRecordId(88L);
        when(workflowTaskMapper.selectOne(any(Wrapper.class))).thenReturn(task);
        when(workflowStageMapper.selectList(any(Wrapper.class))).thenReturn(Collections.singletonList(
                stage(10L, "analysis", "需求分析", 101L, StageStatus.RUNNING)
        ));
        when(workflowTemplateStageMapper.selectOne(any(Wrapper.class))).thenReturn(templateStage(true));

        assertThatThrownBy(() -> service.generateDeliverySummary(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("必需阶段未完成，不能交付：需求分析");
    }

    @Test
    void generateDeliverySummaryRejectsFailedRequiredStageWithReadableMessage() {
        WorkflowTask task = task(true);
        task.setStatus(TaskStatus.TESTING);
        task.setDeliveryRecordId(88L);
        when(workflowTaskMapper.selectOne(any(Wrapper.class))).thenReturn(task);
        when(workflowStageMapper.selectList(any(Wrapper.class))).thenReturn(Collections.singletonList(
                stage(10L, "analysis", "需求分析", 101L, StageStatus.FAILED)
        ));
        when(workflowTemplateStageMapper.selectOne(any(Wrapper.class))).thenReturn(templateStage(true));

        assertThatThrownBy(() -> service.generateDeliverySummary(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("必需阶段失败，不能交付：需求分析");
    }

    private WorkflowTask task(boolean checklistGenerated) {
        WorkflowTask task = new WorkflowTask();
        task.setId(1L);
        task.setTitle("权限下载功能");
        task.setTaskType(TaskType.FEATURE);
        task.setComplexity(Complexity.COMPLEX);
        task.setRiskTags(Arrays.asList(RiskTag.PERMISSION, RiskTag.DOWNLOAD));
        task.setStatus(TaskStatus.DRAFT);
        task.setTestChecklistGenerated(checklistGenerated);
        return task;
    }

    private DeliveryRecord record(Long id, Long taskId) {
        DeliveryRecord record = new DeliveryRecord();
        record.setId(id);
        record.setTaskId(taskId);
        record.setTestChecklist("- [ ] 主流程接口参数、响应字段与前端调用保持一致。");
        record.setTestResult("人工流程已验证。");
        record.setRiskNotes("该任务包含高风险因素：permission, download。");
        return record;
    }

    private WorkflowStage stage(Long id, String key, String name, Long templateStageId, StageStatus status) {
        WorkflowStage stage = new WorkflowStage();
        stage.setId(id);
        stage.setTaskId(1L);
        stage.setTemplateStageId(templateStageId);
        stage.setStageKey(key);
        stage.setStageName(name);
        stage.setStageOrder(1);
        stage.setStatus(status);
        stage.setOutputSummary("输出摘要:\n已完成分析\n\n风险点:\n无新增风险\n\n后续动作:\n继续交付\n\n未验证范围:\n无");
        return stage;
    }

    private WorkflowTemplateStage templateStage(boolean required) {
        WorkflowTemplateStage stage = new WorkflowTemplateStage();
        stage.setId(101L);
        stage.setRequired(required);
        return stage;
    }
}
