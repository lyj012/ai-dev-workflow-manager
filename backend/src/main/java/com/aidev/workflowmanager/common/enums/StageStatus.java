package com.aidev.workflowmanager.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StageStatus implements CodeEnum {
    PENDING("PENDING"),
    RUNNING("RUNNING"),
    COMPLETED("COMPLETED"),
    SKIPPED("SKIPPED"),
    FAILED("FAILED");

    @EnumValue
    private final String code;

    StageStatus(String code) {
        this.code = code;
    }

    @Override
    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static StageStatus fromCode(String code) {
        return EnumParser.parseByCode(StageStatus.class, code);
    }
}
