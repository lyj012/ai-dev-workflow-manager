package com.aidev.workflowmanager.task.service;

import com.aidev.workflowmanager.common.page.PageResult;
import com.aidev.workflowmanager.task.dto.CreateTaskRequest;
import com.aidev.workflowmanager.task.dto.TaskPageQuery;
import com.aidev.workflowmanager.task.vo.TaskDetailResponse;
import com.aidev.workflowmanager.task.vo.TaskResponse;

public interface WorkflowTaskService {

    TaskResponse create(CreateTaskRequest request);

    PageResult<TaskResponse> page(TaskPageQuery query);

    TaskDetailResponse detail(Long taskId);
}
