import request from '@/api/request'
import type { MatchTemplateResponse } from '@/types/template'

export function matchTemplate(taskId: number) {
  return request.post<MatchTemplateResponse>(`/tasks/${taskId}/match-template`)
}

