export type AiTaskStatus = 'CREATED' | 'ANALYZING' | 'SUCCESS' | 'FAILED'
export type AiTaskType = 'requirement_analysis' | 'development_plan' | 'risk_review'
export type AiTaskLogLevel = 'INFO' | 'WARN' | 'ERROR'

export interface AiTask {
  id: number
  title: string
  requirement: string
  taskType?: AiTaskType | null
  remark?: string | null
  status: AiTaskStatus
  retryCount: number
  errorMessage?: string | null
  createdAt: string
  updatedAt: string
}

export interface AiTaskLog {
  id: number
  taskId: number
  logLevel: AiTaskLogLevel
  logNode: string
  message: string
  createdAt: string
}

export interface AiTaskResult {
  id: number
  taskId: number
  summary: string
  riskPoints: string
  suggestedSteps: string
  testSuggestions: string
  rawResult?: string | null
  createdAt: string
  updatedAt: string
}

export interface CreateAiTaskPayload {
  title: string
  requirement: string
  taskType?: AiTaskType | null
  remark?: string | null
}

export interface AiTaskQuery {
  pageNo: number
  pageSize: number
  status?: AiTaskStatus
}
