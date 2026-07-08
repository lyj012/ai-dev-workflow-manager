import request from '@/api/request'
import { USE_MOCK } from '@/constants/enums'
import {
  createMockAiTask,
  getMockAiTask,
  getMockAiTaskLogs,
  getMockAiTaskResult,
  queryMockAiTasks,
  retryMockAiTask
} from '@/mocks/ai-task.mock'
import type { PageResult } from '@/types/common'
import type { AiTask, AiTaskLog, AiTaskQuery, AiTaskResult, CreateAiTaskPayload } from '@/types/ai-task'

export function fetchAiTasks(params: AiTaskQuery) {
  if (USE_MOCK) return Promise.resolve(queryMockAiTasks(params))
  return request.get<PageResult<AiTask>>('/ai-tasks', { params })
}

export function createAiTask(data: CreateAiTaskPayload) {
  if (USE_MOCK) return Promise.resolve(createMockAiTask(data))
  return request.post<AiTask>('/ai-tasks', data)
}

export function fetchAiTaskDetail(taskId: number) {
  if (USE_MOCK) return Promise.resolve(getMockAiTask(taskId))
  return request.get<AiTask>(`/ai-tasks/${taskId}`)
}

export function fetchAiTaskLogs(taskId: number) {
  if (USE_MOCK) return Promise.resolve(getMockAiTaskLogs(taskId))
  return request.get<AiTaskLog[]>(`/ai-tasks/${taskId}/logs`)
}

export function fetchAiTaskResult(taskId: number) {
  if (USE_MOCK) return Promise.resolve(getMockAiTaskResult(taskId))
  return request.get<AiTaskResult | null>(`/ai-tasks/${taskId}/result`)
}

export function retryAiTask(taskId: number) {
  if (USE_MOCK) return Promise.resolve(retryMockAiTask(taskId))
  return request.post<AiTask>(`/ai-tasks/${taskId}/retry`)
}
