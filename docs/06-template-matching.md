# 模板匹配

## 匹配目标

模板匹配用于根据任务属性选择最适合的 workflow 模板。第一版不追求复杂算法，先使用规则匹配，保证结果可解释、可调试、可维护。

输入字段：

- `task_type`
- `complexity`
- `risk_tags`

模板字段：

- `task_type`
- `complexity`
- `risk_tags`
- `priority`
- `version`
- `enabled`

## 匹配规则

1. 只匹配 `enabled = true` 的模板。
2. `task_type` 相同的模板优先。
3. `complexity` 相同的模板优先。
4. 模板覆盖的风险标签越多，得分越高。
5. 高风险标签命中时提高模板优先级。
6. 多模板命中时按 `priority`、`version`、`enabled` 选择。

## 高风险标签权重

高风险标签建议包括：

- `payment`
- `permission`
- `database`
- `config`
- `file`
- `auth`
- `download`
- `amount`
- `callback`

命中这些标签时，模板得分应明显高于普通标签，避免高风险任务匹配到过于简单的 workflow。

## 伪代码

```text
function matchTemplate(task):
    candidates = find workflow_template where enabled = true

    for template in candidates:
        score = 0

        if template.task_type == task.task_type:
            score += 40

        if template.complexity == task.complexity:
            score += 30

        matchedRiskTags = intersection(template.risk_tags, task.risk_tags)
        score += count(matchedRiskTags) * 10

        for tag in matchedRiskTags:
            if tag is high risk:
                score += 20

        score += template.priority
        template.match_score = score

    return candidates
        .filter(match_score > 0)
        .sortBy(match_score desc, priority desc, version desc)
        .first()
```

## 多模板命中处理

当多个模板同时命中时：

1. 先选择得分最高的模板。
2. 得分相同时选择 `priority` 更高的模板。
3. 优先级相同时选择 `version` 更新的模板。
4. 仍然相同时返回多个候选，让用户手动选择。

## 匹配结果说明

接口返回时建议包含匹配说明，例如：

- 命中任务类型：`feature`。
- 命中复杂度：`COMPLEX`。
- 命中高风险标签：`database`、`permission`。
- 选择原因：高风险标签命中，优先使用包含分析、风险评审、测试清单和交付总结的 workflow。
