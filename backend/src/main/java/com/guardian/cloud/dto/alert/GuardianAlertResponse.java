package com.guardian.cloud.dto.alert;

import com.guardian.cloud.entity.AlertSeverity;
import com.guardian.cloud.entity.AlertStatus;
import com.guardian.cloud.entity.EventType;

import java.time.Instant;

public record GuardianAlertResponse(
        Long id,
        Long deviceId,
        String deviceUid,
        String deviceDisplayName,
        Long deviceEventId,
        EventType eventType,
        AlertSeverity severity,
        AlertStatus status,
        String title,
        String message,
        Double latitude,
        Double longitude,
        Instant openedAt,
        AlertUserResponse acknowledgedBy,
        Instant acknowledgedAt,
        String acknowledgementNote,
        AlertUserResponse resolvedBy,
        Instant resolvedAt,
        String resolutionNote,
        Instant createdAt,
        Instant updatedAt
) {
}