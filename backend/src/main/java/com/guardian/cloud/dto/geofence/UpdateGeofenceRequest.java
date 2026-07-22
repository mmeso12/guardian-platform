package com.guardian.cloud.dto.geofence;

import jakarta.validation.constraints.*;

public record UpdateGeofenceRequest(

        @NotBlank
        @Size(max = 120)
        String name,

        @Size(max = 500)
        String description,

        @NotNull
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        Double centerLatitude,

        @NotNull
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        Double centerLongitude,

        @NotNull
        @DecimalMin("10.0")
        @DecimalMax("100000.0")
        Double radiusMeters
) {
}