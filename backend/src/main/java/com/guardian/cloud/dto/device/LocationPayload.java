package com.guardian.cloud.dto.device;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;

public record LocationPayload(

        @DecimalMin(value = "-90.0")
        @DecimalMax(value = "90.0")
        Double latitude,

        @DecimalMin(value = "-180.0")
        @DecimalMax(value = "180.0")
        Double longitude,

        @PositiveOrZero
        Double accuracyMeters,

        @PositiveOrZero
        Double speedMetersPerSecond,

        @DecimalMin(value = "0.0")
        @DecimalMax(value = "360.0")
        Double headingDegrees
) {
}