package com.aidev.workflowmanager.delivery.service;

import com.aidev.workflowmanager.delivery.vo.DeliveryPreviewResponse;
import com.aidev.workflowmanager.delivery.vo.DeliveryRecordResponse;

public interface DeliveryService {

    DeliveryRecordResponse generateTestChecklist(Long taskId);

    DeliveryRecordResponse generateDeliverySummary(Long taskId);

    String exportMarkdown(Long taskId);

    DeliveryPreviewResponse preview(Long taskId);
}
