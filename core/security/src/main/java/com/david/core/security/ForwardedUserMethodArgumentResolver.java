package com.david.core.security;

import com.david.core.forward.ForwardedAuthenticationToken;
import com.david.core.forward.ForwardedUser;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/** 允许 {@link ForwardedUser} 被注入到 MVC 控制器中。 */
public class ForwardedUserMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean annotated = parameter.hasParameterAnnotation(CurrentForwardedUser.class);
        boolean typeMatches = ForwardedUser.class.isAssignableFrom(parameter.getParameterType());
        return annotated && typeMatches;
    }

    @Override
    public Object resolveArgument(
            @NonNull MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            @NonNull NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof ForwardedAuthenticationToken token) {
            return token.getPrincipal();
        }
        return null;
    }
}
