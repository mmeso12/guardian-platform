package com.guardian.cloud.dto.device;

import com.guardian.cloud.entity.AlertSeverity;
import com.guardian.cloud.entity.EventType;

import java.time.Instant;

public record DeviceEventResponse(
        Long eventId,
        String deviceUid,
        Long sequenceNumber,
        EventType eventType,
        AlertSeverity severity,
        String status,
        Instant receivedAt
) {
}