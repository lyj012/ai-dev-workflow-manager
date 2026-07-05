package com.aidev.workflowmanager.matching.service;

import com.aidev.workflowmanager.common.enums.Complexity;
import com.aidev.workflowmanager.common.enums.RiskTag;
import com.aidev.workflowmanager.common.enums.TaskStatus;
import com.aidev.workflowmanager.common.enums.TaskType;
import com.aidev.workflowmanager.common.exception.BusinessException;
import com.aidev.workflowmanager.matching.service.impl.WorkflowTemplateMatchingServiceImpl;
import com.aidev.workflowmanager.matching.vo.TemplateMatchResponse;
import com.aidev.workflowmanager.task.entity.WorkflowTask;
import com.aidev.workflowmanager.task.mapper.WorkflowTaskMapper;
import com.aidev.workflowmanager.template.entity.WorkflowTemplate;
import com.aidev.workflowmanager.template.mapper.WorkflowTemplateMapper;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowTemplateMatchingServiceImplTest {

    @Mock
    private WorkflowTaskMapper workflowTaskMapper;

    @Mock
    private WorkflowTemplateMapper workflowTemplateMapper;

    private WorkflowTemplateMatchingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WorkflowTemplateMatchingServiceImpl(workflowTaskMapper, workflowTemplateMapper);
    }

    @Test
    void matchDocsSimpleAutoBindsSimpleWorkflow() {
        when(workflowTaskMapper.selectOne(any(Wrapper.class)))
                .thenReturn(task(1L, TaskType.DOCS, Complexity.SIMPLE, Collections.<RiskTag>emptyList(), TaskStatus.DRAFT));
        when(workflowTemplateMapper.selectList(any(Wrapper.class)))
                .thenReturn(Arrays.asList(
                        template(10L, "简单任务 workflow", TaskType.DOCS, Complexity.SIMPLE,
                                Collections.<RiskTag>emptyList(), 10, 1, true),
                        template(20L, "标准开发 workflow", TaskType.FEATURE, Complexity.MEDIUM,
                                Collections.<RiskTag>emptyList(), 20, 1, true)
                ));

        TemplateMatchResponse response = service.matchTemplate(1L);

        assertThat(response.getAutoBound()).isTrue();
        assertThat(response.getMatchedTemplateId()).isEqualTo(10L);
        assertThat(response.getMatchedTemplateName()).isEqualTo("简单任务 workflow");
        assertThat(response.getMatchScore()).isEqualTo(70);
        ArgumentCaptor<WorkflowTask> taskCaptor = ArgumentCaptor.forClass(WorkflowTask.class);
        verify(workflowTaskMapper).updateById(taskCaptor.capture());
        assertThat(taskCaptor.getValue().getMatchedTemplateId()).isEqualTo(10L);
        assertThat(taskCaptor.getValue().getStatus()).isEqualTo(TaskStatus.DRAFT);
    }

    @Test
    void matchFeatureMediumAutoBindsStandardWorkflow() {
        when(workflowTaskMapper.selectOne(any(Wrapper.class)))
                .thenReturn(task(2L, TaskType.FEATURE, Complexity.MEDIUM,
                        Collections.<RiskTag>emptyList(), TaskStatus.ANALYZING));
        when(workflowTemplateMapper.selectList(any(Wrapper.class)))
                .thenReturn(Arrays.asList(
                        template(10L, "简单任务 workflow", TaskType.DOCS, Complexity.SIMPLE,
                                Collections.<RiskTag>emptyList(), 10, 1, true),
                        template(20L, "标准开发 workflow", TaskType.FEATURE, Complexity.MEDIUM,
                                Collections.<RiskTag>emptyList(), 20, 1, true)
                ));

        TemplateMatchResponse response = service.matchTemplate(2L);

        assertThat(response.getAutoBound()).isTrue();
        assertThat(response.getMatchedTemplateId()).isEqualTo(20L);
        assertThat(response.getMatchScore()).isEqualTo(70);
        verify(workflowTaskMapper).updateById(any(WorkflowTask.class));
    }

    @Test
    void matchFeatureComplexWithRiskTagsPrioritizesHighRiskWorkflow() {
        when(workflowTaskMapper.selectOne(any(Wrapper.class)))
                .thenReturn(task(3L, TaskType.FEATURE, Complexity.COMPLEX,
                        Arrays.asList(RiskTag.PERMISSION, RiskTag.DOWNLOAD, RiskTag.PAYMENT), TaskStatus.DRAFT));
        when(workflowTemplateMapper.selectList(any(Wrapper.class)))
                .thenReturn(Arrays.asList(
                        template(20L, "标准开发 workflow", TaskType.FEATURE, Complexity.MEDIUM,
                                Collections.<RiskTag>emptyList(), 20, 1, true),
                        template(30L, "高风险 workflow", TaskType.FEATURE, Complexity.COMPLEX,
                                Arrays.asList(RiskTag.values()), 100, 1, true)
                ));

        TemplateMatchResponse response = service.matchTemplate(3L);

        assertThat(response.getAutoBound()).isTrue();
        assertThat(response.getMatchedTemplateId()).isEqualTo(30L);
        assertThat(response.getMatchScore()).isEqualTo(260);
        assertThat(response.getMatchReasons()).anyMatch(reason -> reason.contains("High-risk priority"));
        verify(workflowTaskMapper).updateById(any(WorkflowTask.class));
    }

    @Test
    void matchRejectsWhenNoEnabledTemplatesMatch() {
        when(workflowTaskMapper.selectOne(any(Wrapper.class)))
                .thenReturn(task(4L, TaskType.BUGFIX, Complexity.SIMPLE,
                        Collections.<RiskTag>emptyList(), TaskStatus.DRAFT));
        when(workflowTemplateMapper.selectList(any(Wrapper.class)))
                .thenReturn(Collections.singletonList(template(10L, "unmatched", TaskType.DOCS, Complexity.MEDIUM,
                        Collections.<RiskTag>emptyList(), 10, 1, true)));

        assertThatThrownBy(() -> service.matchTemplate(4L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No enabled workflow template can match task: 4");
        verify(workflowTaskMapper, never()).updateById(any(WorkflowTask.class));
    }

    @Test
    void matchRejectsArchivedAndCanceledTasks() {
        when(workflowTaskMapper.selectOne(any(Wrapper.class)))
                .thenReturn(task(5L, TaskType.DOCS, Complexity.SIMPLE,
                        Collections.<RiskTag>emptyList(), TaskStatus.ARCHIVED))
                .thenReturn(task(6L, TaskType.DOCS, Complexity.SIMPLE,
                        Collections.<RiskTag>emptyList(), TaskStatus.CANCELED));

        assertThatThrownBy(() -> service.matchTemplate(5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Task status does not allow template matching: ARCHIVED");
        assertThatThrownBy(() -> service.matchTemplate(6L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Task status does not allow template matching: CANCELED");
        verify(workflowTaskMapper, never()).updateById(any(WorkflowTask.class));
    }

    @Test
    void matchRejectsMissingTaskAndInvalidTaskId() {
        assertThatThrownBy(() -> service.matchTemplate(null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("taskId must be greater than or equal to 1");
        assertThatThrownBy(() -> service.matchTemplate(0L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("taskId must be greater than or equal to 1");

        when(workflowTaskMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        assertThatThrownBy(() -> service.matchTemplate(404L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Task not found: 404");
    }

    @Test
    void matchTiedCandidatesDoesNotUpdateTask() {
        when(workflowTaskMapper.selectOne(any(Wrapper.class)))
                .thenReturn(task(7L, TaskType.FEATURE, Complexity.MEDIUM,
                        Collections.<RiskTag>emptyList(), TaskStatus.DRAFT));
        when(workflowTemplateMapper.selectList(any(Wrapper.class)))
                .thenReturn(Arrays.asList(
                        template(20L, "standard-v1-a", TaskType.FEATURE, Complexity.MEDIUM,
                                Collections.<RiskTag>emptyList(), 20, 1, true),
                        template(21L, "standard-v1-b", TaskType.FEATURE, Complexity.MEDIUM,
                                Collections.<RiskTag>emptyList(), 20, 1, true)
                ));

        TemplateMatchResponse response = service.matchTemplate(7L);

        assertThat(response.getAutoBound()).isFalse();
        assertThat(response.getMatchedTemplateId()).isNull();
        assertThat(response.getMatchedTemplateName()).isNull();
        assertThat(response.getCandidates()).extracting("templateId").containsExactly(20L, 21L);
        assertThat(response.getMatchReasons()).contains("Multiple candidates require user selection");
        verify(workflowTaskMapper, never()).updateById(any(WorkflowTask.class));
    }

    @Test
    void matchExcludesDisabledTemplatesFromMapperQueryResult() {
        when(workflowTaskMapper.selectOne(any(Wrapper.class)))
                .thenReturn(task(8L, TaskType.DOCS, Complexity.SIMPLE,
                        Collections.<RiskTag>emptyList(), TaskStatus.DRAFT));
        when(workflowTemplateMapper.selectList(any(Wrapper.class)))
                .thenReturn(Collections.singletonList(template(11L, "enabled-simple", TaskType.DOCS, Complexity.SIMPLE,
                        Collections.<RiskTag>emptyList(), 10, 1, true)));

        TemplateMatchResponse response = service.matchTemplate(8L);

        assertThat(response.getMatchedTemplateId()).isEqualTo(11L);
        verify(workflowTemplateMapper).selectList(any(Wrapper.class));
    }

    @Test
    void matchHandlesNullRiskTagsSafely() {
        when(workflowTaskMapper.selectOne(any(Wrapper.class)))
                .thenReturn(task(9L, TaskType.DOCS, Complexity.SIMPLE, null, TaskStatus.DRAFT));
        when(workflowTemplateMapper.selectList(any(Wrapper.class)))
                .thenReturn(Collections.singletonList(template(10L, "simple", TaskType.DOCS, Complexity.SIMPLE,
                        null, null, null, true)));

        TemplateMatchResponse response = service.matchTemplate(9L);

        assertThat(response.getMatchedTemplateId()).isEqualTo(10L);
        assertThat(response.getMatchScore()).isEqualTo(70);
    }

    @Test
    void matchUsesPriorityAndVersionTieBreakersBeforeId() {
        when(workflowTaskMapper.selectOne(any(Wrapper.class)))
                .thenReturn(task(10L, TaskType.FEATURE, Complexity.MEDIUM,
                        Collections.<RiskTag>emptyList(), TaskStatus.DRAFT));
        when(workflowTemplateMapper.selectList(any(Wrapper.class)))
                .thenReturn(Arrays.asList(
                        template(20L, "low-priority", TaskType.FEATURE, Complexity.MEDIUM,
                                Collections.<RiskTag>emptyList(), 10, 9, true),
                        template(30L, "high-priority-old-version", TaskType.FEATURE, Complexity.MEDIUM,
                                Collections.<RiskTag>emptyList(), 20, 1, true),
                        template(40L, "high-priority-new-version", TaskType.FEATURE, Complexity.MEDIUM,
                                Collections.<RiskTag>emptyList(), 20, 2, true)
                ));

        TemplateMatchResponse response = service.matchTemplate(10L);

        assertThat(response.getAutoBound()).isTrue();
        assertThat(response.getMatchedTemplateId()).isEqualTo(40L);
        assertThat(response.getCandidates()).extracting("templateId").containsExactly(40L, 30L, 20L);
        verify(workflowTaskMapper).updateById(any(WorkflowTask.class));
    }

    private WorkflowTask task(Long id, TaskType taskType, Complexity complexity, java.util.List<RiskTag> riskTags,
                              TaskStatus status) {
        WorkflowTask task = new WorkflowTask();
        task.setId(id);
        task.setTaskType(taskType);
        task.setComplexity(complexity);
        task.setRiskTags(riskTags);
        task.setStatus(status);
        return task;
    }

    private WorkflowTemplate template(Long id, String name, TaskType taskType, Complexity complexity,
                                      java.util.List<RiskTag> riskTags, Integer priority, Integer version,
                                      Boolean enabled) {
        WorkflowTemplate template = new WorkflowTemplate();
        template.setId(id);
        template.setName(name);
        template.setTaskType(taskType);
        template.setComplexity(complexity);
        template.setRiskTags(riskTags);
        template.setPriority(priority);
        template.setVersion(version);
        template.setEnabled(enabled);
        return template;
    }
}
