package com.guardian.cloud.controller;

import com.guardian.cloud.dto.device.DeviceTelemetryRequest;
import com.guardian.cloud.dto.device.DeviceTelemetryResponse;
import com.guardian.cloud.security.AuthenticatedDevice;
import com.guardian.cloud.service.DeviceTelemetryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.guardian.cloud.exception.DeviceIdentityMismatchException;
import com.guardian.cloud.security.AuthenticatedDevice;
import org.springframework.security.core.Authentication;

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
            @Valid @RequestBody DeviceTelemetryRequest request,
            Authentication authentication
    ) {
        AuthenticatedDevice authenticatedDevice =
                (AuthenticatedDevice) authentication.getPrincipal();

        if (!authenticatedDevice.deviceUid().equals(request.deviceUid())) {
            throw new DeviceIdentityMismatchException(
                    authenticatedDevice.deviceUid(),
                    request.deviceUid()
            );
        }

        DeviceTelemetryResponse response =
                deviceTelemetryService.processTelemetry(request);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(response);
    }
}