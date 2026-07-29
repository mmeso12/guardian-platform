package com.guardian.cloud.dto.auth;

import jakarta.validation.constraints.Size;

public record AuthenticatedSessionRequest(

        @Size(max = 150)
        String deviceName,

        @Size(max = 50)
        String platform
) {
}