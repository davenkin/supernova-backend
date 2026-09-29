package com.company.andy.common.exception;

import com.company.andy.common.tracing.TracingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

import static com.company.andy.common.exception.ErrorCode.*;
import static com.company.andy.common.exception.ServiceException.systemException;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.valueOf;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final TracingService tracingService;

    @ResponseBody
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<QApiErrorResponse> handleServiceException(ServiceException ex, HttpServletRequest request) {
        log.error("Exception occurred while access[{}]: [{}]{}", request.getRequestURI(), ex.getCode(), ex.toDetailMessage(), ex);
        return createErrorResponse(ex, request);
    }

    @ResponseBody
    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<QApiErrorResponse> handleAccessDeniedException(HttpServletRequest request) {
        return createErrorResponse(new ServiceException(ACCESS_DENIED, "Access Denied."), request);
    }

    @ResponseBody
    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<QApiErrorResponse> handleAuthenticationFailedException(HttpServletRequest request) {
        return createErrorResponse(new ServiceException(AUTHENTICATION_FAILED, "Authentication failed."), request);
    }

    @ResponseBody
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<QApiErrorResponse> handleInvalidRequest(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, Object> error = ex.getBindingResult().getFieldErrors().stream()
                .collect(toImmutableMap(FieldError::getField, fieldError -> {
                    String message = fieldError.getDefaultMessage();
                    return isBlank(message) ? "No message available." : message;
                }, (field1, field2) -> field1 + "|" + field2));

        log.error("Method argument validation error while access[{}:{}]: {}",
                request.getRequestURI(),
                ex.getParameter().getParameterType().getName(),
                error,
                ex);
        ServiceException exception = new ServiceException(REQUEST_VALIDATION_FAILED, "Method argument validation failed.", error);
        return createErrorResponse(exception, request);
    }

    @ResponseBody
    @ExceptionHandler({ServletRequestBindingException.class, HttpMessageNotReadableException.class, ConstraintViolationException.class})
    public ResponseEntity<QApiErrorResponse> handleServletRequestBindingException(Exception ex, HttpServletRequest request) {
        log.error("Request processing error while access[{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        return createErrorResponse(new ServiceException(REQUEST_VALIDATION_FAILED, "Request validation failed."), request);
    }

    @ResponseBody
    @ExceptionHandler({NoResourceFoundException.class})
    public ResponseEntity<QApiErrorResponse> handleNoResourceFoundException(HttpServletRequest request) {
        return createErrorResponse(new ServiceException(NOT_FOUND, "Not found."), request);
    }

    @ResponseBody
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<?> handleGeneralException(Throwable ex, HttpServletRequest request) {
        log.error("Error access[{}]:", request.getRequestURI(), ex);
        return createErrorResponse(systemException(ex), request);
    }

    private ResponseEntity<QApiErrorResponse> createErrorResponse(ServiceException exception, HttpServletRequest request) {
        String traceId = tracingService.currentTraceId();
        ApiError error = new ApiError(exception, request.getMethod(), request.getRequestURI(), traceId);
        QApiErrorResponse representation = error.toErrorResponse();
        return new ResponseEntity<>(representation, new HttpHeaders(), valueOf(representation.error().status()));
    }
}
