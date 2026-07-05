import type { WorkflowStage } from '@/types/stage'
import type { TaskDetail } from '@/types/task'

export interface DeliveryRecord {
  testChecklist?: string
  testResult?: string
  riskNotes?: string
  unverifiedScope?: string
  summary?: string
  markdown?: string
}

export interface DeliveryPreview {
  task: TaskDetail
  workflowName: string
  stages: WorkflowStage[]
  record: DeliveryRecord
  markdown: string
}
