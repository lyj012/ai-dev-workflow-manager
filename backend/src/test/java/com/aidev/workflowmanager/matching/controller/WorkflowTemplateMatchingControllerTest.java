package com.aidev.workflowmanager.matching.controller;

import com.aidev.workflowmanager.common.exception.BusinessException;
import com.aidev.workflowmanager.common.exception.ErrorCode;
import com.aidev.workflowmanager.common.exception.GlobalExceptionHandler;
import com.aidev.workflowmanager.matching.service.WorkflowTemplateMatchingService;
import com.aidev.workflowmanager.matching.vo.TemplateMatchCandidateResponse;
import com.aidev.workflowmanager.matching.vo.TemplateMatchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkflowTemplateMatchingControllerTest {

    private MockMvc mockMvc;
    private WorkflowTemplateMatchingService service;

    @BeforeEach
    void setUp() {
        service = mock(WorkflowTemplateMatchingService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new WorkflowTemplateMatchingController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void matchTemplateReturnsAutoBoundSuccess() throws Exception {
        TemplateMatchResponse response = new TemplateMatchResponse();
        response.setTaskId(1L);
        response.setMatchedTemplateId(10L);
        response.setMatchedTemplateName("简单任务 workflow");
        response.setMatchScore(70);
        response.setAutoBound(true);
        response.setMatchReasons(Collections.singletonList("Task type matched: docs"));
        response.setCandidates(Collections.singletonList(candidate(10L, "简单任务 workflow", 70, 10, 1)));
        when(service.matchTemplate(1L)).thenReturn(response);

        mockMvc.perform(post("/api/v1/tasks/1/match-template"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.taskId").value(1))
                .andExpect(jsonPath("$.data.matchedTemplateId").value(10))
                .andExpect(jsonPath("$.data.matchedTemplateName").value("简单任务 workflow"))
                .andExpect(jsonPath("$.data.autoBound").value(true))
                .andExpect(jsonPath("$.data.candidates[0].templateId").value(10));
    }

    @Test
    void matchTemplateReturnsAmbiguousSuccess() throws Exception {
        TemplateMatchResponse response = new TemplateMatchResponse();
        response.setTaskId(2L);
        response.setMatchedTemplateId(null);
        response.setMatchedTemplateName(null);
        response.setMatchScore(70);
        response.setAutoBound(false);
        response.setMatchReasons(Collections.singletonList("Multiple candidates require user selection"));
        response.setCandidates(Arrays.asList(
                candidate(20L, "standard-a", 70, 20, 1),
                candidate(21L, "standard-b", 70, 20, 1)
        ));
        when(service.matchTemplate(2L)).thenReturn(response);

        mockMvc.perform(post("/api/v1/tasks/2/match-template"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.autoBound").value(false))
                .andExpect(jsonPath("$.data.matchedTemplateId").doesNotExist())
                .andExpect(jsonPath("$.data.candidates[0].templateId").value(20))
                .andExpect(jsonPath("$.data.candidates[1].templateId").value(21));
    }

    @Test
    void matchTemplateWrapsBusinessError() throws Exception {
        when(service.matchTemplate(9L))
                .thenThrow(new BusinessException(ErrorCode.NOT_FOUND,
                        "No enabled workflow template can match task: 9"));

        mockMvc.perform(post("/api/v1/tasks/9/match-template"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("No enabled workflow template can match task: 9"));
    }

    private TemplateMatchCandidateResponse candidate(Long id, String name, Integer score, Integer priority,
                                                     Integer version) {
        TemplateMatchCandidateResponse candidate = new TemplateMatchCandidateResponse();
        candidate.setTemplateId(id);
        candidate.setTemplateName(name);
        candidate.setMatchScore(score);
        candidate.setPriority(priority);
        candidate.setVersion(version);
        candidate.setMatchReasons(Collections.singletonList("matched"));
        return candidate;
    }
}
