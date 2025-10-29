package com.david.core.forward;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** 简单的值对象，用于描述已经通过上游网关认证的用户身份。 */
public record ForwardedUser(Long id, String username, List<String> roles) implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    public ForwardedUser {
        roles = Optional.ofNullable(roles).map(List::copyOf).orElse(List.of());
    }

    /** 便利工厂方法，避免空集合。 */
    public static ForwardedUser of(Long id, String username, List<String> roles) {
        return new ForwardedUser(
                id, username, Optional.ofNullable(roles).orElse(Collections.emptyList()));
    }

    public boolean hasRole(String role) {
        return Optional.ofNullable(roles).filter(r -> !r.isEmpty()).stream()
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .anyMatch(role::equals);
    }
}
