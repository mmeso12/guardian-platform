package com.guardian.cloud.dto.child;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ChildResponse(
        UUID childId,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String gender,
        String profileImageUrl,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}