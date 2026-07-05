package com.aidev.workflowmanager.delivery.controller;

import com.aidev.workflowmanager.common.api.ApiResponse;
import com.aidev.workflowmanager.delivery.service.DeliveryService;
import com.aidev.workflowmanager.delivery.vo.DeliveryPreviewResponse;
import com.aidev.workflowmanager.delivery.vo.DeliveryRecordResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/tasks/{taskId}")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping("/generate-test-checklist")
    public ApiResponse<DeliveryRecordResponse> generateTestChecklist(@PathVariable Long taskId) {
        return ApiResponse.success(deliveryService.generateTestChecklist(taskId));
    }

    @PostMapping("/generate-delivery-summary")
    public ApiResponse<DeliveryRecordResponse> generateDeliverySummary(@PathVariable Long taskId) {
        return ApiResponse.success(deliveryService.generateDeliverySummary(taskId));
    }

    @GetMapping("/export/markdown")
    public ApiResponse<String> exportMarkdown(@PathVariable Long taskId) {
        return ApiResponse.success(deliveryService.exportMarkdown(taskId));
    }

    @GetMapping("/delivery-preview")
    public ApiResponse<DeliveryPreviewResponse> preview(@PathVariable Long taskId) {
        return ApiResponse.success(deliveryService.preview(taskId));
    }
}
