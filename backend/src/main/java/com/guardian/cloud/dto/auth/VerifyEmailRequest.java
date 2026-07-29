package com.guardian.cloud.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(

        @NotBlank
        String token
) {
}