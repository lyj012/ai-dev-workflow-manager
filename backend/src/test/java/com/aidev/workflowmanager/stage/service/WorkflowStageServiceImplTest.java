package com.aidev.workflowmanager.stage.service;

import com.aidev.workflowmanager.common.enums.StageStatus;
import com.aidev.workflowmanager.common.enums.TaskStatus;
import com.aidev.workflowmanager.common.exception.BusinessException;
import com.aidev.workflowmanager.stage.entity.WorkflowStage;
import com.aidev.workflowmanager.stage.mapper.WorkflowStageMapper;
import com.aidev.workflowmanager.stage.service.impl.WorkflowStageServiceImpl;
import com.aidev.workflowmanager.stage.vo.StageInitResponse;
import com.aidev.workflowmanager.task.entity.WorkflowTask;
import com.aidev.workflowmanager.task.mapper.WorkflowTaskMapper;
import com.aidev.workflowmanager.template.entity.WorkflowTemplate;
import com.aidev.workflowmanager.template.entity.WorkflowTemplateStage;
import com.aidev.workflowmanager.template.mapper.WorkflowTemplateMapper;
import com.aidev.workflowmanager.template.mapper.WorkflowTemplateStageMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.springframework.dao.DuplicateKeyException;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowStageServiceImplTest {

    @Mock
    private WorkflowTaskMapper workflowTaskMapper;

    @Mock
    private WorkflowTemplateMapper workflowTemplateMapper;

    @Mock
    private WorkflowTemplateStageMapper workflowTemplateStageMapper;

    @Mock
    private WorkflowStageMapper workflowStageMapper;

    private WorkflowStageServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WorkflowStageServiceImpl(workflowTaskMapper, workflowTemplateMapper,
                workflowTemplateStageMapper, workflowStageMapper);
    }

    @Test
    void initializeStagesCopiesTemplateStagesAsPending() {
        when(workflowTaskMapper.selectOne(any(Wrapper.class))).thenReturn(task(TaskStatus.DRAFT, 20L));
        when(workflowTemplateMapper.selectOne(any(Wrapper.class))).thenReturn(template(20L));
        when(workflowTemplateStageMapper.selectList(any(Wrapper.class))).thenReturn(Arrays.asList(
                templateStage(101L, "analysis", "需求分析", 1),
                templateStage(102L, "implementation", "实现修改", 2)
        ));
        ArgumentCaptor<WorkflowStage> stageCaptor = ArgumentCaptor.forClass(WorkflowStage.class);
        when(workflowStageMapper.selectList(any(Wrapper.class)))
                .thenReturn(Collections.<WorkflowStage>emptyList())
                .thenAnswer(invocation -> stageCaptor.getAllValues());
        when(workflowStageMapper.insert(stageCaptor.capture())).thenAnswer(invocation -> {
            WorkflowStage stage = invocation.getArgument(0);
            stage.setId(Long.valueOf(stage.getStageOrder()));
            return 1;
        });

        StageInitResponse response = service.initializeStages(1L);

        assertThat(response.getTaskId()).isEqualTo(1L);
        assertThat(response.getMatchedTemplateId()).isEqualTo(20L);
        assertThat(response.getStages()).hasSize(2);
        assertThat(response.getStages()).extracting("stageKey").containsExactly("analysis", "implementation");
        assertThat(response.getStages()).extracting("status").containsExactly(StageStatus.PENDING, StageStatus.PENDING);
        assertThat(stageCaptor.getAllValues()).allMatch(stage -> stage.getDeleted() == 0);
        assertThat(stageCaptor.getAllValues()).allMatch(stage -> stage.getStartedAt() == null && stage.getCompletedAt() == null);
        ArgumentCaptor<WorkflowTask> taskCaptor = ArgumentCaptor.forClass(WorkflowTask.class);
        verify(workflowTaskMapper).updateById(taskCaptor.capture());
        assertThat(taskCaptor.getValue().getStatus()).isEqualTo(TaskStatus.EXECUTING);
    }

    @Test
    void initializeStagesReturnsCompleteExistingStagesWithoutInsert() {
        when(workflowTaskMapper.selectOne(any(Wrapper.class))).thenReturn(task(TaskStatus.DRAFT, 20L));
        when(workflowTemplateMapper.selectOne(any(Wrapper.class))).thenReturn(template(20L));
        when(workflowTemplateStageMapper.selectList(any(Wrapper.class))).thenReturn(Arrays.asList(
                templateStage(101L, "analysis", "需求分析", 1),
                templateStage(102L, "implementation", "实现修改", 2)
        ));
        WorkflowStage second = stage(2L, "implementation", 2, StageStatus.PENDING);
        WorkflowStage first = stage(1L, "analysis", 1, StageStatus.RUNNING);
        when(workflowStageMapper.selectList(any(Wrapper.class))).thenReturn(Arrays.asList(second, first));

        StageInitResponse response = service.initializeStages(1L);

        assertThat(response.getStages()).extracting("stageKey").containsExactly("analysis", "implementation");
        verify(workflowStageMapper, never()).insert(any(WorkflowStage.class));
        verify(workflowTaskMapper).updateById(any(WorkflowTask.class));
    }

    @Test
    void initializeStagesCompletesPartialExistingStagesWithoutOverwritingExistingStatus() {
        when(workflowTaskMapper.selectOne(any(Wrapper.class))).thenReturn(task(TaskStatus.DRAFT, 20L));
        when(workflowTemplateMapper.selectOne(any(Wrapper.class))).thenReturn(template(20L));
        when(workflowTemplateStageMapper.selectList(any(Wrapper.class))).thenReturn(Arrays.asList(
                templateStage(101L, "analysis", "需求分析", 1),
                templateStage(102L, "risk_review", "风险评审", 2),
                templateStage(103L, "implementation", "实现修改", 3)
        ));
        WorkflowStage existing = stage(1L, "analysis", 1, StageStatus.RUNNING);
        WorkflowStage insertedRiskReview = stage(2L, "risk_review", 2, StageStatus.PENDING);
        WorkflowStage insertedImplementation = stage(3L, "implementation", 3, StageStatus.PENDING);
        when(workflowStageMapper.selectList(any(Wrapper.class)))
                .thenReturn(Collections.singletonList(existing))
                .thenReturn(Arrays.asList(existing, insertedRiskReview, insertedImplementation));
        ArgumentCaptor<WorkflowStage> stageCaptor = ArgumentCaptor.forClass(WorkflowStage.class);
        when(workflowStageMapper.insert(stageCaptor.capture())).thenReturn(1);

        StageInitResponse response = service.initializeStages(1L);

        assertThat(stageCaptor.getAllValues()).extracting(WorkflowStage::getStageKey)
                .containsExactly("risk_review", "implementation");
        assertThat(response.getStages()).extracting("stageKey")
                .containsExactly("analysis", "risk_review", "implementation");
        assertThat(response.getStages()).extracting("status")
                .containsExactly(StageStatus.RUNNING, StageStatus.PENDING, StageStatus.PENDING);
    }

    @Test
    void initializeStagesRejectsExistingStageOrderWithDifferentKey() {
        when(workflowTaskMapper.selectOne(any(Wrapper.class))).thenReturn(task(TaskStatus.DRAFT, 20L));
        when(workflowTemplateMapper.selectOne(any(Wrapper.class))).thenReturn(template(20L));
        when(workflowTemplateStageMapper.selectList(any(Wrapper.class))).thenReturn(Arrays.asList(
                templateStage(101L, "analysis", "需求分析", 1),
                templateStage(102L, "risk_review", "风险评审", 2)
        ));
        when(workflowStageMapper.selectList(any(Wrapper.class))).thenReturn(Arrays.asList(
                stage(1L, "analysis", 1, StageStatus.PENDING),
                stage(2L, "implementation", 2, StageStatus.PENDING)
        ));

        assertThatThrownBy(() -> service.initializeStages(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("stage order 2 expected key risk_review but found implementation");
        verify(workflowStageMapper, never()).insert(any(WorkflowStage.class));
    }

    @Test
    void initializeStagesReturnsCompleteStagesAfterDuplicateInsertConflict() {
        when(workflowTaskMapper.selectOne(any(Wrapper.class))).thenReturn(task(TaskStatus.DRAFT, 20L));
        when(workflowTemplateMapper.selectOne(any(Wrapper.class))).thenReturn(template(20L));
        when(workflowTemplateStageMapper.selectList(any(Wrapper.class))).thenReturn(Arrays.asList(
                templateStage(101L, "analysis", "需求分析", 1),
                templateStage(102L, "implementation", "实现修改", 2)
        ));
        WorkflowStage analysis = stage(1L, "analysis", 1, StageStatus.PENDING);
        WorkflowStage implementation = stage(2L, "implementation", 2, StageStatus.PENDING);
        when(workflowStageMapper.selectList(any(Wrapper.class)))
                .thenReturn(Collections.<WorkflowStage>emptyList())
                .thenReturn(Arrays.asList(analysis, implementation));
        when(workflowStageMapper.insert(any(WorkflowStage.class))).thenThrow(new DuplicateKeyException("duplicate"));

        StageInitResponse response = service.initializeStages(1L);

        assertThat(response.getStages()).extracting("stageKey").containsExactly("analysis", "implementation");
    }

    @Test
    void initializeStagesRejectsMissingTask() {
        when(workflowTaskMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        assertThatThrownBy(() -> service.initializeStages(404L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Task not found: 404");
    }

    @Test
    void initializeStagesRejectsTaskWithoutMatchedTemplate() {
        when(workflowTaskMapper.selectOne(any(Wrapper.class))).thenReturn(task(TaskStatus.DRAFT, null));

        assertThatThrownBy(() -> service.initializeStages(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Task has no matched template");
    }

    @Test
    void initializeStagesRejectsMissingOrDisabledTemplate() {
        when(workflowTaskMapper.selectOne(any(Wrapper.class))).thenReturn(task(TaskStatus.DRAFT, 20L));
        when(workflowTemplateMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        assertThatThrownBy(() -> service.initializeStages(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Workflow template not found or disabled: 20");
    }

    @Test
    void initializeStagesRejectsTemplateWithNoStages() {
        when(workflowTaskMapper.selectOne(any(Wrapper.class))).thenReturn(task(TaskStatus.DRAFT, 20L));
        when(workflowTemplateMapper.selectOne(any(Wrapper.class))).thenReturn(template(20L));
        when(workflowTemplateStageMapper.selectList(any(Wrapper.class))).thenReturn(Collections.<WorkflowTemplateStage>emptyList());

        assertThatThrownBy(() -> service.initializeStages(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Workflow template has no stages: 20");
    }

    @Test
    void initializeStagesRejectsArchivedAndCanceledTasks() {
        when(workflowTaskMapper.selectOne(any(Wrapper.class)))
                .thenReturn(task(TaskStatus.ARCHIVED, 20L))
                .thenReturn(task(TaskStatus.CANCELED, 20L));

        assertThatThrownBy(() -> service.initializeStages(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Task status does not allow");
        assertThatThrownBy(() -> service.initializeStages(2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Task status does not allow");
    }

    @Test
    void initializeStagesRejectsInvalidTaskId() {
        assertThatThrownBy(() -> service.initializeStages(0L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("taskId must be greater than or equal to 1");
    }

    private WorkflowTask task(TaskStatus status, Long matchedTemplateId) {
        WorkflowTask task = new WorkflowTask();
        task.setId(1L);
        task.setStatus(status);
        task.setMatchedTemplateId(matchedTemplateId);
        return task;
    }

    private WorkflowTemplate template(Long id) {
        WorkflowTemplate template = new WorkflowTemplate();
        template.setId(id);
        template.setEnabled(true);
        return template;
    }

    private WorkflowTemplateStage templateStage(Long id, String key, String name, Integer order) {
        WorkflowTemplateStage stage = new WorkflowTemplateStage();
        stage.setId(id);
        stage.setStageKey(key);
        stage.setStageName(name);
        stage.setStageOrder(order);
        return stage;
    }

    private WorkflowStage stage(Long id, String key, Integer order, StageStatus status) {
        WorkflowStage stage = new WorkflowStage();
        stage.setId(id);
        stage.setTaskId(1L);
        stage.setTemplateStageId(100L + order);
        stage.setStageKey(key);
        stage.setStageName(key);
        stage.setStageOrder(order);
        stage.setStatus(status);
        return stage;
    }
}
