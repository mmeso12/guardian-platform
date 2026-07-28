package com.guardian.cloud.dto.push;

import com.guardian.cloud.entity.MobilePlatform;

import java.time.Instant;

public record GuardianMobileDeviceResponse(
        Long id,
        MobilePlatform platform,
        String deviceName,
        String appVersion,
        boolean enabled,
        Instant lastSeenAt,
        Instant createdAt,
        Instant updatedAt
) {
}