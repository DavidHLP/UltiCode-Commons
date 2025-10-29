package com.david.core.http;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 控制器统一返回结构，便于各微服务之间扩展与协作。
 *
 * @param isSuccess 标识本次调用是否成功
 * @param data 成功时的业务数据
 * @param error 失败时的错误描述
 * @param timestamp 响应产生时间
 * @param metadata 额外的扩展字段
 * @param <T> 业务数据类型
 */
public record ApiResponse<T>(
        boolean isSuccess,
        T data,
        ApiError error,
        OffsetDateTime timestamp,
        Map<String, Object> metadata) {

    public ApiResponse {
        timestamp = timestamp == null ? OffsetDateTime.now() : timestamp;
        metadata = metadata == null ? Map.of() : unmodifiableCopy(metadata);
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, Map.of());
    }

    public static <T> ApiResponse<T> success(T data, Map<String, Object> metadata) {
        return new ApiResponse<>(true, data, null, OffsetDateTime.now(), metadata);
    }

    public static <T> ApiResponse<T> failure(ApiError error) {
        return new ApiResponse<>(false, null, error, OffsetDateTime.now(), Map.of());
    }

    public static ApiResponse<Void> failure(int status, String code, String message) {
        return failure(ApiError.of(status, code, message));
    }

    private static Map<String, Object> unmodifiableCopy(Map<String, Object> source) {
        return Map.copyOf(new LinkedHashMap<>(source));
    }
}
