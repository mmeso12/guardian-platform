package com.guardian.cloud.dto.push;

import com.guardian.cloud.entity.MobilePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterMobileDeviceRequest(

        @NotBlank
        @Size(max = 512)
        String pushToken,

        @NotNull
        MobilePlatform platform,

        @Size(max = 150)
        String deviceName,

        @Size(max = 50)
        String appVersion
) {
}