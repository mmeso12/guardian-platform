package com.guardian.cloud.controller;

import com.guardian.cloud.dto.device.DeviceTelemetryRequest;
import com.guardian.cloud.dto.device.DeviceTelemetryResponse;
import com.guardian.cloud.service.DeviceTelemetryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/device")
public class DeviceTelemetryController {

    private final DeviceTelemetryService deviceTelemetryService;

    public DeviceTelemetryController(
            DeviceTelemetryService deviceTelemetryService
    ) {
        this.deviceTelemetryService = deviceTelemetryService;
    }

    @PostMapping("/telemetry")
    public ResponseEntity<DeviceTelemetryResponse> receiveTelemetry(
            @Valid @RequestBody DeviceTelemetryRequest request
    ) {
        DeviceTelemetryResponse response =
                deviceTelemetryService.processTelemetry(request);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(response);
    }
}