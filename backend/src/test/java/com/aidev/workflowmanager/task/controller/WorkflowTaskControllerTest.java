package com.aidev.workflowmanager.task.controller;

import com.aidev.workflowmanager.common.config.WebConfig;
import com.aidev.workflowmanager.common.enums.RiskTag;
import com.aidev.workflowmanager.common.enums.TaskType;
import com.aidev.workflowmanager.common.exception.BusinessException;
import com.aidev.workflowmanager.common.exception.ErrorCode;
import com.aidev.workflowmanager.common.exception.GlobalExceptionHandler;
import com.aidev.workflowmanager.common.page.PageResult;
import com.aidev.workflowmanager.task.dto.TaskPageQuery;
import com.aidev.workflowmanager.task.service.WorkflowTaskService;
import com.aidev.workflowmanager.task.vo.TaskDetailResponse;
import com.aidev.workflowmanager.task.vo.TaskResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkflowTaskControllerTest {

    private MockMvc mockMvc;
    private WorkflowTaskService service;

    @BeforeEach
    void setUp() {
        service = mock(WorkflowTaskService.class);
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        new WebConfig().addFormatters(conversionService);
        mockMvc = MockMvcBuilders.standaloneSetup(new WorkflowTaskController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setConversionService(conversionService)
                .build();
    }

    @Test
    void pageBindsQueryEnumsAndUsesDefaultPagination() throws Exception {
        TaskResponse task = new TaskResponse();
        task.setId(1L);
        task.setTaskType(TaskType.FEATURE);
        task.setRiskTags(Collections.singletonList(RiskTag.DATABASE));
        when(service.page(any(TaskPageQuery.class)))
                .thenReturn(new PageResult<TaskResponse>(Collections.singletonList(task), 1, 1, 10));

        mockMvc.perform(get("/api/v1/tasks")
                        .param("taskType", "feature")
                        .param("riskTags", "database"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.pageNo").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.records[0].taskType").value("feature"))
                .andExpect(jsonPath("$.data.records[0].riskTags[0]").value("database"));

        ArgumentCaptor<TaskPageQuery> queryCaptor = ArgumentCaptor.forClass(TaskPageQuery.class);
        verify(service).page(queryCaptor.capture());
        TaskPageQuery query = queryCaptor.getValue();
        org.assertj.core.api.Assertions.assertThat(query.getTaskType()).isEqualTo(TaskType.FEATURE);
        org.assertj.core.api.Assertions.assertThat(query.getRiskTags()).containsExactly(RiskTag.DATABASE);
        org.assertj.core.api.Assertions.assertThat(query.normalizedPageNo()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(query.normalizedPageSize()).isEqualTo(10);
    }

    @Test
    void detailReturnsTask() throws Exception {
        TaskDetailResponse detail = new TaskDetailResponse();
        detail.setId(3L);
        detail.setRiskTags(Collections.<RiskTag>emptyList());
        when(service.detail(3L)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/tasks/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(3))
                .andExpect(jsonPath("$.data.riskTags").isArray());
    }

    @Test
    void detailReturnsBusinessNotFound() throws Exception {
        when(service.detail(404L))
                .thenThrow(new BusinessException(ErrorCode.NOT_FOUND, "任务不存在"));

        mockMvc.perform(get("/api/v1/tasks/404"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("任务不存在"));
    }

    @Test
    void createRejectsInvalidEnum() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\",\"taskType\":\"feature\",\"complexity\":\"HUGE\",\"riskTags\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("枚举值无效：Complexity=HUGE")));
    }

    @Test
    void createRejectsInvalidRiskTag() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\",\"taskType\":\"feature\",\"complexity\":\"SIMPLE\",\"riskTags\":[\"unknown\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("枚举值无效：RiskTag=unknown")));
    }

    @Test
    void createRejectsClientStatusField() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\",\"taskType\":\"feature\",\"complexity\":\"SIMPLE\",\"status\":\"DELIVERED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void createRejectsBlankAndLongTitle() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"   \",\"taskType\":\"feature\",\"complexity\":\"SIMPLE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("任务标题")));

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + repeat("a", 201) + "\",\"taskType\":\"feature\",\"complexity\":\"SIMPLE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("任务标题")));
    }

    private String repeat(String value, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(value);
        }
        return builder.toString();
    }
}
