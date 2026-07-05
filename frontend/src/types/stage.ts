export type StageStatus = 'PENDING' | 'RUNNING' | 'COMPLETED' | 'SKIPPED' | 'FAILED'

export interface WorkflowStage {
  id: number
  taskId: number
  templateStageId: number
  stageKey: string
  stageName: string
  stageGoal?: string
  required?: boolean
  stageOrder: number
  status: StageStatus
  startedAt?: string | null
  completedAt?: string | null
  outputSummary?: string
  riskPoints?: string
  nextActions?: string
  unverifiedScope?: string
}

export interface StageInitResponse {
  taskId: number
  matchedTemplateId: number
  stages: WorkflowStage[]
}

export interface StageOutputPayload {
  outputSummary: string
  riskPoints: string
  nextActions: string
  unverifiedScope: string
}
