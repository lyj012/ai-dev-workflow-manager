import request from '@/api/request'
import { USE_MOCK } from '@/constants/enums'
import { mockTemplates } from '@/mocks/workflow.mock'
import type { WorkflowTemplate } from '@/types/template'

export function fetchTemplates() {
  if (USE_MOCK) return Promise.resolve(mockTemplates)
  return request.get<WorkflowTemplate[]>('/workflow-templates')
}

export function fetchTemplateDetail(templateId: number) {
  if (USE_MOCK) {
    return Promise.resolve(mockTemplates.find((item) => item.id === templateId) ?? mockTemplates[0])
  }
  return request.get<WorkflowTemplate>(`/workflow-templates/${templateId}`)
}
