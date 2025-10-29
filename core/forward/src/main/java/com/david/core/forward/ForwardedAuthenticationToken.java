package com.david.core.forward;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Spring Security {@link org.springframework.security.core.Authentication} 实现，表示从网关传播的身份。 */
public class ForwardedAuthenticationToken extends AbstractAuthenticationToken {

    @Serial private static final long serialVersionUID = 1L;

    private final ForwardedUser principal;
    private final String credentials;

    public ForwardedAuthenticationToken(
            ForwardedUser principal, Collection<? extends GrantedAuthority> authorities) {
        this(principal, authorities, "N/A");
    }

    public ForwardedAuthenticationToken(
            ForwardedUser principal,
            Collection<? extends GrantedAuthority> authorities,
            String credentials) {
        super(List.copyOf(authorities));
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    @Override
    public ForwardedUser getPrincipal() {
        return principal;
    }

    @Override
    public String getCredentials() {
        return credentials;
    }

    @Override
    public String getName() {
        return Optional.ofNullable(principal)
                .map(ForwardedUser::username)
                .orElseGet(super::getName);
    }
}
