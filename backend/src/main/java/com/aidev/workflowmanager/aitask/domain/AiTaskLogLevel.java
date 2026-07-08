package com.aidev.workflowmanager.aitask.domain;

import com.aidev.workflowmanager.common.enums.CodeEnum;
import com.aidev.workflowmanager.common.enums.EnumParser;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AiTaskLogLevel implements CodeEnum {
    INFO("INFO"),
    WARN("WARN"),
    ERROR("ERROR");

    @EnumValue
    private final String code;

    AiTaskLogLevel(String code) {
        this.code = code;
    }

    @Override
    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static AiTaskLogLevel fromCode(String code) {
        return EnumParser.parseByCode(AiTaskLogLevel.class, code);
    }
}
