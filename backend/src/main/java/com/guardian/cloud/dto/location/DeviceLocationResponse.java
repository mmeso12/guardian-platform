package com.guardian.cloud.dto.location;

import com.guardian.cloud.entity.DeviceStatus;

import java.time.Instant;

public record DeviceLocationResponse(
        Long deviceId,
        String deviceUid,
        String displayName,
        DeviceStatus status,
        Instant lastSeenAt,
        LocationResponse location
) {
}