package com.guardian.cloud.dto.escalation;

import com.guardian.cloud.entity.EmergencyEscalationStatus;

import java.time.Instant;
import java.util.List;

public record EmergencyEscalationResponse(
        Long id,
        Long alertId,
        Long deviceId,
        String deviceUid,
        String deviceName,
        EmergencyEscalationStatus status,
        Integer currentPriority,
        Integer currentAttemptNumber,
        Instant nextActionAt,
        Instant startedAt,
        Instant acknowledgedAt,
        Instant resolvedAt,
        String acknowledgementNote,
        String resolutionNote,
        List<EmergencyContactAttemptResponse> attempts,
        Instant createdAt,
        Instant updatedAt
) {
}