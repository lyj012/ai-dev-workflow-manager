package com.aidev.workflowmanager.template.service;

import com.aidev.workflowmanager.template.vo.WorkflowTemplateResponse;

import java.util.List;

public interface WorkflowTemplateService {

    void ensureBuiltInTemplates();

    List<WorkflowTemplateResponse> listEnabledTemplates();

    WorkflowTemplateResponse detail(Long templateId);
}
