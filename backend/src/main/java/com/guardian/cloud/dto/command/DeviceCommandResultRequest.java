package com.guardian.cloud.dto.command;

import java.util.Map;

public record DeviceCommandResultRequest(
        Map<String, Object> result
) {
}