package com.guardian.cloud.dto.notification;

public record UpdateNotificationPreferenceRequest(
        boolean inAppEnabled,
        boolean pushEnabled,
        boolean informationalEnabled,
        boolean warningEnabled,
        boolean tamperEnabled,
        boolean lowBatteryEnabled,
        boolean deviceOnlineEnabled,
        boolean deviceOfflineEnabled,
        boolean geofenceEntryEnabled,
        boolean geofenceExitEnabled
) {
}