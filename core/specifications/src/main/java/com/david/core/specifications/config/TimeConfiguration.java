package com.david.core.specifications.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.ZoneId;

@Slf4j
@AutoConfiguration
@AutoConfigureOrder
public class TimeConfiguration {

    @Bean
    public Clock clock(@Value("${app.time-zone:}") String timeZoneId) {
        ZoneId zone = ZoneId.systemDefault();
        if (StringUtils.hasText(timeZoneId)) {
            try {
                zone = ZoneId.of(timeZoneId);
            } catch (DateTimeException ex) {
                log.warn("无效的时区配置 [{}]，将使用系统默认时区 {}", timeZoneId, zone, ex);
            }
        }
        return Clock.system(zone);
    }
}
