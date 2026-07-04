# 开发前确认

## 目标

本文档用于在正式编码前固定第一版 MVP 的实现边界、默认数据、枚举、workflow、Prompt 变量和开发优先级。

第一版不是 AI 平台，也不是自动执行系统，而是个人 AI 开发流程管理与交付复盘工具。

## MVP 主链路

第一版只围绕一条可演示、可闭环的主链路开发：

```text
创建任务
↓
填写任务类型、复杂度、风险标签
↓
匹配 workflow
↓
初始化任务阶段
↓
生成阶段 prompt
↓
人工复制 prompt 到 Codex / Claude Code
↓
人工回填阶段输出
↓
推进后续阶段
↓
生成测试清单
↓
记录测试结果和风险说明
↓
生成交付总结
↓
导出 Markdown 复盘记录
```

第一版所有接口、页面和数据模型都优先服务这条链路。

## 默认枚举

### 任务类型

- `feature`：功能开发。
- `bugfix`：缺陷修复。
- `refactor`：重构优化。
- `review`：代码审查。
- `docs`：文档任务。

### 复杂度

- `SIMPLE`：单点、小范围、低风险任务。
- `MEDIUM`：涉及多个文件或前后端联动，但影响范围可控。
- `COMPLEX`：涉及多模块、状态流转、数据库、权限、文件、支付等高风险或高影响任务。

### 高风险标签

- `payment`：支付。
- `permission`：权限。
- `database`：数据库。
- `config`：配置。
- `file`：文件处理。
- `auth`：登录认证。
- `download`：下载权限。
- `amount`：金额计算。
- `callback`：第三方回调。

### 任务状态

- `DRAFT`：草稿。
- `ANALYZING`：分析中。
- `EXECUTING`：执行中。
- `TESTING`：测试中。
- `DELIVERED`：已交付。
- `ARCHIVED`：已归档。
- `CANCELED`：已取消。

### 阶段状态

- `PENDING`：待开始。
- `RUNNING`：执行中。
- `COMPLETED`：已完成。
- `SKIPPED`：已跳过。
- `FAILED`：执行失败。

## 默认 workflow

第一版先内置三套 workflow 模板，支撑主要演示场景。

### 简单任务 workflow

适用场景：

- 文档任务。
- 单文件小修改。
- 低风险 bugfix。

阶段：

1. `analysis`：需求分析。
2. `implementation`：执行修改。
3. `testing`：验证检查。
4. `delivery`：交付总结。

### 标准开发 workflow

适用场景：

- 普通功能开发。
- 中等复杂度 bugfix。
- 可控范围内的前后端联动。

阶段：

1. `analysis`：需求分析。
2. `design`：方案设计。
3. `implementation`：实现修改。
4. `review`：代码审查。
5. `testing`：测试验证。
6. `delivery`：交付总结。

### 高风险 workflow

适用场景：

- 涉及支付、权限、数据库、配置、文件、登录认证、下载权限、金额计算、第三方回调的任务。
- 复杂度为 `COMPLEX` 的任务。

阶段：

1. `analysis`：需求分析。
2. `risk_review`：风险评审。
3. `design`：方案设计。
4. `implementation`：实现修改。
5. `review`：代码审查。
6. `testing`：测试验证。
7. `delivery`：交付总结。

约束：

- `analysis`、`risk_review`、`testing`、`delivery` 为必需阶段。
- 高风险任务不能跳过 `analysis` 和 `risk_review`。
- 未生成测试清单不能进入 `DELIVERED`。

## Prompt 变量

第一版 Prompt 变量保持简单，先使用字符串替换实现。

必需变量：

- `taskTitle`
- `taskDescription`
- `taskType`
- `complexity`
- `riskTags`
- `workflowName`
- `stageName`
- `stageGoal`
- `previousStageOutputs`
- `stageInstructions`

可选变量：

- `required`
- `riskNotes`
- `testChecklist`
- `unverifiedScope`

第一版不做复杂表达式、不做嵌套变量、不做条件语法。

## Demo 数据

第一版至少准备 5 条演示任务，用于初始化数据、接口测试和面试演示。

### 文档任务

- 标题：完善 README 项目定位。
- 类型：`docs`。
- 复杂度：`SIMPLE`。
- 风险标签：无。
- 展示重点：低风险任务的简单 workflow。

### Bug 修复任务

- 标题：修复任务详情页阶段状态显示异常。
- 类型：`bugfix`。
- 复杂度：`MEDIUM`。
- 风险标签：无。
- 展示重点：标准开发 workflow 和阶段输出回填。

### 功能开发任务

- 标题：新增任务 Markdown 导出功能。
- 类型：`feature`。
- 复杂度：`MEDIUM`。
- 风险标签：`file`。
- 展示重点：Prompt 生成、测试清单和交付记录。

### 高风险任务

- 标题：新增下载权限校验。
- 类型：`feature`。
- 复杂度：`COMPLEX`。
- 风险标签：`permission`、`download`、`file`。
- 展示重点：高风险 workflow、风险评审和未验证范围。

### 代码审查任务

- 标题：审查订单回调状态流转。
- 类型：`review`。
- 复杂度：`COMPLEX`。
- 风险标签：`payment`、`amount`、`callback`。
- 展示重点：高风险约束、审查输出和风险说明。

## 后端开发优先级

1. 创建基础 Spring Boot 项目。
2. 定义枚举、通用响应和异常处理。
3. 实现任务创建和查询。
4. 实现 workflow 模板和阶段模板初始化。
5. 实现模板匹配。
6. 实现任务阶段初始化。
7. 实现阶段开始、完成、跳过、失败。
8. 实现阶段输出回填。
9. 实现阶段 prompt 生成。
10. 实现测试清单生成。
11. 实现交付总结和 Markdown 导出。

第一版模板管理可以先通过初始化 SQL 或启动数据完成，不急着做完整后台编辑能力。

## 前端开发优先级

1. 创建 Vue 3、TypeScript、Element Plus 项目。
2. 实现任务列表。
3. 实现任务创建。
4. 实现任务详情。
5. 实现 workflow 匹配结果展示。
6. 实现阶段列表和阶段状态展示。
7. 实现阶段 prompt 预览和复制。
8. 实现阶段输出回填。
9. 实现测试清单和交付记录预览。
10. 实现 Markdown 导出内容预览。

第一版前端重点是把主链路跑通，不做复杂仪表盘和模板市场。

## 第一版不做

- 不做登录。
- 不做 RBAC。
- 不做多租户。
- 不引入消息队列。
- 不引入复杂规则引擎。
- 不接入真实 AI API。
- 不自动执行 Codex / Claude Code。
- 不扫描代码仓库。
- 不做 RAG。
- 不做模板市场。
- 不做团队审批。
- 不做复杂统计分析。

## 实现取舍

- 风险标签第一版可以用 JSON 字段保存。
- Prompt 变量第一版使用简单字符串替换。
- 状态机第一版在 Service 层做显式校验。
- 模板匹配第一版使用规则打分，不引入规则引擎。
- Markdown 导出第一版返回文本内容，不急着生成文件。
- 模板数据第一版优先使用初始化 SQL 或启动初始化。

## 编码前检查清单

正式进入编码前确认：

- MVP 主链路已经固定。
- 默认枚举已经固定。
- 三套默认 workflow 已经固定。
- Prompt 变量已经固定。
- Demo 数据已经固定。
- 第一版不做事项已经固定。
- 后端和前端优先级已经固定。
