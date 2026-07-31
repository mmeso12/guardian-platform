package com.guardian.cloud.dto.device;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignDeviceToChildRequest(

        @NotNull(message = "Child ID is required")
        UUID childId
) {
}