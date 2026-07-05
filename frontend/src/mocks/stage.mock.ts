import type { WorkflowStage } from '@/types/stage'

export const mockStages: WorkflowStage[] = [
  {
    id: 1001,
    taskId: 1,
    templateStageId: 201,
    stageOrder: 1,
    stageKey: 'analysis',
    stageName: '需求分析',
    stageGoal: '拆解需求目标、影响范围和边界。',
    required: true,
    status: 'COMPLETED',
    startedAt: '2026-07-05T10:20:00',
    completedAt: '2026-07-05T10:45:00',
    outputSummary: '确认本轮以完整页面骨架和 mock 数据为主，不阻塞后端接口。',
    riskPoints: '后续真实接口字段需要和前端展示字段对齐。',
    nextActions: '补齐页面、路由、组件和 mock 数据。',
    unverifiedScope: '未做端到端真实接口联调。'
  },
  {
    id: 1002,
    taskId: 1,
    templateStageId: 202,
    stageOrder: 2,
    stageKey: 'design',
    stageName: '方案设计',
    stageGoal: '确定实现路径、接口和验证方式。',
    required: true,
    status: 'RUNNING',
    startedAt: '2026-07-05T10:50:00',
    completedAt: null
  },
  {
    id: 1003,
    taskId: 1,
    templateStageId: 203,
    stageOrder: 3,
    stageKey: 'implementation',
    stageName: '实现修改',
    stageGoal: '完成代码实现并保持低侵入。',
    required: true,
    status: 'PENDING',
    startedAt: null,
    completedAt: null
  },
  {
    id: 1004,
    taskId: 1,
    templateStageId: 204,
    stageOrder: 4,
    stageKey: 'review',
    stageName: '代码审查',
    stageGoal: '检查行为回归、边界和测试缺口。',
    required: true,
    status: 'PENDING',
    startedAt: null,
    completedAt: null
  }
]
