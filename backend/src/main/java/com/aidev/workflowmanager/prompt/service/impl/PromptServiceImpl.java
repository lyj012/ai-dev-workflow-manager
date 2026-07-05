package com.aidev.workflowmanager.prompt.service.impl;

import com.aidev.workflowmanager.common.enums.Complexity;
import com.aidev.workflowmanager.common.enums.RiskTag;
import com.aidev.workflowmanager.common.exception.BusinessException;
import com.aidev.workflowmanager.common.exception.ErrorCode;
import com.aidev.workflowmanager.prompt.service.PromptService;
import com.aidev.workflowmanager.prompt.vo.GeneratePromptResponse;
import com.aidev.workflowmanager.stage.entity.WorkflowStage;
import com.aidev.workflowmanager.stage.mapper.WorkflowStageMapper;
import com.aidev.workflowmanager.stage.vo.StageOutputParts;
import com.aidev.workflowmanager.task.entity.WorkflowTask;
import com.aidev.workflowmanager.task.mapper.WorkflowTaskMapper;
import com.aidev.workflowmanager.template.entity.WorkflowTemplate;
import com.aidev.workflowmanager.template.entity.WorkflowTemplateStage;
import com.aidev.workflowmanager.template.mapper.WorkflowTemplateMapper;
import com.aidev.workflowmanager.template.mapper.WorkflowTemplateStageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PromptServiceImpl implements PromptService {

    private static final Logger log = LoggerFactory.getLogger(PromptServiceImpl.class);

    private final WorkflowTaskMapper workflowTaskMapper;
    private final WorkflowTemplateMapper workflowTemplateMapper;
    private final WorkflowTemplateStageMapper workflowTemplateStageMapper;
    private final WorkflowStageMapper workflowStageMapper;

    public PromptServiceImpl(WorkflowTaskMapper workflowTaskMapper,
                             WorkflowTemplateMapper workflowTemplateMapper,
                             WorkflowTemplateStageMapper workflowTemplateStageMapper,
                             WorkflowStageMapper workflowStageMapper) {
        this.workflowTaskMapper = workflowTaskMapper;
        this.workflowTemplateMapper = workflowTemplateMapper;
        this.workflowTemplateStageMapper = workflowTemplateStageMapper;
        this.workflowStageMapper = workflowStageMapper;
    }

    @Override
    public GeneratePromptResponse generatePrompt(Long taskId, Long stageId) {
        WorkflowTask task = loadTask(taskId);
        WorkflowStage stage = loadStage(task.getId(), stageId);
        WorkflowTemplate template = loadTemplate(task.getMatchedTemplateId());
        WorkflowTemplateStage templateStage = loadTemplateStage(stage.getTemplateStageId());
        String previousOutputs = buildPreviousOutputs(task.getId(), stage.getStageOrder());

        Map<String, String> variables = new LinkedHashMap<String, String>();
        variables.put("taskTitle", nullToEmpty(task.getTitle()));
        variables.put("taskDescription", nullToEmpty(task.getDescription()));
        variables.put("taskType", task.getTaskType() == null ? "" : task.getTaskType().getCode());
        variables.put("complexity", task.getComplexity() == null ? "" : task.getComplexity().getCode());
        variables.put("riskTags", riskTagsToText(task.getRiskTags()));
        variables.put("workflowName", template == null ? "" : nullToEmpty(template.getName()));
        variables.put("stageName", nullToEmpty(stage.getStageName()));
        variables.put("stageGoal", templateStage == null ? nullToEmpty(stage.getInputSummary()) : nullToEmpty(templateStage.getDescription()));
        variables.put("previousStageOutputs", previousOutputs);
        variables.put("stageInstructions", buildStageInstructions(task, stage));

        GeneratePromptResponse response = new GeneratePromptResponse();
        response.setPromptContent(buildPrompt(variables, isHighRiskTask(task)));
        response.setPromptTemplateId(templateStage == null ? null : templateStage.getDefaultPromptTemplateId());
        response.setVariables(variables);
        log.info("[PROMPT] generated taskId={} stageId={} stageKey={} workflowName={} highRisk={} promptLength={}",
                taskId, stageId, stage.getStageKey(), variables.get("workflowName"), isHighRiskTask(task),
                response.getPromptContent() == null ? 0 : response.getPromptContent().length());
        return response;
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

    private WorkflowStage loadStage(Long taskId, Long stageId) {
        if (stageId == null || stageId < 1) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "stageId must be greater than or equal to 1");
        }
        WorkflowStage stage = workflowStageMapper.selectOne(new LambdaQueryWrapper<WorkflowStage>()
                .eq(WorkflowStage::getId, stageId)
                .eq(WorkflowStage::getTaskId, taskId));
        if (stage == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Workflow stage not found: " + stageId);
        }
        return stage;
    }

    private WorkflowTemplate loadTemplate(Long templateId) {
        if (templateId == null) {
            return null;
        }
        return workflowTemplateMapper.selectOne(new LambdaQueryWrapper<WorkflowTemplate>()
                .eq(WorkflowTemplate::getId, templateId));
    }

    private WorkflowTemplateStage loadTemplateStage(Long templateStageId) {
        if (templateStageId == null) {
            return null;
        }
        return workflowTemplateStageMapper.selectOne(new LambdaQueryWrapper<WorkflowTemplateStage>()
                .eq(WorkflowTemplateStage::getId, templateStageId));
    }

    private String buildPreviousOutputs(Long taskId, Integer currentOrder) {
        List<WorkflowStage> stages = workflowStageMapper.selectList(new LambdaQueryWrapper<WorkflowStage>()
                .eq(WorkflowStage::getTaskId, taskId)
                .lt(currentOrder != null, WorkflowStage::getStageOrder, currentOrder)
                .orderByAsc(WorkflowStage::getStageOrder));
        if (stages == null || stages.isEmpty()) {
            return "暂无历史阶段输出。";
        }
        return stages.stream()
                .map(stage -> stage.getStageOrder() + ". " + stage.getStageName() + "\n"
                        + StageOutputParts.parse(stage.getOutputSummary()).getOutputSummary())
                .collect(Collectors.joining("\n\n"));
    }

    private String buildStageInstructions(WorkflowTask task, WorkflowStage stage) {
        StringBuilder builder = new StringBuilder();
        builder.append("请完成当前阶段要求，输出本阶段结论、风险点、验证建议、未验证范围和下一步动作。");
        if (isHighRiskTask(task)) {
            builder.append("\n这是高风险任务，必须明确影响范围、幂等性、异常分支、回滚或补救方式。");
        }
        if ("testing".equals(stage.getStageKey())) {
            builder.append("\n测试阶段需要列出已执行命令、结果、未覆盖范围和建议人工检查项。");
        }
        if ("delivery".equals(stage.getStageKey())) {
            builder.append("\n交付阶段需要汇总变更文件、关键行为、验证结果、剩余风险和后续建议。");
        }
        return builder.toString();
    }

    private String buildPrompt(Map<String, String> variables, boolean highRiskTask) {
        StringBuilder builder = new StringBuilder();
        builder.append("你正在处理一个 AI 辅助开发任务。\n\n");
        builder.append("任务：").append(variables.get("taskTitle")).append("\n");
        builder.append("描述：").append(variables.get("taskDescription")).append("\n");
        builder.append("类型：").append(variables.get("taskType")).append("\n");
        builder.append("复杂度：").append(variables.get("complexity")).append("\n");
        builder.append("风险标签：").append(variables.get("riskTags")).append("\n");
        builder.append("Workflow：").append(variables.get("workflowName")).append("\n\n");
        builder.append("当前阶段：").append(variables.get("stageName")).append("\n");
        builder.append("阶段目标：").append(variables.get("stageGoal")).append("\n\n");
        builder.append("历史阶段输出：\n").append(variables.get("previousStageOutputs")).append("\n\n");
        builder.append("执行要求：\n").append(variables.get("stageInstructions")).append("\n\n");
        builder.append("输出格式：\n");
        builder.append("1. 本阶段结论\n2. 风险点\n3. 验证方式\n4. 未验证范围\n5. 下一步动作\n");
        if (highRiskTask) {
            builder.append("\n高风险补充要求：必须说明权限、支付、数据库、配置、文件处理、下载、金额或回调等相关风险；不涉及的项请明确写不涉及。\n");
        }
        return builder.toString();
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
}
