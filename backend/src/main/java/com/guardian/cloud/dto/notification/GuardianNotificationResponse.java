package com.guardian.cloud.dto.notification;

import com.guardian.cloud.entity.AlertSeverity;
import com.guardian.cloud.entity.AlertStatus;
import com.guardian.cloud.entity.EventType;

import java.time.Instant;

public record GuardianNotificationResponse(
        Long id,

        Long alertId,
        Long deviceId,
        String deviceUid,
        String deviceName,

        EventType eventType,
        AlertSeverity severity,
        AlertStatus alertStatus,

        String title,
        String message,

        Double latitude,
        Double longitude,

        boolean read,
        Instant readAt,

        Instant alertOpenedAt,
        Instant createdAt,
        Instant updatedAt
) {
}