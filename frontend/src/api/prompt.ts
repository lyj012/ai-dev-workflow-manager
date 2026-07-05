import request from '@/api/request'
import type { GeneratePromptResponse } from '@/types/prompt'

export function generatePrompt(taskId: number, stageId: number) {
  return request.post<GeneratePromptResponse>(`/tasks/${taskId}/stages/${stageId}/generate-prompt`)
}

