package com.aidev.workflowmanager.common.enums;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class EnumConstantsTest {

    @Test
    void enumCodesMustMatchRequirementExactly() {
        assertThat(codes(TaskType.values())).containsExactly("feature", "bugfix", "refactor", "review", "docs");
        assertThat(codes(Complexity.values())).containsExactly("SIMPLE", "MEDIUM", "COMPLEX");
        assertThat(codes(TaskStatus.values())).containsExactly("DRAFT", "ANALYZING", "EXECUTING", "TESTING",
                "DELIVERED", "ARCHIVED", "CANCELED");
        assertThat(codes(StageStatus.values())).containsExactly("PENDING", "RUNNING", "COMPLETED", "SKIPPED",
                "FAILED");
        assertThat(codes(RiskTag.values())).containsExactly("payment", "permission", "database", "config", "file",
                "auth", "download", "amount", "callback");
    }

    private String[] codes(CodeEnum[] values) {
        return Arrays.stream(values).map(CodeEnum::getCode).collect(Collectors.toList()).toArray(new String[0]);
    }
}
