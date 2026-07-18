package com.guardian.cloud.dto.device;

import com.guardian.cloud.entity.EventType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record DeviceEventRequest(

        @NotNull
        @PositiveOrZero
        Long sequenceNumber,

        @NotNull
        EventType eventType,

        @Min(0)
        @Max(100)
        Integer batteryLevel,

        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        Double latitude,

        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        Double longitude,

        @NotNull
        Instant recordedAt,

        @Size(max = 2000)
        String metadata
) {
}