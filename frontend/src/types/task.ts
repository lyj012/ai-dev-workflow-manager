export type TaskType = 'feature' | 'bugfix' | 'refactor' | 'review' | 'docs'
export type Complexity = 'SIMPLE' | 'MEDIUM' | 'COMPLEX'
export type RiskTag =
  | 'payment'
  | 'permission'
  | 'database'
  | 'config'
  | 'file'
  | 'auth'
  | 'download'
  | 'amount'
  | 'callback'

export type TaskStatus =
  | 'DRAFT'
  | 'ANALYZING'
  | 'EXECUTING'
  | 'TESTING'
  | 'DELIVERED'
  | 'ARCHIVED'
  | 'CANCELED'

export interface Task {
  id: number
  title: string
  description: string
  taskType: TaskType
  complexity: Complexity
  riskTags: RiskTag[]
  status: TaskStatus
  matchedTemplateId?: number | null
  matchedTemplateName?: string | null
  testChecklistGenerated?: boolean | null
  createdAt: string
  updatedAt: string
}

export interface TaskDetail extends Task {
  deliveryRecordId?: number | null
}

export interface TaskQuery {
  pageNo: number
  pageSize: number
  status?: TaskStatus
  taskType?: TaskType
  complexity?: Complexity
  riskTag?: RiskTag
}

export interface CreateTaskPayload {
  title: string
  description: string
  taskType: TaskType
  complexity: Complexity
  riskTags: RiskTag[]
}
