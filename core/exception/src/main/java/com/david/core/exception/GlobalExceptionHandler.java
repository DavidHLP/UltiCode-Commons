package com.david.core.exception;

import com.david.core.http.ApiError;
import com.david.core.http.ApiResponse;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMissingRequestHeader(MissingRequestHeaderException ex) {
        String header = ex.getHeaderName();
        return ApiResponse.failure(
                ApiError.of(HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST", "缺少必填请求头: " + header));
    }

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(
            BusinessException ex, HttpServletResponse response) {
        response.setStatus(ex.getStatus().value());
        log.warn("业务异常: {}", ex.getMessage());
        ApiError error =
                ApiError.of(ex.getStatus().value(), ex.getStatus().name(), ex.getMessage());
        return ApiResponse.failure(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ApiResponse<Void> handleBadCredentials(
            BadCredentialsException ex, HttpServletResponse response) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        ApiError error =
                ApiError.of(
                        HttpStatus.UNAUTHORIZED.value(),
                        HttpStatus.UNAUTHORIZED.name(),
                        ex.getMessage());
        return ApiResponse.failure(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ApiResponse<Void> handleAuthenticationException(
            AuthenticationException ex, HttpServletResponse response) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        ApiError error =
                ApiError.of(
                        HttpStatus.UNAUTHORIZED.value(),
                        HttpStatus.UNAUTHORIZED.name(),
                        ex.getMessage());
        return ApiResponse.failure(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponse<Void> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletResponse response) {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        ApiError error =
                ApiError.of(
                        HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(), ex.getMessage());
        return ApiResponse.failure(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletResponse response) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        Map<String, Object> details = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ApiError error =
                ApiError.of(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.name(),
                        "Validation failed",
                        details);
        return ApiResponse.failure(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletResponse response) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        ApiError error =
                ApiError.of(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.name(),
                        ex.getMessage());
        return ApiResponse.failure(error);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleGenericException(Exception ex, HttpServletResponse response) {
        log.error("未捕获异常", ex);
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        ApiError error =
                ApiError.of(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR.name(),
                        "系统繁忙，请稍后重试");
        return ApiResponse.failure(error);
    }
}
