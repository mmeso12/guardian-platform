package com.guardian.cloud.dto.device;

import com.guardian.cloud.entity.DeviceAccessRole;
import com.guardian.cloud.entity.DeviceStatus;
import com.guardian.cloud.entity.MotionState;

import java.time.Instant;

public record GuardianDeviceResponse(
        Long id,
        String deviceUid,
        String displayName,
        DeviceStatus status,
        Integer batteryLevel,
        MotionState motionState,
        String firmwareVersion,
        Instant lastSeenAt,
        boolean paired,
        Instant pairedAt,
        DeviceAccessRole accessRole,
        boolean canViewLocation,
        boolean canManageAlerts,
        boolean canManageDevice
) {
}