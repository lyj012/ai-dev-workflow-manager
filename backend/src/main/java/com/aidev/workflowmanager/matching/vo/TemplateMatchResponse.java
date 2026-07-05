package com.aidev.workflowmanager.matching.vo;

import java.util.ArrayList;
import java.util.List;

public class TemplateMatchResponse {

    private Long taskId;

    private Long matchedTemplateId;

    private String matchedTemplateName;

    private Integer matchScore;

    private List<String> matchReasons = new ArrayList<String>();

    private Boolean autoBound;

    private List<TemplateMatchCandidateResponse> candidates = new ArrayList<TemplateMatchCandidateResponse>();

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getMatchedTemplateId() {
        return matchedTemplateId;
    }

    public void setMatchedTemplateId(Long matchedTemplateId) {
        this.matchedTemplateId = matchedTemplateId;
    }

    public String getMatchedTemplateName() {
        return matchedTemplateName;
    }

    public void setMatchedTemplateName(String matchedTemplateName) {
        this.matchedTemplateName = matchedTemplateName;
    }

    public Integer getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(Integer matchScore) {
        this.matchScore = matchScore;
    }

    public List<String> getMatchReasons() {
        return matchReasons;
    }

    public void setMatchReasons(List<String> matchReasons) {
        this.matchReasons = matchReasons;
    }

    public Boolean getAutoBound() {
        return autoBound;
    }

    public void setAutoBound(Boolean autoBound) {
        this.autoBound = autoBound;
    }

    public List<TemplateMatchCandidateResponse> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<TemplateMatchCandidateResponse> candidates) {
        this.candidates = candidates;
    }
}
