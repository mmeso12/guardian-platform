package com.guardian.cloud.dto.device;

import com.guardian.cloud.entity.MotionState;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.Instant;

public record DeviceTelemetryRequest(

        @NotBlank
        @Size(max = 100)
        String deviceUid,

        @NotNull
        @PositiveOrZero
        Long sequenceNumber,

        @NotNull
        Instant recordedAt,

        @NotNull
        @Min(0)
        @Max(100)
        Integer batteryLevel,

        @NotNull
        MotionState motionState,

        @Size(max = 50)
        String firmwareVersion,

        @NotNull
        @Valid
        LocationPayload location
) {
}