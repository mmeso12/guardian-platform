package com.guardian.cloud.dto.command;

import com.guardian.cloud.entity.DeviceCommandStatus;
import com.guardian.cloud.entity.DeviceCommandType;

import java.time.Instant;
import java.util.Map;

public record DeviceCommandResponse(

        Long id,

        Long deviceId,
        String deviceUid,
        String deviceDisplayName,

        Long createdByGuardianId,

        DeviceCommandType commandType,
        DeviceCommandStatus status,

        Map<String, Object> payload,
        Map<String, Object> result,

        String failureReason,

        Instant deliveredAt,
        Instant receivedAt,
        Instant executionStartedAt,
        Instant completedAt,
        Instant failedAt,
        Instant cancelledAt,
        Instant expiresAt,

        Instant createdAt,
        Instant updatedAt
) {
}