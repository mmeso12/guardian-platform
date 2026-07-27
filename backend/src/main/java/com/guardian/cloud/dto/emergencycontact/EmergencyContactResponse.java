package com.guardian.cloud.dto.emergencycontact;

import com.guardian.cloud.entity.EmergencyContactRelationship;
import com.guardian.cloud.entity.PreferredContactMethod;

import java.time.Instant;

public record EmergencyContactResponse(
        Long id,
        String fullName,
        String phoneNumber,
        String email,
        EmergencyContactRelationship relationship,
        Integer priority,
        PreferredContactMethod preferredContactMethod,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}