package com.david.core.security;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SecurityAuditRecord {
    Long actorId;
    String actorUsername;
    AuditAction action;
    String objectType;
    String objectId;
    String description;
    Map<String, Object> diff;
    String ipAddress;
    LocalDateTime timestamp;
}
