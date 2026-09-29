package com.company.andy.common.exception;

import com.company.andy.common.tracing.TracingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.webmvc.autoconfigure.error.AbstractErrorController;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.company.andy.common.exception.ErrorCode.SYSTEM_ERROR;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.boot.web.error.ErrorAttributeOptions.Include.values;
import static org.springframework.boot.web.error.ErrorAttributeOptions.defaults;

@Tag(name = "Z:RestErrorController", description = "Internal use only")
@Slf4j
@RestController
public class RestErrorController extends AbstractErrorController {
    private static final Map<HttpStatus, String> GENERIC_MESSAGES = Map.of(
            HttpStatus.UNAUTHORIZED, "Authentication failed.",
            HttpStatus.FORBIDDEN, "Access denied.",
            HttpStatus.BAD_REQUEST, "Bad request.",
            HttpStatus.NOT_FOUND, "Not found.",
            HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed.",
            HttpStatus.CONFLICT, "Conflict.");
    private static final Map<HttpStatus, ErrorCode> STATUS_ERROR_CODES = Map.of(
            HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST,
            HttpStatus.UNAUTHORIZED, ErrorCode.AUTHENTICATION_FAILED,
            HttpStatus.FORBIDDEN, ErrorCode.ACCESS_DENIED,
            HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND,
            HttpStatus.METHOD_NOT_ALLOWED, ErrorCode.METHOD_NOT_ALLOWED,
            HttpStatus.CONFLICT, ErrorCode.CONFLICT);
    private final TracingService tracingService;

    public RestErrorController(ErrorAttributes errorAttributes, TracingService tracingService) {
        super(errorAttributes);
        this.tracingService = tracingService;
    }

    @RequestMapping("${server.error.path:${error.path:/error}}")
    public ResponseEntity<QApiErrorResponse> handleError(HttpServletRequest webRequest) {
        HttpStatus status = getStatus(webRequest);
        ErrorCode errorCode = getErrorCode(status);
        Map<String, Object> errorAttributes = getErrorAttributes(webRequest, defaults().including(values()));
        String message = getMessage(status, errorAttributes);
        String path = (String) errorAttributes.get("path");
        String traceId = tracingService.currentTraceId();
        log.error("Error happened when access[{}]:{}", path, message);
        ApiError errorDetail = new ApiError(errorCode, message, webRequest.getMethod(), path, traceId, null);
        return new ResponseEntity<>(errorDetail.toErrorResponse(), new HttpHeaders(), status);
    }

    private String getMessage(HttpStatus status, Map<String, Object> errorAttributes) {
        String genericMessage = GENERIC_MESSAGES.get(status);
        if (isNotBlank(genericMessage)) {
            return genericMessage;
        }

        String message = (String) errorAttributes.get("message");
        if (isNotBlank(message)) {
            return message;
        }

        return "System error.";
    }

    private ErrorCode getErrorCode(HttpStatus status) {
        ErrorCode errorCode = STATUS_ERROR_CODES.get(status);
        return errorCode != null ? errorCode : SYSTEM_ERROR;
    }
}
