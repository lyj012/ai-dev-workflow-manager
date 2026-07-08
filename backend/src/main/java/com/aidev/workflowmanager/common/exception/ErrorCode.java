package com.aidev.workflowmanager.common.exception;

public enum ErrorCode {
    SUCCESS(0, "成功"),
    INVALID_PARAM(400, "请求参数无效"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统内部错误");

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
