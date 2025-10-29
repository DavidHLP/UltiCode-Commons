package com.david.core.http;

import java.util.LinkedHashMap;
import java.util.Map;

/** 描述一次出错时的通用信息，包含状态码、错误码、错误描述以及可选的细节。 */
public record ApiError(int status, String code, String message, Map<String, Object> details) {

    public ApiError {
        details = details == null ? Map.of() : unmodifiableCopy(details);
    }

    public static ApiError of(int status, String code, String message) {
        return new ApiError(status, code, message, Map.of());
    }

    public static ApiError of(
            int status, String code, String message, Map<String, Object> details) {
        return new ApiError(status, code, message, details);
    }

    private static Map<String, Object> unmodifiableCopy(Map<String, Object> source) {
        return Map.copyOf(new LinkedHashMap<>(source));
    }
}
