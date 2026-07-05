import type { DeliveryRecord } from '@/types/delivery'

export const mockDeliveryRecord: DeliveryRecord = {
  testChecklist: [
    '- npm run build',
    '- 浏览器打开 /dashboard、/tasks、/tasks/1、/templates、/delivery/1',
    '- 检查页面不会因后端接口缺失崩溃',
    '- 检查 Prompt 和 Markdown 复制按钮占位'
  ].join('\n'),
  testResult: '前端构建通过，主要页面使用 mock 数据可展示。',
  riskNotes: '当前仍有部分接口未接真实后端，后续需要逐项替换 mock。',
  unverifiedScope: '未验证真实数据库数据、真实阶段推进和真实 Markdown 导出。',
  summary: '完成前端项目架构、路由、页面和 mock 主链路，为后续后端查缺补漏提供页面契约。'
}

export const mockMarkdown = `# 前端项目架构和页面骨架

## 任务信息

- 任务类型：功能开发
- 复杂度：MEDIUM
- 风险标签：配置、权限
- 使用 workflow：标准开发 Workflow

## Workflow 阶段记录

### 需求分析

- 状态：已完成
- 关键输出：明确本轮先搭页面骨架和 mock 数据。

### 方案设计

- 状态：执行中
- 关键输出：保留 API 模块和类型定义，默认使用 mock 数据。

## 测试清单

${mockDeliveryRecord.testChecklist}

## 测试结果

${mockDeliveryRecord.testResult}

## 风险说明

${mockDeliveryRecord.riskNotes}

## 未验证范围

${mockDeliveryRecord.unverifiedScope}

## 交付总结

${mockDeliveryRecord.summary}
`
