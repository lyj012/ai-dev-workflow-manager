package com.aidev.workflowmanager.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RiskTag implements CodeEnum {
    PAYMENT("payment"),
    PERMISSION("permission"),
    DATABASE("database"),
    CONFIG("config"),
    FILE("file"),
    AUTH("auth"),
    DOWNLOAD("download"),
    AMOUNT("amount"),
    CALLBACK("callback");

    @EnumValue
    private final String code;

    RiskTag(String code) {
        this.code = code;
    }

    @Override
    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static RiskTag fromCode(String code) {
        return EnumParser.parseByCode(RiskTag.class, code);
    }
}
