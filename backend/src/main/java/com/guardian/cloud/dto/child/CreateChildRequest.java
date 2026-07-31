package com.guardian.cloud.dto.child;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateChildRequest(

        @NotBlank(message = "First name is required")
        @Size(
                max = 100,
                message = "First name must not exceed 100 characters"
        )
        String firstName,

        @Size(
                max = 100,
                message = "Last name must not exceed 100 characters"
        )
        String lastName,

        @Past(
                message = "Date of birth must be in the past"
        )
        @NotNull
        LocalDate dateOfBirth,

        @Size(
                max = 30,
                message = "Gender must not exceed 30 characters"
        )
        String gender,

        @Size(
                max = 500,
                message = "Profile image URL must not exceed 500 characters"
        )
        String profileImageUrl
) {
}