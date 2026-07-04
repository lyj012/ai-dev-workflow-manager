# 状态机设计

## 任务状态

任务状态用于描述一个 AI 开发任务的整体进度。

- `DRAFT`：草稿，任务刚创建，尚未开始分析。
- `ANALYZING`：分析中，正在进行需求分析、风险识别和 workflow 匹配。
- `EXECUTING`：执行中，正在推进实现、审查或修复阶段。
- `TESTING`：测试中，正在生成或执行测试清单。
- `DELIVERED`：已交付，已经形成交付总结和交付记录。
- `ARCHIVED`：已归档，任务不可再修改。
- `CANCELED`：已取消，任务终止。

推荐主流程：

`DRAFT -> ANALYZING -> EXECUTING -> TESTING -> DELIVERED -> ARCHIVED`

任务也可以从 `DRAFT`、`ANALYZING`、`EXECUTING`、`TESTING` 进入 `CANCELED`。

## 阶段状态

阶段状态用于描述某个 workflow 阶段的执行情况。

- `PENDING`：待开始。
- `RUNNING`：执行中。
- `COMPLETED`：已完成。
- `SKIPPED`：已跳过。
- `FAILED`：执行失败。

推荐主流程：

`PENDING -> RUNNING -> COMPLETED`

非必需阶段可以从 `PENDING` 进入 `SKIPPED`。执行异常时可以从 `RUNNING` 进入 `FAILED`。

## 高风险任务约束

当任务包含支付、权限、数据库、配置、文件处理、登录认证、下载权限、金额计算、回调处理等高风险标签时，需要启用更严格约束：

- 不能跳过需求分析阶段。
- 不能跳过风险评审阶段。
- 进入执行前必须完成风险标签确认。
- 交付前必须生成测试清单。
- 交付总结中必须包含风险说明和未验证范围。

## 交付约束

- 未生成测试清单的任务不能进入 `DELIVERED`。
- 存在 `FAILED` 且未处理的必需阶段时不能进入 `DELIVERED`。
- 未完成必需阶段时不能进入 `TESTING` 或 `DELIVERED`。
- 已归档任务不能修改任务信息、阶段输出和交付记录。

## 状态校验建议

状态流转不要只依赖前端按钮控制，后端接口也必须校验：

- 当前状态是否合法。
- 目标状态是否合法。
- 必需阶段是否完成。
- 高风险约束是否满足。
- 测试清单是否已经生成。
- 归档任务是否被修改。
