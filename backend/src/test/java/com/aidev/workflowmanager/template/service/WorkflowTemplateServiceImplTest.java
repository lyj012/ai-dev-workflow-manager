package com.aidev.workflowmanager.template.service;

import com.aidev.workflowmanager.common.enums.Complexity;
import com.aidev.workflowmanager.common.enums.RiskTag;
import com.aidev.workflowmanager.common.enums.TaskType;
import com.aidev.workflowmanager.common.exception.BusinessException;
import com.aidev.workflowmanager.template.entity.WorkflowTemplate;
import com.aidev.workflowmanager.template.entity.WorkflowTemplateStage;
import com.aidev.workflowmanager.template.mapper.WorkflowTemplateMapper;
import com.aidev.workflowmanager.template.mapper.WorkflowTemplateStageMapper;
import com.aidev.workflowmanager.template.service.impl.WorkflowTemplateServiceImpl;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowTemplateServiceImplTest {

    @Mock
    private WorkflowTemplateMapper workflowTemplateMapper;

    @Mock
    private WorkflowTemplateStageMapper workflowTemplateStageMapper;

    private WorkflowTemplateServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WorkflowTemplateServiceImpl(workflowTemplateMapper, workflowTemplateStageMapper);
    }

    @Test
    void ensureBuiltInTemplatesCreatesThreeTemplatesAndStages() {
        ArgumentCaptor<WorkflowTemplate> templateCaptor = ArgumentCaptor.forClass(WorkflowTemplate.class);
        ArgumentCaptor<WorkflowTemplateStage> stageCaptor = ArgumentCaptor.forClass(WorkflowTemplateStage.class);
        when(workflowTemplateMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        when(workflowTemplateMapper.insert(templateCaptor.capture())).thenAnswer(invocation -> {
            WorkflowTemplate template = invocation.getArgument(0);
            template.setId(Long.valueOf(templateCaptor.getAllValues().size()));
            return 1;
        });
        when(workflowTemplateStageMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        when(workflowTemplateStageMapper.insert(stageCaptor.capture())).thenReturn(1);

        service.ensureBuiltInTemplates();

        List<WorkflowTemplate> templates = templateCaptor.getAllValues();
        assertThat(templates).hasSize(3);
        assertThat(templates).extracting(WorkflowTemplate::getName)
                .containsExactly("简单任务 workflow", "标准开发 workflow", "高风险 workflow");
        assertThat(templates.get(0).getTaskType()).isEqualTo(TaskType.DOCS);
        assertThat(templates.get(0).getComplexity()).isEqualTo(Complexity.SIMPLE);
        assertThat(templates.get(0).getRiskTags()).isEmpty();
        assertThat(templates.get(0).getPriority()).isEqualTo(10);
        assertThat(templates.get(1).getTaskType()).isEqualTo(TaskType.FEATURE);
        assertThat(templates.get(1).getComplexity()).isEqualTo(Complexity.MEDIUM);
        assertThat(templates.get(1).getPriority()).isEqualTo(20);
        assertThat(templates.get(2).getTaskType()).isEqualTo(TaskType.FEATURE);
        assertThat(templates.get(2).getComplexity()).isEqualTo(Complexity.COMPLEX);
        assertThat(templates.get(2).getRiskTags()).containsExactly(RiskTag.values());
        assertThat(templates.get(2).getPriority()).isEqualTo(100);
        assertThat(templates).allMatch(WorkflowTemplate::getEnabled);
        assertThat(templates).allMatch(template -> template.getVersion() == 1);

        List<WorkflowTemplateStage> stages = stageCaptor.getAllValues();
        assertThat(stages).hasSize(17);
        List<WorkflowTemplateStage> highRiskStages = stages.stream()
                .filter(stage -> Long.valueOf(3L).equals(stage.getTemplateId()))
                .collect(Collectors.toList());
        assertThat(highRiskStages).extracting(WorkflowTemplateStage::getStageKey)
                .containsExactly("analysis", "risk_review", "design", "implementation", "review", "testing", "delivery");
        WorkflowTemplateStage riskReview = highRiskStages.get(1);
        assertThat(riskReview.getStageName()).isEqualTo("风险评审");
        assertThat(riskReview.getRequired()).isTrue();
        assertThat(riskReview.getStageOrder()).isEqualTo(2);
    }

    @Test
    void ensureBuiltInTemplatesDoesNotDuplicateExistingTemplatesOrStages() {
        WorkflowTemplate existingTemplate = new WorkflowTemplate();
        existingTemplate.setId(9L);
        when(workflowTemplateMapper.selectOne(any(Wrapper.class))).thenReturn(existingTemplate);
        List<String> existingKeys = Arrays.asList(
                "analysis", "implementation", "testing", "delivery",
                "analysis", "design", "implementation", "review", "testing", "delivery",
                "analysis", "risk_review", "design", "implementation", "review", "testing", "delivery"
        );
        final int[] index = {0};
        when(workflowTemplateStageMapper.selectOne(any(Wrapper.class)))
                .thenAnswer(invocation -> existingStage(existingKeys.get(index[0]++)));

        service.ensureBuiltInTemplates();

        verify(workflowTemplateMapper, never()).insert(any(WorkflowTemplate.class));
        verify(workflowTemplateStageMapper, never()).insert(any(WorkflowTemplateStage.class));
    }

    @Test
    void ensureBuiltInTemplatesRejectsExistingStageOrderWithDifferentKey() {
        WorkflowTemplate existingTemplate = new WorkflowTemplate();
        existingTemplate.setId(9L);
        WorkflowTemplateStage existingStage = new WorkflowTemplateStage();
        existingStage.setId(19L);
        existingStage.setStageKey("wrong_key");
        when(workflowTemplateMapper.selectOne(any(Wrapper.class))).thenReturn(existingTemplate);
        when(workflowTemplateStageMapper.selectOne(any(Wrapper.class))).thenReturn(existingStage);

        assertThatThrownBy(() -> service.ensureBuiltInTemplates())
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("内置 workflow 模板阶段配置冲突")
                .hasMessageContaining("第 1 个阶段应为 analysis，实际为 wrong_key");
        verify(workflowTemplateMapper, never()).insert(any(WorkflowTemplate.class));
        verify(workflowTemplateStageMapper, never()).insert(any(WorkflowTemplateStage.class));
    }

    @Test
    void ensureBuiltInTemplatesRejectsExistingHighRiskRiskReviewWithDifferentRequiredFlag() {
        WorkflowTemplate existingTemplate = new WorkflowTemplate();
        existingTemplate.setId(9L);
        when(workflowTemplateMapper.selectOne(any(Wrapper.class))).thenReturn(existingTemplate);
        List<WorkflowTemplateStage> existingStages = Arrays.asList(
                existingStage("analysis"),
                existingStage("implementation"),
                existingStage("testing"),
                existingStage("delivery"),
                existingStage("analysis"),
                existingStage("design"),
                existingStage("implementation"),
                existingStage("review"),
                existingStage("testing"),
                existingStage("delivery"),
                existingStage("analysis"),
                existingStage("risk_review", false)
        );
        final int[] index = {0};
        when(workflowTemplateStageMapper.selectOne(any(Wrapper.class)))
                .thenAnswer(invocation -> existingStages.get(index[0]++));

        assertThatThrownBy(() -> service.ensureBuiltInTemplates())
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("内置 workflow 模板阶段必需配置冲突")
                .hasMessageContaining("risk_review 应为 true，实际为 false");
        verify(workflowTemplateMapper, never()).insert(any(WorkflowTemplate.class));
        verify(workflowTemplateStageMapper, never()).insert(any(WorkflowTemplateStage.class));
    }

    private WorkflowTemplateStage existingStage(String stageKey) {
        return existingStage(stageKey, true);
    }

    private WorkflowTemplateStage existingStage(String stageKey, boolean required) {
        WorkflowTemplateStage stage = new WorkflowTemplateStage();
        stage.setId(19L);
        stage.setStageKey(stageKey);
        stage.setRequired(required);
        return stage;
    }
}
