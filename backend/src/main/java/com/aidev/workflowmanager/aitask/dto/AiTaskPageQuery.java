package com.aidev.workflowmanager.aitask.dto;

import com.aidev.workflowmanager.aitask.domain.AiTaskStatus;
import com.aidev.workflowmanager.common.page.PageQuery;

public class AiTaskPageQuery extends PageQuery {

    private AiTaskStatus status;

    public AiTaskStatus getStatus() {
        return status;
    }

    public void setStatus(AiTaskStatus status) {
        this.status = status;
    }
}
