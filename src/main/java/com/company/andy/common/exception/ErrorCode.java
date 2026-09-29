package com.company.andy.common.exception;

import lombok.Getter;

//400：Bad request
//401：Authentication failed
//403：Authorization failed
//404：Not found
//409：Business exception
//500：System error
@Getter
public enum ErrorCode {
    //400: Bad request
    BAD_REQUEST(400),
    REQUEST_VALIDATION_FAILED(400),

    //401: Authentication failed
    AUTHENTICATION_FAILED(401),

    //403: Authorization failed
    ACCESS_DENIED(403),

    //404: Not found
    NOT_FOUND(404),
    AR_NOT_FOUND(404),
    MAINTENANCE_RECORD_NOT_FOUND(404),

    //405
    METHOD_NOT_ALLOWED(405),

    //409: Business exception
    CONFLICT(409),
    NOT_SAME_ORG(409),
    EQUIPMENT_NAME_ALREADY_EXISTS(409),

    //429
    TOO_MANY_REQUEST(429),

    //500
    SYSTEM_ERROR(500);

    private final int status;

    ErrorCode(int status) {
        this.status = status;
    }

}
