# API 设计

## 设计说明

第一版 REST API 只覆盖流程管理闭环，不包含用户登录、复杂权限、自动执行、AI 接入和代码扫描。

统一前缀建议使用 `/api/v1`。

## 任务创建

`POST /api/v1/tasks`

请求字段：

- `title`
- `description`
- `taskType`
- `complexity`
- `riskTags`

返回：

- 任务基础信息。
- 初始状态 `DRAFT`。

## 任务查询

`GET /api/v1/tasks`

支持按状态、任务类型、复杂度、风险标签分页查询。

`GET /api/v1/tasks/{taskId}`

返回任务详情、命中模板、阶段列表和交付记录摘要。

## 模板管理

`POST /api/v1/workflow-templates`

创建 workflow 模板。

`GET /api/v1/workflow-templates`

查询模板列表。

`GET /api/v1/workflow-templates/{templateId}`

查询模板详情和阶段定义。

`PUT /api/v1/workflow-templates/{templateId}`

更新模板基础信息。

`POST /api/v1/workflow-templates/{templateId}/stages`

新增模板阶段。

## 模板匹配

`POST /api/v1/tasks/{taskId}/match-template`

根据任务的 `taskType`、`complexity`、`riskTags` 匹配 workflow 模板。

返回：

- 命中模板。
- 匹配原因。
- 是否自动绑定到任务。

## 阶段推进

`POST /api/v1/tasks/{taskId}/stages/init`

根据已匹配模板初始化任务阶段。

`POST /api/v1/tasks/{taskId}/stages/{stageId}/start`

将阶段状态从 `PENDING` 推进到 `RUNNING`。

`POST /api/v1/tasks/{taskId}/stages/{stageId}/complete`

将阶段状态推进到 `COMPLETED`，并记录阶段输出。

`POST /api/v1/tasks/{taskId}/stages/{stageId}/skip`

跳过非必需阶段。高风险任务的分析和风险评审阶段不允许跳过。

`POST /api/v1/tasks/{taskId}/stages/{stageId}/fail`

标记阶段失败并记录原因。

## 阶段输出记录

`POST /api/v1/tasks/{taskId}/stages/{stageId}/outputs`

记录阶段输出摘要、关键结论、风险点和后续动作。

`GET /api/v1/tasks/{taskId}/stages/{stageId}/outputs`

查询阶段输出记录。

## 指令生成

`POST /api/v1/tasks/{taskId}/stages/{stageId}/generate-prompt`

根据任务、阶段、模板和已有输出生成阶段执行指令。

返回：

- `promptContent`
- 使用的 `promptTemplateId`
- 变量填充结果。

## 测试清单生成

`POST /api/v1/tasks/{taskId}/generate-test-checklist`

根据任务类型、复杂度、风险标签和阶段输出生成测试清单。

生成后将任务标记为 `testChecklistGenerated = true`。

## 交付总结生成

`POST /api/v1/tasks/{taskId}/generate-delivery-summary`

根据任务信息、阶段输出和测试清单生成交付总结草案。

未生成测试清单时不允许生成最终交付记录。

## Markdown 导出

`GET /api/v1/tasks/{taskId}/export/markdown`

导出任务 Markdown 交付记录，包含：

- 任务信息。
- workflow 模板。
- 阶段输出。
- 测试清单。
- 风险说明。
- 交付总结。
