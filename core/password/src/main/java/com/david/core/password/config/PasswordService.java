package com.david.core.password.config;

import org.springframework.security.crypto.password.PasswordEncoder;

/** 简单的密码服务封装，便于在领域层进行密码编码与校验。 */
public record PasswordService(PasswordEncoder passwordEncoder) {

    public String encode(CharSequence rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public boolean upgradeEncoding(String encodedPassword) {
        return passwordEncoder.upgradeEncoding(encodedPassword);
    }
}
