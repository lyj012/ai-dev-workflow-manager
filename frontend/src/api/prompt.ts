import request from '@/api/request'
import { USE_MOCK } from '@/constants/enums'
import { getMockTaskDetail } from '@/mocks/task.mock'
import type { GeneratePromptResponse } from '@/types/prompt'

export function generatePrompt(taskId: number, stageId: number) {
  if (USE_MOCK) {
    const detail = getMockTaskDetail(taskId)
    const stage = detail.stages.find((item) => item.id === stageId) ?? detail.stages[0]
    return Promise.resolve<GeneratePromptResponse>({
      promptContent: [
        '你正在处理一个 AI 辅助开发任务。',
        '',
        `任务：${detail.title}`,
        `描述：${detail.description}`,
        `类型：${detail.taskType}`,
        `复杂度：${detail.complexity}`,
        `风险标签：${detail.riskTags.join(', ') || '无'}`,
        '',
        `当前阶段：${stage.stageName}`,
        `阶段目标：${stage.stageGoal ?? '完成本阶段要求'}`,
        '',
        '输出要求：请给出本阶段结论、风险点、验证建议、未验证范围和下一步动作。'
      ].join('\n'),
      promptTemplateId: 1,
      variables: {}
    })
  }
  return request.post<GeneratePromptResponse>(`/tasks/${taskId}/stages/${stageId}/generate-prompt`)
}
