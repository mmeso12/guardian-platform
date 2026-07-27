package com.guardian.cloud.dto.emergencycontact;

import com.guardian.cloud.entity.EmergencyContactRelationship;
import com.guardian.cloud.entity.PreferredContactMethod;
import jakarta.validation.constraints.*;

public record CreateEmergencyContactRequest(

        @NotBlank
        @Size(max = 150)
        String fullName,

        @Size(max = 30)
        String phoneNumber,

        @Email
        @Size(max = 255)
        String email,

        @NotNull
        EmergencyContactRelationship relationship,

        @NotNull
        @Min(1)
        @Max(100)
        Integer priority,

        @NotNull
        PreferredContactMethod preferredContactMethod
) {
}