package com.guardian.cloud.dto.location;

import com.guardian.cloud.entity.MotionState;

import java.time.Instant;

public record LocationResponse(
        Long id,
        Long sequenceNumber,
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