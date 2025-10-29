package com.david.core.forward;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 使用网关已认证的用户身份填充 {@link org.springframework.security.core.context.SecurityContext}。
 * 该过滤器用于在微服务间调用时传递用户认证信息。
 */
@Slf4j
@Order(ForwardedUserContextFilter.ORDER)
public class ForwardedUserContextFilter extends OncePerRequestFilter {

    /** 过滤器执行顺序，设置为最高优先级+50。 */
    public static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 50;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        Optional<Authentication> currentAuthentication =
                Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
        Optional<ForwardedUser> forwardedUser = ForwardedUserParser.from(request);

        forwardedUser.ifPresentOrElse(
                user -> {
                    List<SimpleGrantedAuthority> authorities = buildGrantedAuthorities(user);
                    if (shouldRefreshForwardedAuthentication(
                            currentAuthentication.orElse(null), user, authorities)) {
                        log.debug("刷新转发用户认证信息，用户名: {}", user.username());
                        SecurityContextHolder.clearContext();

                        ForwardedAuthenticationToken authentication =
                                new ForwardedAuthenticationToken(user, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("成功设置转发用户认证信息到安全上下文，用户名: {}", user.username());
                    } else {
                        log.debug("检测到相同的转发用户上下文，跳过刷新，用户名: {}", user.username());
                    }
                },
                () -> {
                    if (currentAuthentication.isPresent() && currentAuthentication.get() instanceof ForwardedAuthenticationToken) {
                        log.warn("未检测到转发的用户信息，清理遗留的安全上下文");
                        SecurityContextHolder.clearContext();
                    } else {
                        log.warn("未检测到转发的用户信息");
                    }
                });

        try {
            filterChain.doFilter(request, response);
        } finally {
            Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                    .filter(auth -> auth instanceof ForwardedAuthenticationToken)
                    .ifPresent(auth -> {
                        SecurityContextHolder.clearContext();
                        log.trace("清理转发用户认证上下文，防止线程复用导致权限异常");
                    });
        }
    }

    private boolean shouldRefreshForwardedAuthentication(
            Authentication currentAuthentication,
            ForwardedUser forwardedUser,
            Collection<? extends GrantedAuthority> newAuthorities) {
        if (currentAuthentication == null) {
            return true;
        }
        if (currentAuthentication instanceof AnonymousAuthenticationToken) {
            return true;
        }
        if (!(currentAuthentication instanceof ForwardedAuthenticationToken existingToken)) {
            return true;
        }
        ForwardedUser existingUser = existingToken.getPrincipal();
        if (existingUser == null) {
            return true;
        }
        if (!Objects.equals(existingUser.id(), forwardedUser.id())) {
            return true;
        }
        if (!Objects.equals(existingUser.username(), forwardedUser.username())) {
            return true;
        }
        return !authoritiesEquals(existingToken.getAuthorities(), newAuthorities);
    }

    private List<SimpleGrantedAuthority> buildGrantedAuthorities(ForwardedUser user) {
        return user.roles().stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    private boolean authoritiesEquals(
            Collection<? extends GrantedAuthority> existingAuthorities,
            Collection<? extends GrantedAuthority> newAuthorities) {
        Set<String> existing =
                existingAuthorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toCollection(HashSet::new));
        Set<String> incoming =
                newAuthorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toCollection(HashSet::new));
        return existing.equals(incoming);
    }
}
