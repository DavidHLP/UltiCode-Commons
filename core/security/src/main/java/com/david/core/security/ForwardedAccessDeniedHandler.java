package com.david.core.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/** 权限不足时返回统一格式。 */
public record ForwardedAccessDeniedHandler(
        ForwardedErrorResponseWriter writer, ForwardedSecurityProperties properties)
        implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException {
        writer.write(response, HttpStatus.FORBIDDEN, properties.getAccessDeniedMessage());
    }
}
