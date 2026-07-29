package com.guardian.cloud.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank
        @Email
        String email,

        @NotBlank
        String password,

        @Size(max = 150)
        String deviceName,

        @Size(max = 50)
        String platform
) {
}