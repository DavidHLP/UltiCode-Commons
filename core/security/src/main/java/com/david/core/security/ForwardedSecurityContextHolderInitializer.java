package com.david.core.security;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.security.core.context.SecurityContextHolder;

/** 根据配置切换 {@link SecurityContextHolder} 的策略，增强异步可见性。 */
record ForwardedSecurityContextHolderInitializer(ForwardedSecurityProperties properties)
        implements SmartInitializingSingleton {

    @Override
    public void afterSingletonsInstantiated() {
        if (properties.isInheritableSecurityContext()) {
            SecurityContextHolder.setStrategyName(
                    SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        }
    }
}
