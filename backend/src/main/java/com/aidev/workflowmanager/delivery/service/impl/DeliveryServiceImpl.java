package com.aidev.workflowmanager.delivery.service.impl;

import com.aidev.workflowmanager.common.enums.Complexity;
import com.aidev.workflowmanager.common.enums.RiskTag;
import com.aidev.workflowmanager.common.enums.StageStatus;
import com.aidev.workflowmanager.common.enums.TaskStatus;
import com.aidev.workflowmanager.common.enums.TaskType;
import com.aidev.workflowmanager.common.exception.BusinessException;
import com.aidev.workflowmanager.common.exception.ErrorCode;
import com.aidev.workflowmanager.delivery.entity.DeliveryRecord;
import com.aidev.workflowmanager.delivery.mapper.DeliveryRecordMapper;
import com.aidev.workflowmanager.delivery.service.DeliveryService;
import com.aidev.workflowmanager.delivery.vo.DeliveryPreviewResponse;
import com.aidev.workflowmanager.delivery.vo.DeliveryRecordResponse;
import com.aidev.workflowmanager.stage.entity.WorkflowStage;
import com.aidev.workflowmanager.stage.mapper.WorkflowStageMapper;
import com.aidev.workflowmanager.stage.vo.StageOutputParts;
import com.aidev.workflowmanager.stage.vo.WorkflowStageResponse;
import com.aidev.workflowmanager.task.entity.WorkflowTask;
import com.aidev.workflowmanager.task.mapper.WorkflowTaskMapper;
import com.aidev.workflowmanager.task.vo.TaskDetailResponse;
import com.aidev.workflowmanager.template.entity.WorkflowTemplate;
import com.aidev.workflowmanager.template.entity.WorkflowTemplateStage;
import com.aidev.workflowmanager.template.mapper.WorkflowTemplateMapper;
import com.aidev.workflowmanager.template.mapper.WorkflowTemplateStageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryServiceImpl.class);

    private final WorkflowTaskMapper workflowTaskMapper;
    private final WorkflowTemplateMapper workflowTemplateMapper;
    private final WorkflowTemplateStageMapper workflowTemplateStageMapper;
    private final WorkflowStageMapper workflowStageMapper;
    private final DeliveryRecordMapper deliveryRecordMapper;

    public DeliveryServiceImpl(WorkflowTaskMapper workflowTaskMapper,
                               WorkflowTemplateMapper workflowTemplateMapper,
                               WorkflowTemplateStageMapper workflowTemplateStageMapper,
                               WorkflowStageMapper workflowStageMapper,
                               DeliveryRecordMapper deliveryRecordMapper) {
        this.workflowTaskMapper = workflowTaskMapper;
        this.workflowTemplateMapper = workflowTemplateMapper;
        this.workflowTemplateStageMapper = workflowTemplateStageMapper;
        this.workflowStageMapper = workflowStageMapper;
        this.deliveryRecordMapper = deliveryRecordMapper;
    }

    @Override
    @Transactional
    public DeliveryRecordResponse generateTestChecklist(Long taskId) {
        WorkflowTask task = loadMutableTask(taskId, "test checklist generation");
        DeliveryRecord record = getOrCreateRecord(task);
        record.setTestChecklist(buildTestChecklist(task));
        record.setTestResult("待回填实际测试结果。");
        record.setRiskNotes(buildRiskNotes(task, loadStages(task.getId())));
        deliveryRecordMapper.updateById(record);

        task.setTestChecklistGenerated(true);
        task.setDeliveryRecordId(record.getId());
        if (!TaskStatus.DELIVERED.equals(task.getStatus())) {
            task.setStatus(TaskStatus.TESTING);
        }
        workflowTaskMapper.updateById(task);
        log.info("[DELIVERY] test checklist generated taskId={} deliveryRecordId={} checklistLength={} highRisk={}",
                taskId, record.getId(), length(record.getTestChecklist()), isHighRiskTask(task));
        return DeliveryRecordResponse.from(record, buildUnverifiedScope(loadStages(task.getId())));
    }

    @Override
    @Transactional
    public DeliveryRecordResponse generateDeliverySummary(Long taskId) {
        WorkflowTask task = loadMutableTask(taskId, "delivery summary generation");
        if (!Boolean.TRUE.equals(task.getTestChecklistGenerated())) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "请先生成测试清单，再生成交付总结。");
        }
        List<WorkflowStage> stages = loadStages(task.getId());
        assertDeliverableStages(task, stages);

        DeliveryRecord record = getOrCreateRecord(task);
        String unverifiedScope = buildUnverifiedScope(stages);
        if (isHighRiskTask(task) && !StringUtils.hasText(record.getRiskNotes())) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "High-risk delivery requires risk notes");
        }
        if (isHighRiskTask(task) && !StringUtils.hasText(unverifiedScope)) {
            unverifiedScope = "未回填明确未验证范围，请人工复核后再对外交付。";
        }
        record.setSummary(buildSummary(task, stages, unverifiedScope));
        record.setMarkdownContent(buildMarkdown(task, record, stages, unverifiedScope));
        record.setDeliveredAt(LocalDateTime.now());
        deliveryRecordMapper.updateById(record);

        task.setStatus(TaskStatus.DELIVERED);
        task.setDeliveryRecordId(record.getId());
        workflowTaskMapper.updateById(task);
        log.info("[DELIVERY] summary generated taskId={} deliveryRecordId={} summaryLength={} markdownLength={}",
                taskId, record.getId(), length(record.getSummary()), length(record.getMarkdownContent()));
        return DeliveryRecordResponse.from(record, unverifiedScope);
    }

    @Override
    public String exportMarkdown(Long taskId) {
        WorkflowTask task = loadTask(taskId);
        DeliveryRecord record = loadRecord(task);
        List<WorkflowStage> stages = loadStages(task.getId());
        String unverifiedScope = buildUnverifiedScope(stages);
        if (!StringUtils.hasText(record.getMarkdownContent())) {
            String markdown = buildMarkdown(task, record, stages, unverifiedScope);
            log.info("[DELIVERY] markdown exported taskId={} generatedFromRecord=false markdownLength={}",
                    taskId, length(markdown));
            return markdown;
        }
        log.info("[DELIVERY] markdown exported taskId={} generatedFromRecord=true markdownLength={}",
                taskId, length(record.getMarkdownContent()));
        return record.getMarkdownContent();
    }

    @Override
    public DeliveryPreviewResponse preview(Long taskId) {
        WorkflowTask task = loadTask(taskId);
        DeliveryRecord record = findRecord(task);
        List<WorkflowStage> stages = loadStages(task.getId());
        String markdown = record == null ? buildMarkdown(task, null, stages, buildUnverifiedScope(stages)) : exportMarkdown(taskId);
        WorkflowTemplate template = findTemplate(task.getMatchedTemplateId());

        DeliveryPreviewResponse response = new DeliveryPreviewResponse();
        TaskDetailResponse taskResponse = TaskDetailResponse.from(task);
        taskResponse.setMatchedTemplateName(template == null ? null : template.getName());
        taskResponse.setStages(toStageResponses(stages));
        response.setTask(taskResponse);
        response.setWorkflowName(template == null ? "" : template.getName());
        response.setStages(toStageResponses(stages));
        response.setRecord(DeliveryRecordResponse.from(record, buildUnverifiedScope(stages)));
        response.setMarkdown(markdown);
        log.info("[DELIVERY] preview loaded taskId={} stageCount={} hasRecord={} markdownLength={}",
                taskId, stages.size(), record != null, length(markdown));
        return response;
    }

    private WorkflowTask loadMutableTask(Long taskId, String action) {
        WorkflowTask task = loadTask(taskId);
        if (TaskStatus.ARCHIVED.equals(task.getStatus()) || TaskStatus.CANCELED.equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PARAM,
                    "Task status does not allow " + action + ": " + task.getStatus());
        }
        return task;
    }

    private WorkflowTask loadTask(Long taskId) {
        if (taskId == null || taskId < 1) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "taskId must be greater than or equal to 1");
        }
        WorkflowTask task = workflowTaskMapper.selectOne(new LambdaQueryWrapper<WorkflowTask>()
                .eq(WorkflowTask::getId, taskId));
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Task not found: " + taskId);
        }
        return task;
    }

    private DeliveryRecord getOrCreateRecord(WorkflowTask task) {
        DeliveryRecord record = findRecord(task);
        if (record != null) {
            return record;
        }
        record = new DeliveryRecord();
        record.setTaskId(task.getId());
        record.setDeleted(0);
        deliveryRecordMapper.insert(record);
        task.setDeliveryRecordId(record.getId());
        workflowTaskMapper.updateById(task);
        return record;
    }

    private DeliveryRecord loadRecord(WorkflowTask task) {
        DeliveryRecord record = findRecord(task);
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Delivery record not found for task: " + task.getId());
        }
        return record;
    }

    private DeliveryRecord findRecord(WorkflowTask task) {
        if (task.getDeliveryRecordId() != null) {
            DeliveryRecord byId = deliveryRecordMapper.selectOne(new LambdaQueryWrapper<DeliveryRecord>()
                    .eq(DeliveryRecord::getId, task.getDeliveryRecordId()));
            if (byId != null) {
                return byId;
            }
        }
        return deliveryRecordMapper.selectOne(new LambdaQueryWrapper<DeliveryRecord>()
                .eq(DeliveryRecord::getTaskId, task.getId()));
    }

    private List<WorkflowStage> loadStages(Long taskId) {
        return workflowStageMapper.selectList(new LambdaQueryWrapper<WorkflowStage>()
                .eq(WorkflowStage::getTaskId, taskId)
                .orderByAsc(WorkflowStage::getStageOrder));
    }

    private WorkflowTemplate findTemplate(Long templateId) {
        if (templateId == null) {
            return null;
        }
        return workflowTemplateMapper.selectOne(new LambdaQueryWrapper<WorkflowTemplate>()
                .eq(WorkflowTemplate::getId, templateId));
    }

    private void assertDeliverableStages(WorkflowTask task, List<WorkflowStage> stages) {
        if (stages == null || stages.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "Task stages must be initialized before delivery summary");
        }
        for (WorkflowStage stage : stages) {
            WorkflowTemplateStage templateStage = null;
            if (stage.getTemplateStageId() != null) {
                templateStage = workflowTemplateStageMapper.selectOne(new LambdaQueryWrapper<WorkflowTemplateStage>()
                        .eq(WorkflowTemplateStage::getId, stage.getTemplateStageId()));
            }
            boolean required = templateStage == null || Boolean.TRUE.equals(templateStage.getRequired());
            if (required && StageStatus.FAILED.equals(stage.getStatus())) {
                throw new BusinessException(ErrorCode.INVALID_PARAM,
                        "必需阶段失败，不能交付：" + stageDisplayName(stage));
            }
            if (required && !StageStatus.COMPLETED.equals(stage.getStatus())) {
                throw new BusinessException(ErrorCode.INVALID_PARAM,
                        "必需阶段未完成，不能交付：" + stageDisplayName(stage));
            }
        }
        if (isHighRiskTask(task) && !Boolean.TRUE.equals(task.getTestChecklistGenerated())) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "高风险任务必须先生成测试清单。");
        }
    }

    private String buildTestChecklist(WorkflowTask task) {
        List<String> items = new ArrayList<String>();
        items.add("- [ ] 后端接口返回统一 `code/message/data` 结构。");
        items.add("- [ ] 主流程接口参数、响应字段与前端调用保持一致。");
        items.add("- [ ] 异常分支返回可读业务错误。");
        if (TaskType.FEATURE.equals(task.getTaskType())) {
            items.add("- [ ] 功能主流程从创建任务到交付预览可串联。");
        }
        if (TaskType.BUGFIX.equals(task.getTaskType())) {
            items.add("- [ ] 覆盖缺陷复现路径和相关回归路径。");
        }
        if (TaskType.REVIEW.equals(task.getTaskType())) {
            items.add("- [ ] 检查风险、缺陷、状态一致性和缺失验证。");
        }
        if (TaskType.DOCS.equals(task.getTaskType())) {
            items.add("- [ ] 检查内容准确性、示例和链接可用性。");
        }
        appendRiskChecklist(items, task.getRiskTags());
        return String.join("\n", items);
    }

    private void appendRiskChecklist(List<String> items, List<RiskTag> riskTags) {
        if (riskTags == null) {
            return;
        }
        for (RiskTag riskTag : riskTags) {
            if (RiskTag.PERMISSION.equals(riskTag)) {
                items.add("- [ ] 权限成功、权限失败、越权访问均已验证。");
            } else if (RiskTag.DATABASE.equals(riskTag)) {
                items.add("- [ ] 字段兼容、迁移风险和回滚风险已检查。");
            } else if (RiskTag.FILE.equals(riskTag)) {
                items.add("- [ ] 文件存在、异常文件和文件权限路径已覆盖。");
            } else if (RiskTag.DOWNLOAD.equals(riskTag)) {
                items.add("- [ ] 有权限下载、无权限拒绝和链接过期已验证。");
            } else if (RiskTag.PAYMENT.equals(riskTag)) {
                items.add("- [ ] 重复回调、金额一致性和支付状态一致性已验证。");
            } else if (RiskTag.CALLBACK.equals(riskTag)) {
                items.add("- [ ] 幂等、签名校验和异常重试已验证。");
            } else if (RiskTag.AUTH.equals(riskTag)) {
                items.add("- [ ] 登录态失效、未登录访问和认证边界已验证。");
            } else if (RiskTag.CONFIG.equals(riskTag)) {
                items.add("- [ ] 配置默认值、环境差异和敏感信息风险已检查。");
            } else if (RiskTag.AMOUNT.equals(riskTag)) {
                items.add("- [ ] 金额精度、舍入规则和边界金额已验证。");
            }
        }
    }

    private String buildRiskNotes(WorkflowTask task, List<WorkflowStage> stages) {
        StringBuilder builder = new StringBuilder();
        if (isHighRiskTask(task)) {
            builder.append("该任务包含高风险因素：").append(riskTagsToText(task.getRiskTags())).append("。需人工复核状态一致性、异常分支和回滚方案。");
        } else {
            builder.append("当前任务未标记高风险标签，仍需按实际变更确认异常分支。");
        }
        String stageRisks = stages.stream()
                .map(stage -> StageOutputParts.parse(stage.getOutputSummary()).getRiskPoints())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("\n"));
        if (StringUtils.hasText(stageRisks)) {
            builder.append("\n\n阶段风险记录：\n").append(stageRisks);
        }
        return builder.toString();
    }

    private String buildUnverifiedScope(List<WorkflowStage> stages) {
        if (stages == null) {
            return "";
        }
        return stages.stream()
                .map(stage -> StageOutputParts.parse(stage.getOutputSummary()).getUnverifiedScope())
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.joining("\n"));
    }

    private String buildSummary(WorkflowTask task, List<WorkflowStage> stages, String unverifiedScope) {
        String completedStages = stages.stream()
                .filter(stage -> StageStatus.COMPLETED.equals(stage.getStatus()))
                .map(WorkflowStage::getStageName)
                .collect(Collectors.joining("、"));
        return "任务 `" + task.getTitle() + "` 已完成交付记录生成。已完成阶段：" + completedStages
                + "。测试清单已生成，交付前仍需结合实际命令结果和人工检查补充测试结果。"
                + (StringUtils.hasText(unverifiedScope) ? "\n未验证范围：\n" + unverifiedScope : "");
    }

    private String buildMarkdown(WorkflowTask task, DeliveryRecord record, List<WorkflowStage> stages, String unverifiedScope) {
        WorkflowTemplate template = findTemplate(task.getMatchedTemplateId());
        StringBuilder builder = new StringBuilder();
        builder.append("# AI 开发任务交付记录\n\n");
        builder.append("## 任务信息\n\n");
        builder.append("- 标题：").append(nullToEmpty(task.getTitle())).append("\n");
        builder.append("- 类型：").append(task.getTaskType() == null ? "" : task.getTaskType().getCode()).append("\n");
        builder.append("- 复杂度：").append(task.getComplexity() == null ? "" : task.getComplexity().getCode()).append("\n");
        builder.append("- 风险标签：").append(riskTagsToText(task.getRiskTags())).append("\n");
        builder.append("- Workflow：").append(template == null ? "" : template.getName()).append("\n\n");
        builder.append("## 阶段记录\n\n");
        for (WorkflowStage stage : stages) {
            StageOutputParts parts = StageOutputParts.parse(stage.getOutputSummary());
            builder.append("### ").append(stage.getStageOrder()).append(". ").append(stage.getStageName()).append("\n\n");
            builder.append("- 状态：").append(stage.getStatus()).append("\n");
            builder.append("- 输出摘要：").append(nullToDash(parts.getOutputSummary())).append("\n");
            builder.append("- 风险点：").append(nullToDash(parts.getRiskPoints())).append("\n");
            builder.append("- 后续动作：").append(nullToDash(parts.getNextActions())).append("\n");
            builder.append("- 未验证范围：").append(nullToDash(parts.getUnverifiedScope())).append("\n\n");
        }
        builder.append("## 测试清单\n\n").append(record == null ? "" : nullToEmpty(record.getTestChecklist())).append("\n\n");
        builder.append("## 测试结果\n\n").append(record == null ? "" : nullToEmpty(record.getTestResult())).append("\n\n");
        builder.append("## 风险说明\n\n").append(record == null ? "" : nullToEmpty(record.getRiskNotes())).append("\n\n");
        builder.append("## 未验证范围\n\n").append(nullToEmpty(unverifiedScope)).append("\n\n");
        builder.append("## 交付总结\n\n").append(record == null ? "" : nullToEmpty(record.getSummary())).append("\n");
        return builder.toString();
    }

    private List<WorkflowStageResponse> toStageResponses(List<WorkflowStage> stages) {
        return stages.stream().map(WorkflowStageResponse::from).collect(Collectors.toList());
    }

    private boolean isHighRiskTask(WorkflowTask task) {
        return Complexity.COMPLEX.equals(task.getComplexity())
                || (task.getRiskTags() != null && !task.getRiskTags().isEmpty());
    }

    private String riskTagsToText(List<RiskTag> riskTags) {
        if (riskTags == null || riskTags.isEmpty()) {
            return "无";
        }
        return riskTags.stream().map(RiskTag::getCode).collect(Collectors.joining(", "));
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String nullToDash(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }

    private int length(String value) {
        return value == null ? 0 : value.length();
    }

    private String stageDisplayName(WorkflowStage stage) {
        return StringUtils.hasText(stage.getStageName()) ? stage.getStageName() : stage.getStageKey();
    }
}
