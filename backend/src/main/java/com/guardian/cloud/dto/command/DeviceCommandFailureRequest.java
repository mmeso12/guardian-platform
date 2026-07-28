package com.guardian.cloud.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record DeviceCommandFailureRequest(

        @NotBlank
        @Size(max = 1000)
        String failureReason,

        Map<String, Object> result
) {
}