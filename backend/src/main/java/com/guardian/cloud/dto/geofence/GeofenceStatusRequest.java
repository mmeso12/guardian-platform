package com.guardian.cloud.dto.geofence;

import jakarta.validation.constraints.NotNull;

public record GeofenceStatusRequest(

        @NotNull
        Boolean enabled
) {
}