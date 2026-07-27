package com.guardian.cloud.dto.escalation;

import com.guardian.cloud.entity.EmergencyContactAttemptStatus;
import com.guardian.cloud.entity.PreferredContactMethod;

import java.time.Instant;

public record EmergencyContactAttemptResponse(
        Long id,
        Long emergencyContactId,
        Integer attemptNumber,
        PreferredContactMethod contactMethod,
        EmergencyContactAttemptStatus status,
        String contactName,
        String phoneNumber,
        String email,
        Instant attemptedAt,
        Instant acknowledgedAt,
        Instant completedAt,
        String failureReason
) {
}