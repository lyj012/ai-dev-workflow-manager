import request from '@/api/request'
import type { PageResult } from '@/types/common'
import type { CreateTaskPayload, Task, TaskDetail, TaskQuery } from '@/types/task'

export function fetchTasks(params: TaskQuery) {
  return request.get<PageResult<Task>>('/tasks', { params })
}

export function createTask(data: CreateTaskPayload) {
  return request.post<Task>('/tasks', data)
}

export function fetchTaskDetail(taskId: number) {
  return request.get<TaskDetail>(`/tasks/${taskId}`)
}

