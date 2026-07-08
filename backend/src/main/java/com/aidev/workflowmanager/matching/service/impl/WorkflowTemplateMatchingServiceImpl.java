package com.aidev.workflowmanager.matching.service.impl;

import com.aidev.workflowmanager.common.enums.Complexity;
import com.aidev.workflowmanager.common.enums.RiskTag;
import com.aidev.workflowmanager.common.enums.TaskStatus;
import com.aidev.workflowmanager.common.exception.BusinessException;
import com.aidev.workflowmanager.common.exception.ErrorCode;
import com.aidev.workflowmanager.matching.service.WorkflowTemplateMatchingService;
import com.aidev.workflowmanager.matching.vo.TemplateMatchCandidateResponse;
import com.aidev.workflowmanager.matching.vo.TemplateMatchResponse;
import com.aidev.workflowmanager.task.entity.WorkflowTask;
import com.aidev.workflowmanager.task.mapper.WorkflowTaskMapper;
import com.aidev.workflowmanager.template.entity.WorkflowTemplate;
import com.aidev.workflowmanager.template.mapper.WorkflowTemplateMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WorkflowTemplateMatchingServiceImpl implements WorkflowTemplateMatchingService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowTemplateMatchingServiceImpl.class);

    private final WorkflowTaskMapper workflowTaskMapper;
    private final WorkflowTemplateMapper workflowTemplateMapper;

    public WorkflowTemplateMatchingServiceImpl(WorkflowTaskMapper workflowTaskMapper,
                                               WorkflowTemplateMapper workflowTemplateMapper) {
        this.workflowTaskMapper = workflowTaskMapper;
        this.workflowTemplateMapper = workflowTemplateMapper;
    }

    @Override
    @Transactional
    public TemplateMatchResponse matchTemplate(Long taskId) {
        if (taskId == null || taskId < 1) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "任务 ID 必须大于等于 1。");
        }

        WorkflowTask task = workflowTaskMapper.selectOne(new LambdaQueryWrapper<WorkflowTask>()
                .eq(WorkflowTask::getId, taskId));
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在：" + taskId);
        }
        if (TaskStatus.ARCHIVED.equals(task.getStatus()) || TaskStatus.CANCELED.equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PARAM,
                    "当前任务状态不允许匹配 workflow：" + task.getStatus());
        }

        List<WorkflowTemplate> templates = workflowTemplateMapper.selectList(new LambdaQueryWrapper<WorkflowTemplate>()
                .eq(WorkflowTemplate::getEnabled, true));
        List<ScoredTemplate> scoredTemplates = scoreTemplates(task, templates);
        log.info("[MATCHING] candidates scored taskId={} taskType={} complexity={} riskTags={} candidateCount={}",
                taskId, task.getTaskType(), task.getComplexity(), task.getRiskTags(), scoredTemplates.size());
        if (scoredTemplates.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND,
                    "没有可匹配的启用 workflow 模板，任务 ID：" + taskId);
        }

        Collections.sort(scoredTemplates, scoredTemplateComparator());
        ScoredTemplate best = scoredTemplates.get(0);
        List<ScoredTemplate> topTies = findTopTies(scoredTemplates, best);
        if (topTies.size() == 1) {
            task.setMatchedTemplateId(best.template.getId());
            if (TaskStatus.DRAFT.equals(task.getStatus())) {
                task.setStatus(TaskStatus.ANALYZING);
            }
            workflowTaskMapper.updateById(task);
            log.info("[MATCHING] auto bound taskId={} templateId={} templateName={} score={}",
                    taskId, best.template.getId(), best.template.getName(), best.score);
            return buildAutoBoundResponse(taskId, best, scoredTemplates);
        }
        log.info("[MATCHING] ambiguous taskId={} topScore={} tieCount={}", taskId, best.score, topTies.size());
        return buildAmbiguousResponse(taskId, best, topTies);
    }

    private List<ScoredTemplate> scoreTemplates(WorkflowTask task, List<WorkflowTemplate> templates) {
        if (templates == null || templates.isEmpty()) {
            return Collections.emptyList();
        }
        List<ScoredTemplate> result = new ArrayList<ScoredTemplate>();
        boolean highRiskTask = isHighRiskTask(task);
        Set<RiskTag> taskRiskTags = toRiskTagSet(task.getRiskTags());

        for (WorkflowTemplate template : templates) {
            int score = 0;
            List<String> reasons = new ArrayList<String>();

            if (task.getTaskType() != null && task.getTaskType().equals(template.getTaskType())) {
                score += 40;
                reasons.add("任务类型匹配：" + task.getTaskType().getCode());
            }
            if (task.getComplexity() != null && task.getComplexity().equals(template.getComplexity())) {
                score += 30;
                reasons.add("复杂度匹配：" + task.getComplexity().getCode());
            }

            Set<RiskTag> templateRiskTags = toRiskTagSet(template.getRiskTags());
            for (RiskTag riskTag : taskRiskTags) {
                if (templateRiskTags.contains(riskTag)) {
                    score += 30;
                    reasons.add("风险标签匹配：" + riskTag.getCode());
                }
            }

            if (highRiskTask && isHighRiskTemplate(template)) {
                score += 100;
                reasons.add("任务包含高风险因素，优先匹配高风险 workflow");
            }

            if (score > 0) {
                result.add(new ScoredTemplate(template, score, reasons));
            }
        }
        return result;
    }

    private boolean isHighRiskTask(WorkflowTask task) {
        return Complexity.COMPLEX.equals(task.getComplexity()) || !toRiskTagSet(task.getRiskTags()).isEmpty();
    }

    private boolean isHighRiskTemplate(WorkflowTemplate template) {
        return Complexity.COMPLEX.equals(template.getComplexity()) && !toRiskTagSet(template.getRiskTags()).isEmpty();
    }

    private Set<RiskTag> toRiskTagSet(List<RiskTag> riskTags) {
        Set<RiskTag> result = new HashSet<RiskTag>();
        if (riskTags == null) {
            return result;
        }
        for (RiskTag riskTag : riskTags) {
            if (riskTag != null) {
                result.add(riskTag);
            }
        }
        return result;
    }

    private Comparator<ScoredTemplate> scoredTemplateComparator() {
        return (left, right) -> {
            int scoreCompare = right.score.compareTo(left.score);
            if (scoreCompare != 0) {
                return scoreCompare;
            }
            int priorityCompare = Integer.valueOf(nullToZero(right.template.getPriority()))
                    .compareTo(nullToZero(left.template.getPriority()));
            if (priorityCompare != 0) {
                return priorityCompare;
            }
            int versionCompare = Integer.valueOf(nullToZero(right.template.getVersion()))
                    .compareTo(nullToZero(left.template.getVersion()));
            if (versionCompare != 0) {
                return versionCompare;
            }
            return Long.valueOf(nullToMax(left.template.getId())).compareTo(nullToMax(right.template.getId()));
        };
    }

    private List<ScoredTemplate> findTopTies(List<ScoredTemplate> scoredTemplates, ScoredTemplate best) {
        return scoredTemplates.stream()
                .filter(candidate -> best.score.equals(candidate.score)
                        && nullToZero(best.template.getPriority()) == nullToZero(candidate.template.getPriority())
                        && nullToZero(best.template.getVersion()) == nullToZero(candidate.template.getVersion()))
                .collect(Collectors.toList());
    }

    private TemplateMatchResponse buildAutoBoundResponse(Long taskId, ScoredTemplate winner,
                                                         List<ScoredTemplate> scoredTemplates) {
        TemplateMatchResponse response = new TemplateMatchResponse();
        response.setTaskId(taskId);
        response.setMatchedTemplateId(winner.template.getId());
        response.setMatchedTemplateName(winner.template.getName());
        response.setMatchScore(winner.score);
        response.setMatchReasons(new ArrayList<String>(winner.reasons));
        response.setAutoBound(true);
        response.setCandidates(toCandidateResponses(scoredTemplates));
        return response;
    }

    private TemplateMatchResponse buildAmbiguousResponse(Long taskId, ScoredTemplate best,
                                                         List<ScoredTemplate> topTies) {
        TemplateMatchResponse response = new TemplateMatchResponse();
        response.setTaskId(taskId);
        response.setMatchedTemplateId(null);
        response.setMatchedTemplateName(null);
        response.setMatchScore(best.score);
        List<String> reasons = new ArrayList<String>(best.reasons);
        reasons.add("存在多个同分候选模板，需要人工选择");
        response.setMatchReasons(reasons);
        response.setAutoBound(false);
        response.setCandidates(toCandidateResponses(topTies));
        return response;
    }

    private List<TemplateMatchCandidateResponse> toCandidateResponses(List<ScoredTemplate> scoredTemplates) {
        return scoredTemplates.stream()
                .map(this::toCandidateResponse)
                .collect(Collectors.toList());
    }

    private TemplateMatchCandidateResponse toCandidateResponse(ScoredTemplate scoredTemplate) {
        TemplateMatchCandidateResponse response = new TemplateMatchCandidateResponse();
        response.setTemplateId(scoredTemplate.template.getId());
        response.setTemplateName(scoredTemplate.template.getName());
        response.setMatchScore(scoredTemplate.score);
        response.setMatchReasons(new ArrayList<String>(scoredTemplate.reasons));
        response.setPriority(scoredTemplate.template.getPriority());
        response.setVersion(scoredTemplate.template.getVersion());
        return response;
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    private long nullToMax(Long value) {
        return value == null ? Long.MAX_VALUE : value;
    }

    private static class ScoredTemplate {
        private final WorkflowTemplate template;
        private final Integer score;
        private final List<String> reasons;

        private ScoredTemplate(WorkflowTemplate template, Integer score, List<String> reasons) {
            this.template = template;
            this.score = score;
            this.reasons = reasons;
        }
    }
}
