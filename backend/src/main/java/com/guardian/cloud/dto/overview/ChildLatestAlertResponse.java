package com.guardian.cloud.dto.overview;

import com.guardian.cloud.entity.AlertSeverity;
import com.guardian.cloud.entity.AlertStatus;
import com.guardian.cloud.entity.EventType;

import java.time.Instant;

public record ChildLatestAlertResponse(
        Long alertId,
        EventType eventType,
        AlertSeverity severity,
        AlertStatus status,
        String title,
        String message,
        Double latitude,
        Double longitude,
        Instant openedAt,
        Instant createdAt
) {
}