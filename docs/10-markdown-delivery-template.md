# Markdown 交付记录模板

## 模板目标

Markdown 交付记录用于把一次 AI 辅助开发任务沉淀成可复盘、可展示的结果。它既可以用于个人复盘，也可以作为 GitHub、面试或自媒体内容的基础素材。

## 推荐结构

```markdown
# {{taskTitle}}

## 任务信息

- 任务类型：{{taskType}}
- 复杂度：{{complexity}}
- 风险标签：{{riskTags}}
- 使用 workflow：{{workflowTemplateName}}
- 交付时间：{{deliveredAt}}

## 需求摘要

{{taskDescription}}

## Workflow 阶段记录

{{stageRecords}}

## 测试清单

{{testChecklist}}

## 测试结果

{{testResult}}

## 风险说明

{{riskNotes}}

## 未验证范围

{{unverifiedScope}}

## 交付总结

{{summary}}
```

## 阶段记录格式

每个阶段建议导出为：

```markdown
### {{stageName}}

- 状态：{{stageStatus}}
- 阶段目标：{{stageGoal}}
- 关键输出：{{outputSummary}}
- 风险点：{{riskPoints}}
- 后续动作：{{nextActions}}
```

## 高风险任务要求

当任务包含高风险标签时，Markdown 交付记录必须包含：

- 风险说明。
- 未验证范围。
- 测试结果。
- 关键阶段输出。

如果缺少测试清单或存在未处理的必需阶段失败记录，不允许生成最终交付记录。
