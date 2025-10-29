package com.david.core.password.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** 自动装配 PasswordEncoder 及简单的密码服务。 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(PasswordEncoder.class)
@EnableConfigurationProperties(PasswordProperties.class)
@ConditionalOnProperty(
        prefix = "app.security.password",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class PasswordAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder(PasswordProperties properties) {
        return switch (properties.getAlgorithm()) {
            case BCRYPT -> createBcryptEncoder(properties);
            case PBKDF2 -> createPbkdf2Encoder(properties);
            case DELEGATING -> createDelegatingEncoder(properties);
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordService passwordService(PasswordEncoder passwordEncoder) {
        return new PasswordService(passwordEncoder);
    }

    private PasswordEncoder createBcryptEncoder(PasswordProperties properties) {
        int strength = clamp(properties.getBcryptStrength());
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(strength);
    }

    private PasswordEncoder createPbkdf2Encoder(PasswordProperties properties) {
        Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm algorithm =
                resolveSecretKeyFactoryAlgorithm(properties.getPbkdf2Algorithm());

        // 使用包含 algorithm 的构造器，随后再设置 iterations / base64
        Pbkdf2PasswordEncoder encoder =
                new Pbkdf2PasswordEncoder(
                        properties.getSecret(),
                        Math.max(8, properties.getPbkdf2SaltLength()), // 下限保护
                        properties.getPbkdf2HashWidth(),
                        algorithm);
        encoder.setEncodeHashAsBase64(properties.isPbkdf2EncodeHashAsBase64());
        return encoder;
    }

    private PasswordEncoder createDelegatingEncoder(PasswordProperties properties) {
        Map<String, PasswordEncoder> delegates = new HashMap<>();
        delegates.put("bcrypt", createBcryptEncoder(properties));
        delegates.put("pbkdf2", createPbkdf2Encoder(properties));

        String idForEncode = properties.getDelegatingId();
        PasswordEncoder encoderForId = delegates.get(idForEncode);
        if (encoderForId == null) {
            log.warn("未知的 DelegatingPasswordEncoder 默认算法 id '{}'，回退至 'bcrypt'", idForEncode);
            idForEncode = "bcrypt";
            encoderForId = delegates.get(idForEncode);
        }
        DelegatingPasswordEncoder delegating =
                new DelegatingPasswordEncoder(idForEncode, delegates);
        // 当待校验的密文没有前缀时，使用该编码器进行匹配
        delegating.setDefaultPasswordEncoderForMatches(encoderForId);
        return delegating;
    }

    private int clamp(int value) {
        return Math.max(4, Math.min(31, value));
    }

    private Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm resolveSecretKeyFactoryAlgorithm(
            String algorithmName) {
        if (Objects.isNull(algorithmName) || algorithmName.isBlank()) {
            return Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256;
        }
        try {
            return Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.valueOf(algorithmName);
        } catch (IllegalArgumentException ex) {
            log.warn("不支持的 PBKDF2 算法 '{}'，使用默认 PBKDF2WithHmacSHA256", algorithmName);
            return Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256;
        }
    }
}
