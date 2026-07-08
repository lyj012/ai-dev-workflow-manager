import type { PageResult } from '@/types/common'
import type { AiTask, AiTaskLog, AiTaskQuery, AiTaskResult, CreateAiTaskPayload } from '@/types/ai-task'

const now = () => new Date().toISOString()
let nextTaskId = 4
let nextLogId = 10
let nextResultId = 3

export const mockAiTasks: AiTask[] = [
  {
    id: 1,
    title: '分析会员下载失败问题',
    requirement: '用户反馈会员下载按钮点击后没有文件返回，需要分析可能原因并给出排查步骤。',
    taskType: 'requirement_analysis',
    remark: '用于演示成功任务结果。',
    status: 'SUCCESS',
    retryCount: 0,
    errorMessage: null,
    createdAt: '2026-07-08T16:40:00',
    updatedAt: '2026-07-08T16:40:04'
  },
  {
    id: 2,
    title: '生成异步任务模块开发计划',
    requirement: '围绕 AI 异步任务平台 P0，拆分前端架子、后端接口、状态机和联调步骤。',
    taskType: 'development_plan',
    remark: '模拟分析中任务。',
    status: 'ANALYZING',
    retryCount: 0,
    errorMessage: null,
    createdAt: '2026-07-08T16:50:00',
    updatedAt: '2026-07-08T16:50:01'
  },
  {
    id: 3,
    title: '模拟 AI 分析失败任务',
    requirement: '输入内容缺少必要上下文，模拟执行失败后允许用户重试。',
    taskType: 'risk_review',
    remark: '用于验证失败重试入口。',
    status: 'FAILED',
    retryCount: 1,
    errorMessage: '模拟分析失败：需求描述缺少可分析上下文。',
    createdAt: '2026-07-08T16:55:00',
    updatedAt: '2026-07-08T16:55:04'
  }
]

const mockLogs: AiTaskLog[] = [
  { id: 1, taskId: 1, logLevel: 'INFO', logNode: 'TASK_CREATED', message: '任务已创建。', createdAt: '2026-07-08T16:40:00' },
  { id: 2, taskId: 1, logLevel: 'INFO', logNode: 'TASK_ANALYZING', message: '开始模拟 AI 分析。', createdAt: '2026-07-08T16:40:01' },
  { id: 3, taskId: 1, logLevel: 'INFO', logNode: 'TASK_SUCCESS', message: '模拟分析完成。', createdAt: '2026-07-08T16:40:04' },
  { id: 4, taskId: 2, logLevel: 'INFO', logNode: 'TASK_CREATED', message: '任务已创建。', createdAt: '2026-07-08T16:50:00' },
  { id: 5, taskId: 2, logLevel: 'INFO', logNode: 'TASK_ANALYZING', message: '开始模拟 AI 分析。', createdAt: '2026-07-08T16:50:01' },
  { id: 6, taskId: 3, logLevel: 'INFO', logNode: 'TASK_CREATED', message: '任务已创建。', createdAt: '2026-07-08T16:55:00' },
  { id: 7, taskId: 3, logLevel: 'INFO', logNode: 'TASK_ANALYZING', message: '开始模拟 AI 分析。', createdAt: '2026-07-08T16:55:01' },
  { id: 8, taskId: 3, logLevel: 'ERROR', logNode: 'TASK_FAILED', message: '模拟分析失败：需求描述缺少可分析上下文。', createdAt: '2026-07-08T16:55:04' }
]

const mockResults: AiTaskResult[] = [
  {
    id: 1,
    taskId: 1,
    summary: '会员下载失败需要优先确认接口返回、文件流处理和前端 Blob 错误分支。',
    riskPoints: '下载权限、文件流异常、后端错误被前端当作 Blob 处理。',
    suggestedSteps: '1. 复现下载请求；\n2. 查看响应头和状态码；\n3. 检查 Blob 错误解析；\n4. 验证权限失败提示。',
    testSuggestions: '覆盖成功下载、权限失败、文件不存在、网络异常和重复点击。',
    rawResult: 'mock ai result',
    createdAt: '2026-07-08T16:40:04',
    updatedAt: '2026-07-08T16:40:04'
  }
]

export function queryMockAiTasks(query: AiTaskQuery): PageResult<AiTask> {
  const filtered = mockAiTasks.filter((task) => !query.status || task.status === query.status)
  const start = (query.pageNo - 1) * query.pageSize
  return {
    records: filtered.slice(start, start + query.pageSize),
    total: filtered.length,
    pageNo: query.pageNo,
    pageSize: query.pageSize
  }
}

export function getMockAiTask(taskId: number) {
  return mockAiTasks.find((task) => task.id === taskId) ?? mockAiTasks[0]
}

export function getMockAiTaskLogs(taskId: number) {
  return mockLogs.filter((log) => log.taskId === taskId).sort((a, b) => a.createdAt.localeCompare(b.createdAt))
}

export function getMockAiTaskResult(taskId: number) {
  return mockResults.find((result) => result.taskId === taskId) ?? null
}

export function createMockAiTask(payload: CreateAiTaskPayload) {
  const task: AiTask = {
    id: nextTaskId++,
    title: payload.title,
    requirement: payload.requirement,
    taskType: payload.taskType,
    remark: payload.remark,
    status: 'CREATED',
    retryCount: 0,
    errorMessage: null,
    createdAt: now(),
    updatedAt: now()
  }
  mockAiTasks.unshift(task)
  appendLog(task.id, 'INFO', 'TASK_CREATED', '任务已创建。')
  scheduleMockExecution(task.id)
  return task
}

export function retryMockAiTask(taskId: number) {
  const task = getMockAiTask(taskId)
  if (task.status !== 'FAILED') return task
  task.retryCount += 1
  task.errorMessage = null
  task.status = 'CREATED'
  task.updatedAt = now()
  appendLog(task.id, 'INFO', 'TASK_RETRY', '用户触发失败任务重试。')
  scheduleMockExecution(task.id)
  return task
}

function scheduleMockExecution(taskId: number) {
  setTimeout(() => {
    const task = getMockAiTask(taskId)
    if (task.status !== 'CREATED') return
    task.status = 'ANALYZING'
    task.updatedAt = now()
    appendLog(task.id, 'INFO', 'TASK_ANALYZING', '开始模拟 AI 分析。')
  }, 300)

  setTimeout(() => {
    const task = getMockAiTask(taskId)
    if (task.status !== 'ANALYZING') return
    task.status = 'SUCCESS'
    task.updatedAt = now()
    upsertMockResult(task)
    appendLog(task.id, 'INFO', 'TASK_SUCCESS', '模拟分析完成。')
  }, 3300)
}

function appendLog(taskId: number, logLevel: AiTaskLog['logLevel'], logNode: string, message: string) {
  mockLogs.push({ id: nextLogId++, taskId, logLevel, logNode, message, createdAt: now() })
}

function upsertMockResult(task: AiTask) {
  const result = getMockAiTaskResult(task.id)
  const data = {
    taskId: task.id,
    summary: `已完成对「${task.title}」的模拟分析，建议先确认需求边界，再拆分执行和验证步骤。`,
    riskPoints: '当前为 mock 分析结果，真实风险需要后续接入 AI 或人工补充。',
    suggestedSteps: '1. 明确输入需求；\n2. 拆分执行步骤；\n3. 记录关键日志；\n4. 输出验证建议。',
    testSuggestions: '建议覆盖任务创建、状态刷新、日志展示、结果展示和失败重试。',
    rawResult: 'mock ai result',
    updatedAt: now()
  }
  if (result) {
    Object.assign(result, data)
  } else {
    mockResults.push({ id: nextResultId++, ...data, createdAt: now() })
  }
}
