package com.aidev.workflowmanager.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskType implements CodeEnum {
    FEATURE("feature"),
    BUGFIX("bugfix"),
    REFACTOR("refactor"),
    REVIEW("review"),
    DOCS("docs");

    @EnumValue
    private final String code;

    TaskType(String code) {
        this.code = code;
    }

    @Override
    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static TaskType fromCode(String code) {
        return EnumParser.parseByCode(TaskType.class, code);
    }
}
