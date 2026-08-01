package com.guardian.cloud.dto.overview;

import java.time.LocalDate;
import java.util.UUID;

public record ChildOverviewProfileResponse(
        UUID childId,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String gender,
        String profileImageUrl
) {
}