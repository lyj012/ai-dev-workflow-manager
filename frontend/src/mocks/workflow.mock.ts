import type { WorkflowTemplate } from '@/types/template'

export const mockTemplates: WorkflowTemplate[] = [
  {
    id: 1,
    name: '简单任务 Workflow',
    description: '适合文档、单点修改和低风险任务。',
    applicableTaskTypes: ['docs'],
    applicableComplexities: ['SIMPLE'],
    riskTags: [],
    priority: 10,
    version: '1.0.0',
    enabled: true,
    stages: [
      { id: 101, templateId: 1, stageOrder: 1, stageKey: 'analysis', stageName: '需求分析', required: true, stageGoal: '明确任务目标、边界和验收标准。' },
      { id: 102, templateId: 1, stageOrder: 2, stageKey: 'implementation', stageName: '执行修改', required: true, stageGoal: '按最小范围完成修改。' },
      { id: 103, templateId: 1, stageOrder: 3, stageKey: 'testing', stageName: '验证检查', required: true, stageGoal: '执行必要验证并记录结果。' },
      { id: 104, templateId: 1, stageOrder: 4, stageKey: 'delivery', stageName: '交付总结', required: true, stageGoal: '整理交付内容和剩余风险。' }
    ]
  },
  {
    id: 2,
    name: '标准开发 Workflow',
    description: '适合功能开发、缺陷修复和可控范围内的前后端联动。',
    applicableTaskTypes: ['feature', 'bugfix', 'refactor'],
    applicableComplexities: ['MEDIUM'],
    riskTags: [],
    priority: 20,
    version: '1.0.0',
    enabled: true,
    stages: [
      { id: 201, templateId: 2, stageOrder: 1, stageKey: 'analysis', stageName: '需求分析', required: true, stageGoal: '拆解需求目标、影响范围和边界。' },
      { id: 202, templateId: 2, stageOrder: 2, stageKey: 'design', stageName: '方案设计', required: true, stageGoal: '确定实现路径、接口和验证方式。' },
      { id: 203, templateId: 2, stageOrder: 3, stageKey: 'implementation', stageName: '实现修改', required: true, stageGoal: '完成代码实现并保持低侵入。' },
      { id: 204, templateId: 2, stageOrder: 4, stageKey: 'review', stageName: '代码审查', required: true, stageGoal: '检查行为回归、边界和测试缺口。' },
      { id: 205, templateId: 2, stageOrder: 5, stageKey: 'testing', stageName: '测试验证', required: true, stageGoal: '执行构建、自动化或浏览器验证。' },
      { id: 206, templateId: 2, stageOrder: 6, stageKey: 'delivery', stageName: '交付总结', required: true, stageGoal: '输出交付摘要、风险和后续建议。' }
    ]
  },
  {
    id: 3,
    name: '高风险 Workflow',
    description: '适合涉及权限、数据库、支付、配置、文件等高风险任务。',
    applicableTaskTypes: ['feature', 'bugfix', 'review'],
    applicableComplexities: ['COMPLEX'],
    riskTags: ['permission', 'database', 'payment', 'config', 'file', 'auth', 'download', 'amount', 'callback'],
    priority: 30,
    version: '1.0.0',
    enabled: true,
    stages: [
      { id: 301, templateId: 3, stageOrder: 1, stageKey: 'analysis', stageName: '需求分析', required: true, stageGoal: '明确目标、影响范围和高风险标签。' },
      { id: 302, templateId: 3, stageOrder: 2, stageKey: 'risk_review', stageName: '风险评审', required: true, stageGoal: '复核权限、数据、配置、回调和回滚风险。' },
      { id: 303, templateId: 3, stageOrder: 3, stageKey: 'design', stageName: '方案设计', required: true, stageGoal: '设计可回滚、可验证的实现方案。' },
      { id: 304, templateId: 3, stageOrder: 4, stageKey: 'implementation', stageName: '实现修改', required: true, stageGoal: '完成实现并补充必要观测信息。' },
      { id: 305, templateId: 3, stageOrder: 5, stageKey: 'review', stageName: '代码审查', required: true, stageGoal: '重点审查状态一致性、权限和幂等。' },
      { id: 306, templateId: 3, stageOrder: 6, stageKey: 'testing', stageName: '测试验证', required: true, stageGoal: '覆盖成功、失败、边界和回归场景。' },
      { id: 307, templateId: 3, stageOrder: 7, stageKey: 'delivery', stageName: '交付总结', required: true, stageGoal: '明确验证范围、未验证范围和剩余风险。' }
    ]
  }
]
