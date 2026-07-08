package com.aidev.workflowmanager.aitask.domain;

import com.aidev.workflowmanager.common.enums.CodeEnum;
import com.aidev.workflowmanager.common.enums.EnumParser;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AiTaskStatus implements CodeEnum {
    CREATED("CREATED"),
    ANALYZING("ANALYZING"),
    SUCCESS("SUCCESS"),
    FAILED("FAILED");

    @EnumValue
    private final String code;

    AiTaskStatus(String code) {
        this.code = code;
    }

    @Override
    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static AiTaskStatus fromCode(String code) {
        return EnumParser.parseByCode(AiTaskStatus.class, code);
    }
}
