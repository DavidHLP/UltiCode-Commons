package com.david.core.forward;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/** 用于从请求头中读取转发用户信息的工具类。 */
public final class ForwardedUserParser {

    private ForwardedUserParser() {}

    public static Optional<ForwardedUser> from(HttpServletRequest request) {
        return from(request::getHeader);
    }

    public static Optional<ForwardedUser> from(HttpHeaders headers) {
        return from(headers::getFirst);
    }

    private static Optional<ForwardedUser> from(Function<String, String> headerProvider) {
        String idHeader = headerProvider.apply(ForwardedUserHeaders.USER_ID);
        String username = headerProvider.apply(ForwardedUserHeaders.USER_NAME);
        String rolesHeader = headerProvider.apply(ForwardedUserHeaders.USER_ROLES);

        if (!StringUtils.hasText(idHeader) || !StringUtils.hasText(username)) {
            return Optional.empty();
        }

        return parseUserId(idHeader)
                .map(
                        userId -> {
                            List<String> roles = parseRoles(rolesHeader);
                            return ForwardedUser.of(userId, username, roles);
                        });
    }

    private static Optional<Long> parseUserId(String idHeader) {
        try {
            return Optional.of(Long.valueOf(idHeader));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private static List<String> parseRoles(@Nullable String rolesHeader) {
        if (!StringUtils.hasText(rolesHeader)) {
            return List.of();
        }
        return Arrays.stream(rolesHeader.split(ForwardedUserHeaders.ROLE_DELIMITER))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }
}
