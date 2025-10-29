package com.david.core.security;

import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

/**
 * 提供日志与导出场景下的敏感数据脱敏能力，保证在各模块打印信息时不会泄露原始数据。
 */
@UtilityClass
public class SensitiveDataMasker {

    private static final String MASK = "***";

    public static String maskEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return MASK;
        }
        String prefix = email.substring(0, Math.min(atIndex, 3));
        return prefix + MASK + email.substring(atIndex);
    }

    public static String maskIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return ip;
        }
        String[] segments = ip.split("\\.");
        if (segments.length != 4) {
            return MASK;
        }
        return segments[0] + "." + segments[1] + ".***.***";
    }

    public static String maskToken(String token) {
        if (!StringUtils.hasText(token)) {
            return token;
        }
        if (token.length() <= 8) {
            return MASK;
        }
        return token.substring(0, 4) + MASK + token.substring(token.length() - 4);
    }
}
