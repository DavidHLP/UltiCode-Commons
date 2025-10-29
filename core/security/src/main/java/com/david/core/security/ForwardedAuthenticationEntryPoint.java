package com.david.core.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/** 未认证访问时返回统一的 JSON 响应。 */
public record ForwardedAuthenticationEntryPoint(
        ForwardedErrorResponseWriter writer, ForwardedSecurityProperties properties)
        implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException {
        writer.write(response, HttpStatus.UNAUTHORIZED, properties.getUnauthorizedMessage());
    }
}
