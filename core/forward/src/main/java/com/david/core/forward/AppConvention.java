package com.david.core.forward;

import lombok.Getter;

import java.time.Duration;
import java.util.List;

@Getter
public abstract class AppConvention {

    protected static final List<String> DEFAULT_WHITE_LIST_PATHS =
            List.of(
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/auth/refresh",
                    "/api/auth/forgot",
                    "/api/auth/introspect",
                    "/actuator/**");

    protected static final List<String> DEFAULT_ALLOWED_ORIGINS = List.of("http://localhost:5173");

    protected static final Duration DEFAULT_TOKEN_CACHE_TTL = Duration.ofSeconds(30);

    /** 空或空列表 -> 默认；否则原样返回 */
    protected List<String> normalizeList(List<String> value) {
        return (value == null || value.isEmpty()) ? AppConvention.DEFAULT_ALLOWED_ORIGINS : value;
    }

    /** null/<=0 -> 默认；否则原样返回 */
    protected Duration normalizeDuration(Duration value) {
        if (value == null || value.isZero() || value.isNegative()) {
            return AppConvention.DEFAULT_TOKEN_CACHE_TTL;
        }
        return value;
    }
}
