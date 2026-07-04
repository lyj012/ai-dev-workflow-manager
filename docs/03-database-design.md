# 数据库设计

## 设计原则

第一版数据模型保持简单，优先支撑流程闭环，不提前设计复杂权限、多租户、模板市场或自动执行记录。

通用字段：

- `id`：主键。
- `created_at`：创建时间。
- `updated_at`：更新时间。
- `deleted`：逻辑删除标记。

## workflow_task

AI 开发任务主表。

核心字段：

- `title`：任务标题。
- `description`：任务描述。
- `task_type`：任务类型，例如 feature、bugfix、refactor、review、docs。
- `complexity`：复杂度，例如 SIMPLE、MEDIUM、COMPLEX。
- `risk_tags`：风险标签，第一版可用 JSON 字符串保存。
- `status`：任务状态。
- `matched_template_id`：命中的 workflow 模板。
- `test_checklist_generated`：是否已生成测试清单。
- `delivery_record_id`：交付记录 ID。

## workflow_template

workflow 模板主表。

核心字段：

- `name`：模板名称。
- `description`：模板说明。
- `task_type`：适用任务类型。
- `complexity`：适用复杂度。
- `risk_tags`：适用风险标签。
- `priority`：模板优先级。
- `version`：模板版本。
- `enabled`：是否启用。

## workflow_template_stage

模板阶段定义表。

核心字段：

- `template_id`：所属 workflow 模板。
- `stage_key`：阶段标识，例如 analysis、design、implementation、review、testing、delivery。
- `stage_name`：阶段名称。
- `stage_order`：阶段顺序。
- `required`：是否必需。
- `default_prompt_template_id`：默认 prompt 模板。
- `description`：阶段说明。

## workflow_stage

任务实际执行阶段表。

核心字段：

- `task_id`：所属任务。
- `template_stage_id`：来源模板阶段。
- `stage_key`：阶段标识。
- `stage_name`：阶段名称。
- `stage_order`：阶段顺序。
- `status`：阶段状态。
- `input_summary`：阶段输入摘要。
- `output_summary`：阶段输出摘要。
- `started_at`：开始时间。
- `completed_at`：完成时间。

## prompt_template

prompt 模板表。

核心字段：

- `name`：模板名称。
- `prompt_type`：模板类型，例如 STAGE、TEST_CHECKLIST、DELIVERY_SUMMARY、EXPORT。
- `content`：模板内容。
- `variables`：变量说明，第一版可用 JSON 字符串保存。
- `enabled`：是否启用。

## delivery_record

交付记录表。

核心字段：

- `task_id`：所属任务。
- `summary`：交付总结。
- `test_checklist`：测试清单。
- `test_result`：测试结果。
- `risk_notes`：风险说明。
- `markdown_content`：Markdown 导出内容。
- `delivered_at`：交付时间。

## risk_tag

可选风险标签字典表。

核心字段：

- `tag_key`：标签标识，例如 payment、permission、database、config、file、auth。
- `tag_name`：标签名称。
- `description`：标签说明。
- `risk_level`：风险等级。
- `enabled`：是否启用。

第一版也可以先不创建 `risk_tag` 表，直接在代码或配置中维护风险标签枚举。
