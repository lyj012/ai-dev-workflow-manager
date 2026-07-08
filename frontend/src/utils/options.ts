import type { Complexity, RiskTag, TaskStatus, TaskType } from '@/types/task'
import type { StageStatus } from '@/types/stage'
import type { AiTaskStatus, AiTaskType } from '@/types/ai-task'

export const taskTypeOptions: Array<{ label: string; value: TaskType }> = [
  { label: '功能开发', value: 'feature' },
  { label: '缺陷修复', value: 'bugfix' },
  { label: '重构优化', value: 'refactor' },
  { label: '代码审查', value: 'review' },
  { label: '文档任务', value: 'docs' }
]

export const complexityOptions: Array<{ label: string; value: Complexity }> = [
  { label: '简单', value: 'SIMPLE' },
  { label: '中等', value: 'MEDIUM' },
  { label: '复杂', value: 'COMPLEX' }
]

export const riskTagOptions: Array<{ label: string; value: RiskTag }> = [
  { label: '支付', value: 'payment' },
  { label: '权限', value: 'permission' },
  { label: '数据库', value: 'database' },
  { label: '配置', value: 'config' },
  { label: '文件处理', value: 'file' },
  { label: '登录认证', value: 'auth' },
  { label: '下载权限', value: 'download' },
  { label: '金额计算', value: 'amount' },
  { label: '第三方回调', value: 'callback' }
]

export const taskStatusOptions: Array<{ label: string; value: TaskStatus }> = [
  { label: '草稿', value: 'DRAFT' },
  { label: '分析中', value: 'ANALYZING' },
  { label: '执行中', value: 'EXECUTING' },
  { label: '测试中', value: 'TESTING' },
  { label: '已交付', value: 'DELIVERED' },
  { label: '已归档', value: 'ARCHIVED' },
  { label: '已取消', value: 'CANCELED' }
]

export const stageStatusOptions: Array<{ label: string; value: StageStatus }> = [
  { label: '待开始', value: 'PENDING' },
  { label: '执行中', value: 'RUNNING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已跳过', value: 'SKIPPED' },
  { label: '执行失败', value: 'FAILED' }
]

export const aiTaskStatusOptions: Array<{ label: string; value: AiTaskStatus }> = [
  { label: '已创建', value: 'CREATED' },
  { label: '分析中', value: 'ANALYZING' },
  { label: '执行成功', value: 'SUCCESS' },
  { label: '执行失败', value: 'FAILED' }
]

export const aiTaskTypeOptions: Array<{ label: string; value: AiTaskType }> = [
  { label: '需求分析', value: 'requirement_analysis' },
  { label: '开发计划', value: 'development_plan' },
  { label: '风险评审', value: 'risk_review' }
]

export function labelOf<T extends string>(options: Array<{ label: string; value: T }>, value?: T | null) {
  return options.find((item) => item.value === value)?.label ?? value ?? '-'
}
