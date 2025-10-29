package com.david.core.security;

import com.david.core.forward.ForwardedUserContextFilter;
import com.david.core.forward.ForwardedSecurityAutoConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/** 提供下游服务默认的安全配置，实现与网关转发身份的无缝衔接。 */
@AutoConfiguration(after = ForwardedSecurityAutoConfiguration.class)
@ConditionalOnClass({SecurityFilterChain.class, HttpSecurity.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(
        prefix = "app.security.forwarded",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(ForwardedSecurityProperties.class)
public class DownstreamSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ForwardedUserMethodArgumentResolver forwardedUserMethodArgumentResolver() {
        return new ForwardedUserMethodArgumentResolver();
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public WebMvcConfigurer forwardedUserWebMvcConfigurer(
            ForwardedUserMethodArgumentResolver resolver) {
        return new WebMvcConfigurer() {
            @Override
            public void addArgumentResolvers(
                    @NonNull List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(resolver);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    ForwardedSecurityContextHolderInitializer forwardedSecurityContextHolderInitializer(
            ForwardedSecurityProperties properties) {
        return new ForwardedSecurityContextHolderInitializer(properties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "forwardedErrorResponseWriter")
    ForwardedErrorResponseWriter forwardedErrorResponseWriter(
            org.springframework.beans.factory.ObjectProvider<ObjectMapper> objectMapperProvider) {
        return new ForwardedErrorResponseWriter(objectMapperProvider.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean(name = "forwardedAuthenticationEntryPoint")
    ForwardedAuthenticationEntryPoint forwardedAuthenticationEntryPoint(
            ForwardedErrorResponseWriter writer, ForwardedSecurityProperties properties) {
        return new ForwardedAuthenticationEntryPoint(writer, properties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "forwardedAccessDeniedHandler")
    ForwardedAccessDeniedHandler forwardedAccessDeniedHandler(
            ForwardedErrorResponseWriter writer, ForwardedSecurityProperties properties) {
        return new ForwardedAccessDeniedHandler(writer, properties);
    }

    @Bean
    @ConditionalOnBean(ForwardedUserContextFilter.class)
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    SecurityFilterChain forwardedSecurityFilterChain(
            HttpSecurity http,
            ForwardedSecurityProperties properties,
            ForwardedUserContextFilter forwardedUserContextFilter,
            ForwardedAccessDeniedHandler accessDeniedHandler,
            ForwardedAuthenticationEntryPoint authenticationEntryPoint)
            throws Exception {
        configureHttpSecurity(http, properties);
        http.addFilterBefore(
                forwardedUserContextFilter, UsernamePasswordAuthenticationFilter.class);
        http.exceptionHandling(
                exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint(authenticationEntryPoint)
                                .accessDeniedHandler(accessDeniedHandler));
        return http.build();
    }

    private void configureHttpSecurity(HttpSecurity http, ForwardedSecurityProperties properties)
            throws Exception {
        if (!properties.isCsrfEnabled()) {
            http.csrf(AbstractHttpConfigurer::disable);
        }
        http.sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);
        if (!properties.isAnonymousEnabled()) {
            http.anonymous(AbstractHttpConfigurer::disable);
        }

        http.authorizeHttpRequests(
                authorize -> {
                    if (properties.isAllowPreflight()) {
                        authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    }
                    List<String> permitAll = properties.getPermitAll();
                    if (!CollectionUtils.isEmpty(permitAll)) {
                        permitAll.stream()
                                .map(AntPathRequestMatcher::new)
                                .forEach(matcher -> authorize.requestMatchers(matcher).permitAll());
                    }
                    // 根据指令，默认放行所有请求，这是微服务内部的security
                    authorize.anyRequest().permitAll();
                });
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(SecurityContextHolderStrategy.class)
    @ConditionalOnProperty(
            prefix = "app.security.forwarded.method-security",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @EnableMethodSecurity
    static class ForwardedMethodSecurityConfiguration {}
}
