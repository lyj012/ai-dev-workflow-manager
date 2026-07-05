import request from '@/api/request'
import { USE_MOCK } from '@/constants/enums'
import { getMockTaskDetail } from '@/mocks/task.mock'
import type { StageInitResponse, StageOutputPayload, WorkflowStage } from '@/types/stage'

export function initializeStages(taskId: number) {
  if (USE_MOCK) {
    const detail = getMockTaskDetail(taskId)
    return Promise.resolve({
      taskId,
      matchedTemplateId: detail.matchedTemplateId ?? 2,
      stages: detail.stages
    })
  }
  return request.post<StageInitResponse>(`/tasks/${taskId}/stages/init`)
}

export function startStage(taskId: number, stageId: number) {
  if (USE_MOCK) return Promise.resolve(updateMockStage(taskId, stageId, 'RUNNING'))
  return request.post<WorkflowStage>(`/tasks/${taskId}/stages/${stageId}/start`)
}

export function completeStage(taskId: number, stageId: number, outputSummary?: string) {
  if (USE_MOCK) return Promise.resolve({ ...updateMockStage(taskId, stageId, 'COMPLETED'), outputSummary })
  return request.post<WorkflowStage>(`/tasks/${taskId}/stages/${stageId}/complete`, {
    outputSummary
  })
}

export function skipStage(taskId: number, stageId: number) {
  if (USE_MOCK) return Promise.resolve(updateMockStage(taskId, stageId, 'SKIPPED'))
  return request.post<WorkflowStage>(`/tasks/${taskId}/stages/${stageId}/skip`)
}

export function failStage(taskId: number, stageId: number, outputSummary?: string) {
  if (USE_MOCK) return Promise.resolve({ ...updateMockStage(taskId, stageId, 'FAILED'), outputSummary })
  return request.post<WorkflowStage>(`/tasks/${taskId}/stages/${stageId}/fail`, {
    outputSummary
  })
}

export function saveStageOutput(taskId: number, stageId: number, data: StageOutputPayload) {
  if (USE_MOCK) return Promise.resolve(data)
  return request.post<StageOutputPayload>(`/tasks/${taskId}/stages/${stageId}/outputs`, data)
}

export function fetchStageOutput(taskId: number, stageId: number) {
  if (USE_MOCK) {
    const stage = getMockTaskDetail(taskId).stages.find((item) => item.id === stageId)
    return Promise.resolve({
      outputSummary: stage?.outputSummary ?? '',
      riskPoints: stage?.riskPoints ?? '',
      nextActions: stage?.nextActions ?? '',
      unverifiedScope: stage?.unverifiedScope ?? ''
    })
  }
  return request.get<StageOutputPayload>(`/tasks/${taskId}/stages/${stageId}/outputs`)
}

function updateMockStage(taskId: number, stageId: number, status: WorkflowStage['status']) {
  const stages = getMockTaskDetail(taskId).stages
  const stage = stages.find((item) => item.id === stageId) ?? stages[0]
  return {
    ...stage,
    status,
    startedAt: status === 'RUNNING' ? new Date().toISOString() : stage.startedAt,
    completedAt: ['COMPLETED', 'SKIPPED', 'FAILED'].includes(status) ? new Date().toISOString() : stage.completedAt
  }
}
