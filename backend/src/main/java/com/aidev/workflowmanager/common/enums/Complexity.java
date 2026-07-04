package com.aidev.workflowmanager.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Complexity implements CodeEnum {
    SIMPLE("SIMPLE"),
    MEDIUM("MEDIUM"),
    COMPLEX("COMPLEX");

    @EnumValue
    private final String code;

    Complexity(String code) {
        this.code = code;
    }

    @Override
    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static Complexity fromCode(String code) {
        return EnumParser.parseByCode(Complexity.class, code);
    }
}
