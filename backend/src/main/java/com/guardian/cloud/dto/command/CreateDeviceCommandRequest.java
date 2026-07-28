package com.guardian.cloud.dto.command;

import com.guardian.cloud.entity.DeviceCommandType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CreateDeviceCommandRequest(

        @NotNull
        DeviceCommandType commandType,

        Map<String, Object> payload,

        @Min(30)
        @Max(86400)
        Long expiresInSeconds
) {
}