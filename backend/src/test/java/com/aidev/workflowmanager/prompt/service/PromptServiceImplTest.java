package com.aidev.workflowmanager.prompt.service;

import com.aidev.workflowmanager.common.enums.Complexity;
import com.aidev.workflowmanager.common.enums.RiskTag;
import com.aidev.workflowmanager.common.enums.StageStatus;
import com.aidev.workflowmanager.common.enums.TaskStatus;
import com.aidev.workflowmanager.common.enums.TaskType;
import com.aidev.workflowmanager.common.exception.BusinessException;
import com.aidev.workflowmanager.prompt.service.impl.PromptServiceImpl;
import com.aidev.workflowmanager.prompt.vo.GeneratePromptResponse;
import com.aidev.workflowmanager.stage.entity.WorkflowStage;
import com.aidev.workflowmanager.stage.mapper.WorkflowStageMapper;
import com.aidev.workflowmanager.stage.vo.StageOutputParts;
import com.aidev.workflowmanager.task.entity.WorkflowTask;
import com.aidev.workflowmanager.task.mapper.WorkflowTaskMapper;
import com.aidev.workflowmanager.template.entity.WorkflowTemplate;
import com.aidev.workflowmanager.template.entity.WorkflowTemplateStage;
import com.aidev.workflowmanager.template.mapper.WorkflowTemplateMapper;
import com.aidev.workflowmanager.template.mapper.WorkflowTemplateStageMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromptServiceImplTest {

    @Mock
    private WorkflowTaskMapper workflowTaskMapper;

    @Mock
    private WorkflowTemplateMapper workflowTemplateMapper;

    @Mock
    private WorkflowTemplateStageMapper workflowTemplateStageMapper;

    @Mock
    private WorkflowStageMapper workflowStageMapper;

    private PromptServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PromptServiceImpl(workflowTaskMapper, workflowTemplateMapper,
                workflowTemplateStageMapper, workflowStageMapper);
    }

    @Test
    void generatePromptIncludesVariablesPreviousOutputsAndHighRiskRequirements() {
        WorkflowTask task = task();
        WorkflowStage currentStage = stage(20L, 202L, "implementation", "实现修改", 3, null);
        WorkflowStage previousStage = stage(10L, 201L, "analysis", "需求分析", 1,
                StageOutputParts.of("确认下载权限链路需要修复", "权限边界需要复核", "进入实现", "未跑浏览器验证").format());
        when(workflowTaskMapper.selectOne(any(Wrapper.class))).thenReturn(task);
        when(workflowStageMapper.selectOne(any(Wrapper.class))).thenReturn(currentStage);
        when(workflowTemplateMapper.selectOne(any(Wrapper.class))).thenReturn(template());
        when(workflowTemplateStageMapper.selectOne(any(Wrapper.class))).thenReturn(templateStage());
        when(workflowStageMapper.selectList(any(Wrapper.class))).thenReturn(Collections.singletonList(previousStage));

        GeneratePromptResponse response = service.generatePrompt(1L, 20L);

        assertThat(response.getPromptTemplateId()).isEqualTo(900L);
        assertThat(response.getVariables()).containsEntry("taskTitle", "修复下载权限");
        assertThat(response.getVariables()).containsEntry("riskTags", "permission, download");
        assertThat(response.getVariables().get("previousStageOutputs")).contains("确认下载权限链路需要修复");
        assertThat(response.getPromptContent()).contains("高风险补充要求");
        assertThat(response.getPromptContent()).contains("权限、支付、数据库、配置、文件处理、下载、金额或回调");
        assertThat(response.getPromptContent()).contains("当前阶段：实现修改");
    }

    @Test
    void generatePromptRejectsMissingStage() {
        when(workflowTaskMapper.selectOne(any(Wrapper.class))).thenReturn(task());
        when(workflowStageMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        assertThatThrownBy(() -> service.generatePrompt(1L, 404L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Workflow stage not found: 404");
    }

    private WorkflowTask task() {
        WorkflowTask task = new WorkflowTask();
        task.setId(1L);
        task.setTitle("修复下载权限");
        task.setDescription("下载接口需要正确处理有权限和无权限场景。");
        task.setTaskType(TaskType.BUGFIX);
        task.setComplexity(Complexity.COMPLEX);
        task.setRiskTags(Arrays.asList(RiskTag.PERMISSION, RiskTag.DOWNLOAD));
        task.setStatus(TaskStatus.EXECUTING);
        task.setMatchedTemplateId(100L);
        return task;
    }

    private WorkflowTemplate template() {
        WorkflowTemplate template = new WorkflowTemplate();
        template.setId(100L);
        template.setName("高风险 workflow");
        return template;
    }

    private WorkflowTemplateStage templateStage() {
        WorkflowTemplateStage stage = new WorkflowTemplateStage();
        stage.setId(202L);
        stage.setDescription("实现并验证下载权限修复。");
        stage.setDefaultPromptTemplateId(900L);
        return stage;
    }

    private WorkflowStage stage(Long id, Long templateStageId, String key, String name, Integer order, String outputSummary) {
        WorkflowStage stage = new WorkflowStage();
        stage.setId(id);
        stage.setTaskId(1L);
        stage.setTemplateStageId(templateStageId);
        stage.setStageKey(key);
        stage.setStageName(name);
        stage.setStageOrder(order);
        stage.setStatus(StageStatus.COMPLETED);
        stage.setInputSummary(name + "目标");
        stage.setOutputSummary(outputSummary);
        return stage;
    }
}
