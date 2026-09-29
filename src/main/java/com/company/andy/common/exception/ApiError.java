package com.company.andy.common.exception;

import java.time.Instant;
import java.util.Map;

import static java.time.Instant.now;

public record ApiError(
        ErrorCode code,
        String message,
        int status,
        String path,
        Instant timestamp,
        String traceId,
        Map<String, Object> data) {

    public ApiError(ServiceException ex, String method, String path, String traceId) {
        ErrorCode errorCode = ex.getCode();
        this(errorCode, ex.getMessage(), errorCode.getStatus(), method + ":" + path, now(), traceId, ex.getData());
    }

    public ApiError(ErrorCode code, String message, String method, String path, String traceId, Map<String, Object> data) {
        this(code, message, code.getStatus(), method + ":" + path, now(), traceId, data);
    }

    public QApiErrorResponse toErrorResponse() {
        return new QApiErrorResponse(this);
    }
}
