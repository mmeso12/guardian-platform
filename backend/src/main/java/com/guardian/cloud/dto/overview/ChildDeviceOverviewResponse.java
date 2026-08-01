package com.guardian.cloud.dto.overview;

import com.guardian.cloud.entity.DeviceStatus;
import com.guardian.cloud.entity.MotionState;

import java.time.Instant;

public record ChildDeviceOverviewResponse(
        Long deviceId,
        String deviceUid,
        String displayName,
        DeviceStatus status,
        Integer batteryLevel,
        MotionState motionState,
        String firmwareVersion,
        Instant lastSeenAt,
        Instant assignedAt
) {
}