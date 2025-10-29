package com.david.core.security;

import com.david.core.http.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** 以统一格式向客户端输出安全异常响应。 */
record ForwardedErrorResponseWriter(ObjectMapper objectMapper) {

    ForwardedErrorResponseWriter(@Nullable ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        String resolvedMessage = StringUtils.hasText(message) ? message : status.getReasonPhrase();
        ApiResponse<Void> payload =
                ApiResponse.failure(status.value(), status.name(), resolvedMessage);
        byte[] body = serialize(payload, status, resolvedMessage);
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        response.getOutputStream().write(body);
        response.getOutputStream().flush();
    }

    private byte[] serialize(ApiResponse<Void> payload, HttpStatus status, String fallbackMessage) {
        try {
            return objectMapper.writeValueAsBytes(payload);
        } catch (JsonProcessingException ex) {
            String fallback =
                    "{\"status\":"
                            + status.value()
                            + ",\"code\":\""
                            + status.name()
                            + "\",\"message\":\""
                            + escape(fallbackMessage)
                            + "\"}";
            return fallback.getBytes(StandardCharsets.UTF_8);
        }
    }

    private String escape(String input) {
        return input.replace("\"", "\\\"");
    }
}
