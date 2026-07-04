package com.aidev.workflowmanager.common.exception;

public enum ErrorCode {
    SUCCESS(0, "success"),
    INVALID_PARAM(400, "Invalid request parameter"),
    NOT_FOUND(404, "Resource not found"),
    INTERNAL_ERROR(500, "Internal server error");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
