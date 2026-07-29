package com.guardian.cloud.dto.auth;

import java.time.Instant;
import java.util.UUID;

public record GuardianSessionResponse(

        UUID sessionId,

        String deviceName,
        String platform,
        String ipAddress,
        String userAgent,

        boolean current,
        boolean active,

        Instant lastUsedAt,
        Instant expiresAt,
        Instant revokedAt,
        Instant createdAt
) {
}