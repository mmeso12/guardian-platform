package com.guardian.cloud.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank
        @Size(max = 100)
        String firstName,

        @NotBlank
        @Size(max = 100)
        String lastName,

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @Size(max = 30)
        @Pattern(
                regexp = "^$|^[+]?[0-9 ()-]{7,30}$",
                message = "Phone number format is invalid"
        )
        String phoneNumber,

        @NotBlank
        @Size(min = 8, max = 100)
        String password
) {
}