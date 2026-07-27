package com.guardian.cloud.dto.emergencycontact;

import jakarta.validation.constraints.NotNull;

public record EmergencyContactStatusRequest(

        @NotNull
        Boolean enabled
) {
}