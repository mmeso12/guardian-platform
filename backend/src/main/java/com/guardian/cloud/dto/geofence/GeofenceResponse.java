package com.guardian.cloud.dto.geofence;

import java.time.Instant;

public record GeofenceResponse(
        Long id,
        Long deviceId,
        String deviceUid,
        String deviceDisplayName,
        String name,
        String description,
        Double centerLatitude,
        Double centerLongitude,
        Double radiusMeters,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}