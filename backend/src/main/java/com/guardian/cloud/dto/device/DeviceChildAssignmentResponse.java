package com.guardian.cloud.dto.device;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DeviceChildAssignmentResponse(
        UUID assignmentId,

        Long deviceId,
        String deviceUid,
        String deviceDisplayName,

        UUID childId,
        String childFirstName,
        String childLastName,
        LocalDate childDateOfBirth,

        Instant assignedAt,
        Instant unassignedAt,
        boolean active
) {
}