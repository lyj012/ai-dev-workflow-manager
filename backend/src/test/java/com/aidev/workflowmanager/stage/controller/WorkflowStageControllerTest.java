package com.aidev.workflowmanager.stage.controller;

import com.aidev.workflowmanager.common.enums.StageStatus;
import com.aidev.workflowmanager.common.exception.BusinessException;
import com.aidev.workflowmanager.common.exception.ErrorCode;
import com.aidev.workflowmanager.common.exception.GlobalExceptionHandler;
import com.aidev.workflowmanager.stage.service.WorkflowStageService;
import com.aidev.workflowmanager.stage.vo.StageInitResponse;
import com.aidev.workflowmanager.stage.vo.WorkflowStageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkflowStageControllerTest {

    private MockMvc mockMvc;
    private WorkflowStageService service;

    @BeforeEach
    void setUp() {
        service = mock(WorkflowStageService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new WorkflowStageController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void initializeStagesReturnsSuccess() throws Exception {
        StageInitResponse response = new StageInitResponse();
        response.setTaskId(1L);
        response.setMatchedTemplateId(20L);
        WorkflowStageResponse stage = new WorkflowStageResponse();
        stage.setId(100L);
        stage.setTaskId(1L);
        stage.setTemplateStageId(200L);
        stage.setStageKey("analysis");
        stage.setStageName("需求分析");
        stage.setStageOrder(1);
        stage.setStatus(StageStatus.PENDING);
        response.setStages(Collections.singletonList(stage));
        when(service.initializeStages(1L)).thenReturn(response);

        mockMvc.perform(post("/api/v1/tasks/1/stages/init"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.taskId").value(1))
                .andExpect(jsonPath("$.data.matchedTemplateId").value(20))
                .andExpect(jsonPath("$.data.stages[0].stageKey").value("analysis"))
                .andExpect(jsonPath("$.data.stages[0].status").value("PENDING"));
    }

    @Test
    void initializeStagesWrapsBusinessError() throws Exception {
        when(service.initializeStages(9L))
                .thenThrow(new BusinessException(ErrorCode.INVALID_PARAM, "任务还没有匹配 workflow 模板。"));

        mockMvc.perform(post("/api/v1/tasks/9/stages/init"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("任务还没有匹配 workflow 模板。"));
    }
}
