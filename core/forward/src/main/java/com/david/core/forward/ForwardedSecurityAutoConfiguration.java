package com.david.core.forward;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.OncePerRequestFilter;

/** 自动配置类，用于将转发用户基础设施集成到Spring MVC应用程序中。 */
@AutoConfiguration
@ConditionalOnClass(OncePerRequestFilter.class)
public class ForwardedSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ForwardedUserContextFilter forwardedUserContextFilter() {
        return new ForwardedUserContextFilter();
    }
}
