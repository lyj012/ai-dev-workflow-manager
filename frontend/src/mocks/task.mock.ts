import type { PageResult } from '@/types/common'
import type { CreateTaskPayload, Task, TaskDetail, TaskQuery } from '@/types/task'
import { mockStages } from '@/mocks/stage.mock'

export const mockTasks: Task[] = [
  {
    id: 1,
    title: '搭建前端项目架构和完整页面骨架',
    description: '按 docs/14 搭建完整路由、布局、页面、组件和 mock 数据。',
    taskType: 'feature',
    complexity: 'MEDIUM',
    riskTags: ['config'],
    status: 'EXECUTING',
    matchedTemplateId: 2,
    matchedTemplateName: '标准开发 Workflow',
    testChecklistGenerated: true,
    createdAt: '2026-07-05T10:00:00',
    updatedAt: '2026-07-05T11:30:00'
  },
  {
    id: 2,
    title: '补齐高风险阶段输出字段',
    description: '验证权限、数据库、回调等高风险字段在交付记录中可追踪。',
    taskType: 'review',
    complexity: 'COMPLEX',
    riskTags: ['permission', 'database', 'callback'],
    status: 'TESTING',
    matchedTemplateId: 3,
    matchedTemplateName: '高风险 Workflow',
    testChecklistGenerated: true,
    createdAt: '2026-07-04T16:10:00',
    updatedAt: '2026-07-05T09:40:00'
  },
  {
    id: 3,
    title: '整理项目 README 展示文案',
    description: '补充 MVP 定位和演示说明。',
    taskType: 'docs',
    complexity: 'SIMPLE',
    riskTags: [],
    status: 'DRAFT',
    matchedTemplateId: 1,
    matchedTemplateName: '简单任务 Workflow',
    testChecklistGenerated: false,
    createdAt: '2026-07-03T14:20:00',
    updatedAt: '2026-07-03T18:05:00'
  }
]

export function queryMockTasks(query: TaskQuery): PageResult<Task> {
  const filtered = mockTasks.filter((task) => {
    return (
      (!query.status || task.status === query.status) &&
      (!query.taskType || task.taskType === query.taskType) &&
      (!query.complexity || task.complexity === query.complexity) &&
      (!query.riskTag || task.riskTags.includes(query.riskTag))
    )
  })
  const start = (query.pageNo - 1) * query.pageSize
  return {
    records: filtered.slice(start, start + query.pageSize),
    total: filtered.length,
    pageNo: query.pageNo,
    pageSize: query.pageSize
  }
}

export function getMockTaskDetail(taskId: number): TaskDetail & { stages: typeof mockStages } {
  const task = mockTasks.find((item) => item.id === taskId) ?? mockTasks[0]
  return {
    ...task,
    deliveryRecordId: 1,
    stages: mockStages.map((stage) => ({ ...stage, taskId: task.id }))
  }
}

export function createMockTask(payload: CreateTaskPayload): Task {
  const task: Task = {
    id: Date.now(),
    title: payload.title,
    description: payload.description,
    taskType: payload.taskType,
    complexity: payload.complexity,
    riskTags: payload.riskTags,
    status: 'DRAFT',
    matchedTemplateId: null,
    matchedTemplateName: null,
    testChecklistGenerated: false,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }
  mockTasks.unshift(task)
  return task
}
