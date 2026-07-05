import request from '@/api/request'
import { USE_MOCK } from '@/constants/enums'
import { mockDeliveryRecord, mockMarkdown } from '@/mocks/delivery.mock'
import { getMockTaskDetail } from '@/mocks/task.mock'
import type { DeliveryPreview, DeliveryRecord } from '@/types/delivery'

export function generateTestChecklist(taskId: number) {
  if (USE_MOCK) return Promise.resolve(mockDeliveryRecord)
  return request.post<DeliveryRecord>(`/tasks/${taskId}/generate-test-checklist`)
}

export function generateDeliverySummary(taskId: number) {
  if (USE_MOCK) return Promise.resolve(mockDeliveryRecord)
  return request.post<DeliveryRecord>(`/tasks/${taskId}/generate-delivery-summary`)
}

export function exportMarkdown(taskId: number) {
  if (USE_MOCK) return Promise.resolve(mockMarkdown)
  return request.get<string>(`/tasks/${taskId}/export/markdown`)
}

export function fetchDeliveryPreview(taskId: number) {
  if (USE_MOCK) {
    const task = getMockTaskDetail(taskId)
    return Promise.resolve<DeliveryPreview>({
      task,
      workflowName: task.matchedTemplateName ?? '标准开发 Workflow',
      stages: task.stages,
      record: mockDeliveryRecord,
      markdown: mockMarkdown
    })
  }
  return request.get<DeliveryPreview>(`/tasks/${taskId}/delivery-preview`)
}
