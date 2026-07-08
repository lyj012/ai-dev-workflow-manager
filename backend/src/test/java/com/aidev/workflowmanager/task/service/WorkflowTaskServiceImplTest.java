package com.aidev.workflowmanager.task.service;

import com.aidev.workflowmanager.common.enums.Complexity;
import com.aidev.workflowmanager.common.enums.RiskTag;
import com.aidev.workflowmanager.common.enums.TaskStatus;
import com.aidev.workflowmanager.common.enums.TaskType;
import com.aidev.workflowmanager.common.exception.BusinessException;
import com.aidev.workflowmanager.common.page.PageResult;
import com.aidev.workflowmanager.stage.mapper.WorkflowStageMapper;
import com.aidev.workflowmanager.task.dto.CreateTaskRequest;
import com.aidev.workflowmanager.task.dto.TaskPageQuery;
import com.aidev.workflowmanager.task.entity.WorkflowTask;
import com.aidev.workflowmanager.task.mapper.WorkflowTaskMapper;
import com.aidev.workflowmanager.task.service.impl.WorkflowTaskServiceImpl;
import com.aidev.workflowmanager.task.vo.TaskDetailResponse;
import com.aidev.workflowmanager.task.vo.TaskResponse;
import com.aidev.workflowmanager.template.mapper.WorkflowTemplateMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowTaskServiceImplTest {

    @Mock
    private WorkflowTaskMapper workflowTaskMapper;

    @Mock
    private WorkflowTemplateMapper workflowTemplateMapper;

    @Mock
    private WorkflowStageMapper workflowStageMapper;

    private WorkflowTaskServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WorkflowTaskServiceImpl(workflowTaskMapper, workflowTemplateMapper, workflowStageMapper);
    }

    @Test
    void createForcesDraftAndIgnoresAnyExternalStatusPath() {
        ArgumentCaptor<WorkflowTask> captor = ArgumentCaptor.forClass(WorkflowTask.class);
        when(workflowTaskMapper.insert(captor.capture())).thenAnswer(invocation -> {
            WorkflowTask task = invocation.getArgument(0);
            task.setId(10L);
            return 1;
        });

        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("  Build backend  ");
        request.setDescription("Phase 1");
        request.setTaskType(TaskType.FEATURE);
        request.setComplexity(Complexity.MEDIUM);
        request.setRiskTags(Arrays.asList(RiskTag.DATABASE, RiskTag.DATABASE, RiskTag.AUTH));

        TaskResponse response = service.create(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo(TaskStatus.DRAFT);
        assertThat(response.getRiskTags()).containsExactly(RiskTag.DATABASE, RiskTag.AUTH);
        assertThat(captor.getValue().getTitle()).isEqualTo("Build backend");
        assertThat(captor.getValue().getStatus()).isEqualTo(TaskStatus.DRAFT);
        assertThat(captor.getValue().getTestChecklistGenerated()).isFalse();
        assertThat(captor.getValue().getDeleted()).isZero();
    }

    @Test
    void pageReturnsTaskRecordsAndPageMetadata() {
        WorkflowTask task = sampleTask(1L);
        Page<WorkflowTask> mapperPage = new Page<WorkflowTask>(2, 5);
        mapperPage.setRecords(Collections.singletonList(task));
        mapperPage.setTotal(11);
        when(workflowTaskMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(mapperPage);

        TaskPageQuery query = new TaskPageQuery();
        query.setPageNo(2L);
        query.setPageSize(5L);
        query.setStatus(TaskStatus.DRAFT);
        query.setTaskType(TaskType.FEATURE);
        query.setComplexity(Complexity.MEDIUM);
        query.setRiskTags(Collections.singletonList(RiskTag.DATABASE));

        PageResult<TaskResponse> result = service.page(query);

        assertThat(result.getTotal()).isEqualTo(11);
        assertThat(result.getPageNo()).isEqualTo(2);
        assertThat(result.getPageSize()).isEqualTo(5);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void detailReturnsPhaseOneFields() {
        WorkflowTask task = sampleTask(3L);
        task.setMatchedTemplateId(20L);
        task.setDeliveryRecordId(30L);
        task.setTestChecklistGenerated(true);
        when(workflowTaskMapper.selectOne(any(Wrapper.class))).thenReturn(task);
        when(workflowStageMapper.selectList(any(Wrapper.class))).thenReturn(Collections.emptyList());

        TaskDetailResponse response = service.detail(3L);

        assertThat(response.getId()).isEqualTo(3L);
        assertThat(response.getMatchedTemplateId()).isEqualTo(20L);
        assertThat(response.getDeliveryRecordId()).isEqualTo(30L);
        assertThat(response.getTestChecklistGenerated()).isTrue();
    }

    @Test
    void detailReturnsEmptyRiskTagsForLegacyNullValue() {
        WorkflowTask task = sampleTask(4L);
        task.setRiskTags(null);
        when(workflowTaskMapper.selectOne(any(Wrapper.class))).thenReturn(task);
        when(workflowStageMapper.selectList(any(Wrapper.class))).thenReturn(Collections.emptyList());

        TaskDetailResponse response = service.detail(4L);

        assertThat(response.getRiskTags()).isEmpty();
    }

    @Test
    void detailRejectsMissingTask() {
        when(workflowTaskMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        assertThatThrownBy(() -> service.detail(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("任务不存在");
    }

    @Test
    void titleValidationRejectsBlankAndLongTitle() {
        CreateTaskRequest blank = validCreateRequest();
        blank.setTitle("   ");
        assertThatThrownBy(() -> service.create(blank)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("任务标题不能为空");

        CreateTaskRequest tooLong = validCreateRequest();
        tooLong.setTitle(repeat("a", 201));
        assertThatThrownBy(() -> service.create(tooLong)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("任务标题长度不能超过");
    }

    @Test
    void pageValidationRejectsInvalidPageInput() {
        TaskPageQuery zeroPage = new TaskPageQuery();
        zeroPage.setPageNo(0L);
        assertThatThrownBy(() -> service.page(zeroPage)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("页码必须大于等于 1");

        TaskPageQuery tooLarge = new TaskPageQuery();
        tooLarge.setPageSize(101L);
        assertThatThrownBy(() -> service.page(tooLarge)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("每页数量必须在 1 到 100 之间");
    }

    private CreateTaskRequest validCreateRequest() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Valid title");
        request.setTaskType(TaskType.FEATURE);
        request.setComplexity(Complexity.SIMPLE);
        return request;
    }

    private WorkflowTask sampleTask(Long id) {
        WorkflowTask task = new WorkflowTask();
        task.setId(id);
        task.setTitle("Task " + id);
        task.setDescription("desc");
        task.setTaskType(TaskType.FEATURE);
        task.setComplexity(Complexity.MEDIUM);
        task.setRiskTags(Collections.singletonList(RiskTag.DATABASE));
        task.setStatus(TaskStatus.DRAFT);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setTestChecklistGenerated(false);
        return task;
    }

    private String repeat(String value, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(value);
        }
        return builder.toString();
    }
}
