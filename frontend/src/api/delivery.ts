import request from '@/api/request'
import type { DeliveryRecord } from '@/types/delivery'

export function generateTestChecklist(taskId: number) {
  return request.post<DeliveryRecord>(`/tasks/${taskId}/generate-test-checklist`)
}

export function generateDeliverySummary(taskId: number) {
  return request.post<DeliveryRecord>(`/tasks/${taskId}/generate-delivery-summary`)
}

export function exportMarkdown(taskId: number) {
  return request.get<string>(`/tasks/${taskId}/export/markdown`)
}

