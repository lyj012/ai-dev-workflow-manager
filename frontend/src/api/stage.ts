import request from '@/api/request'
import type { StageInitResponse, StageOutputPayload, WorkflowStage } from '@/types/stage'

export function initializeStages(taskId: number) {
  return request.post<StageInitResponse>(`/tasks/${taskId}/stages/init`)
}

export function startStage(taskId: number, stageId: number) {
  return request.post<WorkflowStage>(`/tasks/${taskId}/stages/${stageId}/start`)
}

export function completeStage(taskId: number, stageId: number, outputSummary?: string) {
  return request.post<WorkflowStage>(`/tasks/${taskId}/stages/${stageId}/complete`, {
    outputSummary
  })
}

export function skipStage(taskId: number, stageId: number) {
  return request.post<WorkflowStage>(`/tasks/${taskId}/stages/${stageId}/skip`)
}

export function failStage(taskId: number, stageId: number, outputSummary?: string) {
  return request.post<WorkflowStage>(`/tasks/${taskId}/stages/${stageId}/fail`, {
    outputSummary
  })
}

export function saveStageOutput(taskId: number, stageId: number, data: StageOutputPayload) {
  return request.post<StageOutputPayload>(`/tasks/${taskId}/stages/${stageId}/outputs`, data)
}

export function fetchStageOutput(taskId: number, stageId: number) {
  return request.get<StageOutputPayload>(`/tasks/${taskId}/stages/${stageId}/outputs`)
}

