import request from '@/api/request'
import { USE_MOCK } from '@/constants/enums'
import { mockTemplates } from '@/mocks/workflow.mock'
import type { MatchTemplateResponse } from '@/types/template'

export function matchTemplate(taskId: number) {
  if (USE_MOCK) {
    return Promise.resolve<MatchTemplateResponse>({
      matchedTemplateId: 2,
      matchedTemplateName: '标准开发 Workflow',
      score: 86,
      reason: `任务 ${taskId} 为中等复杂度开发任务，优先匹配标准开发流程。`,
      autoBound: true,
      candidates: mockTemplates.map((item) => ({
        templateId: item.id,
        templateName: item.name,
        score: item.id === 2 ? 86 : item.id === 3 ? 72 : 54,
        reason: item.description
      }))
    })
  }
  return request.post<MatchTemplateResponse>(`/tasks/${taskId}/match-template`)
}
