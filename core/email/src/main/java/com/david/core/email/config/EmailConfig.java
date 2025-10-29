package com.david.core.email.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

/** 为 JavaMailSender 提供开箱即用的自动配置。 读取 spring.mail.* 与 spring.mail.properties.*。 */
@Configuration
@ConditionalOnClass(JavaMailSenderImpl.class)
@ConditionalOnMissingBean(JavaMailSender.class)
@EnableConfigurationProperties(MailProperties.class)
public class EmailConfig {

    @Bean
    public JavaMailSender javaMailSender(MailProperties props) {
        JavaMailSenderImpl sender = getJavaMailSender(props);

        // 合并底层 JavaMail 属性（如：smtp.auth、starttls、超时等）
        Properties javaMailProps = sender.getJavaMailProperties();
        if (props.getProperties() != null) {
            javaMailProps.putAll(props.getProperties());
        }

        // 常见默认值（若未显式配置）
        javaMailProps.putIfAbsent("mail.smtp.auth", "true");
        // 对应 spring.mail.properties.mail.smtp.starttls.enable=true
        javaMailProps.putIfAbsent("mail.smtp.starttls.enable", "true");

        return sender;
    }

    private JavaMailSenderImpl getJavaMailSender(MailProperties props) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(props.getHost());
        if (props.getPort() != null) {
            sender.setPort(props.getPort());
        }
        sender.setUsername(props.getUsername());
        sender.setPassword(props.getPassword());
        sender.setProtocol(props.getProtocol());
        sender.setDefaultEncoding(
                props.getDefaultEncoding() != null
                        ? props.getDefaultEncoding().name()
                        : StandardCharsets.UTF_8.name());
        return sender;
    }
}
