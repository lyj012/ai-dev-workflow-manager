import type { Complexity, RiskTag, TaskType } from '@/types/task'

export interface TemplateCandidate {
  templateId: number
  templateName: string
  score: number
  reason: string
}

export interface MatchTemplateResponse {
  matchedTemplateId?: number
  matchedTemplateName?: string
  score?: number
  reason?: string
  autoBound: boolean
  candidates: TemplateCandidate[]
}

export interface WorkflowTemplateStage {
  id: number
  templateId: number
  stageOrder: number
  stageKey: string
  stageName: string
  required: boolean
  stageGoal: string
}

export interface WorkflowTemplate {
  id: number
  name: string
  description: string
  applicableTaskTypes: TaskType[]
  applicableComplexities: Complexity[]
  riskTags: RiskTag[]
  priority: number
  version: string
  enabled: boolean
  stages: WorkflowTemplateStage[]
}
