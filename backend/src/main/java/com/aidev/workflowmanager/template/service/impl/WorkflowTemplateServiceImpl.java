package com.aidev.workflowmanager.template.service.impl;

import com.aidev.workflowmanager.common.enums.Complexity;
import com.aidev.workflowmanager.common.enums.RiskTag;
import com.aidev.workflowmanager.common.enums.TaskType;
import com.aidev.workflowmanager.common.exception.BusinessException;
import com.aidev.workflowmanager.common.exception.ErrorCode;
import com.aidev.workflowmanager.template.entity.WorkflowTemplate;
import com.aidev.workflowmanager.template.entity.WorkflowTemplateStage;
import com.aidev.workflowmanager.template.mapper.WorkflowTemplateMapper;
import com.aidev.workflowmanager.template.mapper.WorkflowTemplateStageMapper;
import com.aidev.workflowmanager.template.service.WorkflowTemplateService;
import com.aidev.workflowmanager.template.vo.WorkflowTemplateResponse;
import com.aidev.workflowmanager.template.vo.WorkflowTemplateStageResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkflowTemplateServiceImpl implements WorkflowTemplateService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowTemplateServiceImpl.class);

    private final WorkflowTemplateMapper workflowTemplateMapper;
    private final WorkflowTemplateStageMapper workflowTemplateStageMapper;

    public WorkflowTemplateServiceImpl(WorkflowTemplateMapper workflowTemplateMapper,
                                       WorkflowTemplateStageMapper workflowTemplateStageMapper) {
        this.workflowTemplateMapper = workflowTemplateMapper;
        this.workflowTemplateStageMapper = workflowTemplateStageMapper;
    }

    @Override
    @Transactional
    public void ensureBuiltInTemplates() {
        ensureTemplate(new BuiltInTemplate(
                "简单任务 workflow",
                "适用于文档任务、单文件小修改和低风险任务。",
                TaskType.DOCS,
                Complexity.SIMPLE,
                Collections.<RiskTag>emptyList(),
                10,
                Arrays.asList(
                        stage("analysis", "需求分析", "分析任务目标、影响范围和验收标准。", true),
                        stage("implementation", "执行修改", "按确认范围完成最小必要修改。", true),
                        stage("testing", "验证检查", "执行必要测试与人工检查。", true),
                        stage("delivery", "交付总结", "整理交付内容、验证结果和剩余风险。", true)
                )));

        ensureTemplate(new BuiltInTemplate(
                "标准开发 workflow",
                "适用于普通功能开发、中等复杂度 bugfix 和可控范围内的联动修改。",
                TaskType.FEATURE,
                Complexity.MEDIUM,
                Collections.<RiskTag>emptyList(),
                20,
                Arrays.asList(
                        stage("analysis", "需求分析", "分析需求目标、业务流程、影响范围和验收标准。", true),
                        stage("design", "方案设计", "设计实现方案、接口数据结构和验证策略。", true),
                        stage("implementation", "实现修改", "按方案完成代码实现并保持最小改动。", true),
                        stage("review", "代码审查", "审查正确性、边界、风险和项目规范一致性。", true),
                        stage("testing", "测试验证", "执行单元测试、接口验证或必要人工验证。", true),
                        stage("delivery", "交付总结", "汇总变更、测试结果、风险和后续建议。", true)
                )));

        ensureTemplate(new BuiltInTemplate(
                "高风险 workflow",
                "适用于复杂任务，以及涉及支付、权限、数据库、配置、文件、认证、下载、金额或回调的任务。",
                TaskType.FEATURE,
                Complexity.COMPLEX,
                Arrays.asList(RiskTag.values()),
                100,
                Arrays.asList(
                        stage("analysis", "需求分析", "分析需求目标、业务流程、状态流转和历史兼容。", true),
                        stage("risk_review", "风险评审", "复核高风险点、权限、幂等、异常分支和回滚方案。", true),
                        stage("design", "方案设计", "设计低侵入实现方案、接口数据结构和风险控制策略。", true),
                        stage("implementation", "实现修改", "按方案完成代码实现，避免扩大高风险影响面。", true),
                        stage("review", "代码审查", "重点审查正确性、边界、权限、幂等和状态一致性。", true),
                        stage("testing", "测试验证", "执行覆盖高风险路径的测试和必要人工复核。", true),
                        stage("delivery", "交付总结", "汇总变更、验证结果、风险说明和未验证范围。", true)
                )));
    }

    @Override
    public List<WorkflowTemplateResponse> listEnabledTemplates() {
        List<WorkflowTemplate> templates = workflowTemplateMapper.selectList(new LambdaQueryWrapper<WorkflowTemplate>()
                .eq(WorkflowTemplate::getEnabled, true)
                .orderByDesc(WorkflowTemplate::getPriority)
                .orderByDesc(WorkflowTemplate::getVersion)
                .orderByAsc(WorkflowTemplate::getId));
        List<WorkflowTemplateResponse> responses = templates.stream()
                .map(template -> WorkflowTemplateResponse.from(template, loadStageResponses(template.getId())))
                .collect(Collectors.toList());
        log.info("[TEMPLATE] list loaded enabledOnly=true templateCount={}", responses.size());
        return responses;
    }

    @Override
    public WorkflowTemplateResponse detail(Long templateId) {
        if (templateId == null || templateId < 1) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "templateId must be greater than or equal to 1");
        }
        WorkflowTemplate template = workflowTemplateMapper.selectOne(new LambdaQueryWrapper<WorkflowTemplate>()
                .eq(WorkflowTemplate::getId, templateId));
        if (template == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Workflow template not found: " + templateId);
        }
        List<WorkflowTemplateStageResponse> stages = loadStageResponses(template.getId());
        log.info("[TEMPLATE] detail loaded templateId={} templateName={} enabled={} stageCount={}",
                template.getId(), template.getName(), template.getEnabled(), stages.size());
        return WorkflowTemplateResponse.from(template, stages);
    }

    private List<WorkflowTemplateStageResponse> loadStageResponses(Long templateId) {
        List<WorkflowTemplateStage> stages = workflowTemplateStageMapper.selectList(new LambdaQueryWrapper<WorkflowTemplateStage>()
                .eq(WorkflowTemplateStage::getTemplateId, templateId)
                .orderByAsc(WorkflowTemplateStage::getStageOrder));
        return stages.stream()
                .map(WorkflowTemplateStageResponse::from)
                .collect(Collectors.toList());
    }

    private void ensureTemplate(BuiltInTemplate builtInTemplate) {
        WorkflowTemplate template = workflowTemplateMapper.selectOne(new LambdaQueryWrapper<WorkflowTemplate>()
                .eq(WorkflowTemplate::getName, builtInTemplate.name)
                .eq(WorkflowTemplate::getVersion, 1));
        if (template == null) {
            template = new WorkflowTemplate();
            template.setName(builtInTemplate.name);
            template.setDescription(builtInTemplate.description);
            template.setTaskType(builtInTemplate.taskType);
            template.setComplexity(builtInTemplate.complexity);
            template.setRiskTags(new ArrayList<RiskTag>(builtInTemplate.riskTags));
            template.setPriority(builtInTemplate.priority);
            template.setVersion(1);
            template.setEnabled(true);
            template.setDeleted(0);
            workflowTemplateMapper.insert(template);
        }

        for (int i = 0; i < builtInTemplate.stages.size(); i++) {
            StageDefinition stageDefinition = builtInTemplate.stages.get(i);
            int stageOrder = i + 1;
            WorkflowTemplateStage existingStage = workflowTemplateStageMapper.selectOne(new LambdaQueryWrapper<WorkflowTemplateStage>()
                    .eq(WorkflowTemplateStage::getTemplateId, template.getId())
                    .eq(WorkflowTemplateStage::getStageOrder, stageOrder));
            if (existingStage != null) {
                if (!stageDefinition.stageKey.equals(existingStage.getStageKey())) {
                    throw new BusinessException(ErrorCode.INVALID_PARAM,
                            "Built-in workflow template stage conflict for template " + template.getId()
                                    + " order " + stageOrder + ": expected key " + stageDefinition.stageKey
                                    + " but found " + existingStage.getStageKey());
                }
                if (!stageDefinition.required.equals(existingStage.getRequired())) {
                    throw new BusinessException(ErrorCode.INVALID_PARAM,
                            "Built-in workflow template stage conflict for template " + template.getId()
                                    + " order " + stageOrder + " key " + stageDefinition.stageKey
                                    + ": expected required " + stageDefinition.required
                                    + " but found " + existingStage.getRequired());
                }
                continue;
            }
            WorkflowTemplateStage stage = new WorkflowTemplateStage();
            stage.setTemplateId(template.getId());
            stage.setStageKey(stageDefinition.stageKey);
            stage.setStageName(stageDefinition.stageName);
            stage.setStageOrder(stageOrder);
            stage.setRequired(stageDefinition.required);
            stage.setDefaultPromptTemplateId(null);
            stage.setDescription(stageDefinition.description);
            stage.setDeleted(0);
            workflowTemplateStageMapper.insert(stage);
        }
    }

    private StageDefinition stage(String stageKey, String stageName, String description, boolean required) {
        return new StageDefinition(stageKey, stageName, description, required);
    }

    private static class BuiltInTemplate {
        private final String name;
        private final String description;
        private final TaskType taskType;
        private final Complexity complexity;
        private final List<RiskTag> riskTags;
        private final Integer priority;
        private final List<StageDefinition> stages;

        private BuiltInTemplate(String name, String description, TaskType taskType, Complexity complexity,
                                List<RiskTag> riskTags, Integer priority, List<StageDefinition> stages) {
            this.name = name;
            this.description = description;
            this.taskType = taskType;
            this.complexity = complexity;
            this.riskTags = riskTags;
            this.priority = priority;
            this.stages = stages;
        }
    }

    private static class StageDefinition {
        private final String stageKey;
        private final String stageName;
        private final String description;
        private final Boolean required;

        private StageDefinition(String stageKey, String stageName, String description, Boolean required) {
            this.stageKey = stageKey;
            this.stageName = stageName;
            this.description = description;
            this.required = required;
        }
    }
}
