package com.aidev.workflowmanager.template.config;

import com.aidev.workflowmanager.template.service.WorkflowTemplateService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class WorkflowTemplateInitializer implements ApplicationRunner {

    private final WorkflowTemplateService workflowTemplateService;

    public WorkflowTemplateInitializer(WorkflowTemplateService workflowTemplateService) {
        this.workflowTemplateService = workflowTemplateService;
    }

    @Override
    public void run(ApplicationArguments args) {
        workflowTemplateService.ensureBuiltInTemplates();
    }
}
