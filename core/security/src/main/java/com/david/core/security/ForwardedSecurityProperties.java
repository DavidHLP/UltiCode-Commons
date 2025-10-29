package com.david.core.security;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/** 下游服务统一安全配置的外部化参数。 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.forwarded")
public class ForwardedSecurityProperties {

    /** 是否启用默认的转发安全配置。 */
    private boolean enabled = true;

    /** 是否启用 CSRF 防护，面向纯 REST 服务默认关闭。 */
    private boolean csrfEnabled = false;

    /** 是否允许匿名用户访问，默认开启以便授权规则自行控制。 */
    private boolean anonymousEnabled = true;

    /** 是否启用 InheritableThreadLocal 策略，以兼容异步/线程池场景。 */
    private boolean inheritableSecurityContext = true;

    /** 放行路径，支持 Ant 风格匹配。 */
    private List<String> permitAll = defaultPermitAll();

    /** 认证失败时返回的提示信息。 */
    private String unauthorizedMessage = "未认证的请求";

    /** 权限不足时返回的提示信息。 */
    private String accessDeniedMessage = "权限不足，拒绝访问";

    /** 是否为所有 OPTIONS 预检请求放行。 */
    private boolean allowPreflight = true;

    private MethodSecurity methodSecurity = new MethodSecurity();

    private static List<String> defaultPermitAll() {
        List<String> defaults = new ArrayList<>();
        defaults.add("/actuator/health");
        defaults.add("/actuator/health/**");
        defaults.add("/actuator/info");
        return defaults;
    }

    public void setPermitAll(List<String> permitAll) {
        this.permitAll = permitAll == null ? defaultPermitAll() : new ArrayList<>(permitAll);
    }

    @Getter
    @Setter
    public static class MethodSecurity {

        /** 是否启用基于注解的方法级鉴权。 */
        private boolean enabled = true;
    }
}
