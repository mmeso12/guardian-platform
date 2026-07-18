package com.guardian.cloud.dto.device;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PairDeviceRequest(

        @NotBlank
        @Size(max = 100)
        String deviceUid,

        @NotBlank
        @Size(min = 6, max = 100)
        String pairingCode,

        @Size(max = 100)
        String displayName
) {
}