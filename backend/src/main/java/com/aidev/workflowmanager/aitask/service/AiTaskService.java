package com.aidev.workflowmanager.aitask.service;

import com.aidev.workflowmanager.aitask.dto.AiTaskPageQuery;
import com.aidev.workflowmanager.aitask.dto.CreateAiTaskRequest;
import com.aidev.workflowmanager.aitask.vo.AiTaskLogResponse;
import com.aidev.workflowmanager.aitask.vo.AiTaskResponse;
import com.aidev.workflowmanager.aitask.vo.AiTaskResultResponse;
import com.aidev.workflowmanager.common.page.PageResult;

import java.util.List;

public interface AiTaskService {

    AiTaskResponse create(CreateAiTaskRequest request);

    PageResult<AiTaskResponse> page(AiTaskPageQuery query);

    AiTaskResponse detail(Long taskId);

    List<AiTaskLogResponse> logs(Long taskId);

    AiTaskResultResponse result(Long taskId);

    AiTaskResponse retry(Long taskId);
}
