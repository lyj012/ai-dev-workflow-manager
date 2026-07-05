import request from '@/api/request'
import { USE_MOCK } from '@/constants/enums'
import { createMockTask, getMockTaskDetail, queryMockTasks } from '@/mocks/task.mock'
import type { PageResult } from '@/types/common'
import type { CreateTaskPayload, Task, TaskDetail, TaskQuery } from '@/types/task'

export function fetchTasks(params: TaskQuery) {
  if (USE_MOCK) return Promise.resolve(queryMockTasks(params))
  return request.get<PageResult<Task>>('/tasks', { params })
}

export function createTask(data: CreateTaskPayload) {
  if (USE_MOCK) return Promise.resolve(createMockTask(data))
  return request.post<Task>('/tasks', data)
}

export function fetchTaskDetail(taskId: number) {
  if (USE_MOCK) return Promise.resolve(getMockTaskDetail(taskId))
  return request.get<TaskDetail>(`/tasks/${taskId}`)
}
