package com.aidev.workflowmanager.aitask.domain;

import com.aidev.workflowmanager.common.enums.CodeEnum;
import com.aidev.workflowmanager.common.enums.EnumParser;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AiTaskType implements CodeEnum {
    REQUIREMENT_ANALYSIS("requirement_analysis"),
    DEVELOPMENT_PLAN("development_plan"),
    RISK_REVIEW("risk_review");

    @EnumValue
    private final String code;

    AiTaskType(String code) {
        this.code = code;
    }

    @Override
    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static AiTaskType fromCode(String code) {
        return EnumParser.parseByCode(AiTaskType.class, code);
    }
}
