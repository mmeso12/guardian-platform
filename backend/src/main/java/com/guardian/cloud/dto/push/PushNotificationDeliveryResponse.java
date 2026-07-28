package com.guardian.cloud.dto.push;

import com.guardian.cloud.entity.MobilePlatform;
import com.guardian.cloud.entity.PushDeliveryStatus;

import java.time.Instant;

public record PushNotificationDeliveryResponse(
        Long id,
        Long guardianNotificationId,
        Long mobileDeviceId,
        String mobileDeviceName,
        MobilePlatform platform,
        PushDeliveryStatus status,
        String provider,
        Integer attemptCount,
        String providerMessageId,
        String failureReason,
        Instant nextRetryAt,
        Instant sentAt,
        Instant createdAt,
        Instant updatedAt
) {
}