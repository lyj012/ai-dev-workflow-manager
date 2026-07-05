package com.aidev.workflowmanager.delivery.service;

import com.aidev.workflowmanager.common.enums.Complexity;
import com.aidev.workflowmanager.common.enums.RiskTag;
import com.aidev.workflowmanager.common.enums.TaskStatus;
import com.aidev.workflowmanager.common.enums.TaskType;
import com.aidev.workflowmanager.common.exception.BusinessException;
import com.aidev.workflowmanager.delivery.entity.DeliveryRecord;
import com.aidev.workflowmanager.delivery.mapper.DeliveryRecordMapper;
import com.aidev.workflowmanager.delivery.service.impl.DeliveryServiceImpl;
import com.aidev.workflowmanager.delivery.vo.DeliveryRecordResponse;
import com.aidev.workflowmanager.stage.mapper.WorkflowStageMapper;
import com.aidev.workflowmanager.task.entity.WorkflowTask;
import com.aidev.workflowmanager.task.mapper.WorkflowTaskMapper;
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
                .hasMessageContaining("Test checklist must be generated");
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
}
