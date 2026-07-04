package com.aidev.workflowmanager.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskStatus implements CodeEnum {
    DRAFT("DRAFT"),
    ANALYZING("ANALYZING"),
    EXECUTING("EXECUTING"),
    TESTING("TESTING"),
    DELIVERED("DELIVERED"),
    ARCHIVED("ARCHIVED"),
    CANCELED("CANCELED");

    @EnumValue
    private final String code;

    TaskStatus(String code) {
        this.code = code;
    }

    @Override
    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static TaskStatus fromCode(String code) {
        return EnumParser.parseByCode(TaskStatus.class, code);
    }
}
