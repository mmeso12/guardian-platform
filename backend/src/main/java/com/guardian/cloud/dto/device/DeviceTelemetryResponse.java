package com.guardian.cloud.dto.device;

import java.time.Instant;

public record DeviceTelemetryResponse(
        String deviceUid,
        Long sequenceNumber,
        String status,
        Instant receivedAt
) {
}