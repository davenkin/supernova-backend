package com.company.andy.common.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

import static com.company.andy.common.exception.ErrorCode.SYSTEM_ERROR;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

// This is the only exception that you should throw as we follow a flat exception model
// When thrown, pass in an ErrorCode that categorize the error
@Getter
public final class ServiceException extends RuntimeException {
    private final ErrorCode code;
    private Map<String, Object> data;

    public ServiceException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ServiceException(ErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public ServiceException(
            ErrorCode code, String message,
            String key, Object value) {
        super(message);
        this.code = code;
        addData(key, value);
    }

    public ServiceException(
            ErrorCode code, String message,
            String key, Object value,
            Throwable cause) {
        super(message, cause);
        this.code = code;
        addData(key, value);
    }

    public ServiceException(
            ErrorCode code, String message,
            String key1, Object value1,
            String key2, Object value2) {
        super(message);
        this.code = code;
        addData(key1, value1);
        addData(key2, value2);
    }

    public ServiceException(
            ErrorCode code, String message,
            String key1, Object value1,
            String key2, Object value2,
            Throwable cause) {
        super(message, cause);
        this.code = code;
        addData(key1, value1);
        addData(key2, value2);
    }

    public ServiceException(
            ErrorCode code, String message,
            String key1, Object value1,
            String key2, Object value2,
            String key3, Object value3) {
        super(message);
        this.code = code;
        addData(key1, value1);
        addData(key2, value2);
        addData(key3, value3);
    }

    public ServiceException(
            ErrorCode code, String message,
            String key1, Object value1,
            String key2, Object value2,
            String key3, Object value3,
            Throwable cause) {
        super(message, cause);
        this.code = code;
        addData(key1, value1);
        addData(key2, value2);
        addData(key3, value3);
    }

    public ServiceException(
            ErrorCode code, String message,
            String key1, Object value1,
            String key2, Object value2,
            String key3, Object value3,
            String key4, Object value4) {
        super(message);
        this.code = code;
        addData(key1, value1);
        addData(key2, value2);
        addData(key3, value3);
        addData(key4, value4);
    }

    public ServiceException(
            ErrorCode code, String message,
            String key1, Object value1,
            String key2, Object value2,
            String key3, Object value3,
            String key4, Object value4,
            Throwable cause) {
        super(message, cause);
        this.code = code;
        addData(key1, value1);
        addData(key2, value2);
        addData(key3, value3);
        addData(key4, value4);
    }

    public ServiceException(
            ErrorCode code, String message,
            String key1, Object value1,
            String key2, Object value2,
            String key3, Object value3,
            String key4, Object value4,
            String key5, Object value5) {
        super(message);
        this.code = code;
        addData(key1, value1);
        addData(key2, value2);
        addData(key3, value3);
        addData(key4, value4);
        addData(key5, value5);
    }

    public ServiceException(
            ErrorCode code, String message,
            String key1, Object value1,
            String key2, Object value2,
            String key3, Object value3,
            String key4, Object value4,
            String key5, Object value5,
            Throwable cause) {
        super(message, cause);
        this.code = code;
        addData(key1, value1);
        addData(key2, value2);
        addData(key3, value3);
        addData(key4, value4);
        addData(key5, value5);
    }

    public ServiceException(ErrorCode code, String message, Map<String, Object> data) {
        super(message);
        this.code = code;
        this.data = data;
    }

    public ServiceException(ErrorCode code, String message, Map<String, Object> data, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.data = data;
    }

    public static ServiceException systemException(Throwable ex) {
        return new ServiceException(SYSTEM_ERROR, "System error.", ex);
    }

    public String toDetailMessage() {
        StringBuilder stringBuilder = new StringBuilder().append("[").append(this.code.toString()).append("]");

        if (isNotBlank(this.getMessage())) {
            stringBuilder.append(this.getMessage());
        }

        if (isNotEmpty(this.data)) {
            stringBuilder.append("|Data: ").append(this.data);
        }

        return stringBuilder.toString();
    }

    public void addData(String key, Object value) {
        requireNonNull(key, "key should not be null");

        if (this.data == null) {
            this.data = new HashMap<>();
        }
        this.data.put(key, value);
    }
}
