package com.guardian.cloud.dto.notification;

import java.time.Instant;

public record GuardianNotificationPreferenceResponse(
        boolean inAppEnabled,
        boolean pushEnabled,
        boolean informationalEnabled,
        boolean warningEnabled,
        boolean sosEnabled,
        boolean tamperEnabled,
        boolean lowBatteryEnabled,
        boolean deviceOnlineEnabled,
        boolean deviceOfflineEnabled,
        boolean geofenceEntryEnabled,
        boolean geofenceExitEnabled,
        Instant createdAt,
        Instant updatedAt
) {
}