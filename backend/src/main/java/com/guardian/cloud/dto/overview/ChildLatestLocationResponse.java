package com.guardian.cloud.dto.overview;

import com.guardian.cloud.entity.MotionState;

import java.time.Instant;

public record ChildLatestLocationResponse(
        Long locationId,
        Double latitude,
        Double longitude,
        Double accuracyMeters,
        Double speedMetersPerSecond,
        Double headingDegrees,
        Integer batteryLevel,
        MotionState motionState,
        Instant recordedAt,
        Instant receivedAt
) {
}