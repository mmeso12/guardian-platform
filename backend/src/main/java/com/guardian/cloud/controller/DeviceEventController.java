package com.guardian.cloud.controller;

import com.guardian.cloud.dto.device.DeviceEventRequest;
import com.guardian.cloud.dto.device.DeviceEventResponse;
import com.guardian.cloud.security.AuthenticatedDevice;
import com.guardian.cloud.service.DeviceEventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/device")
public class DeviceEventController {

    private final DeviceEventService deviceEventService;

    public DeviceEventController(
            DeviceEventService deviceEventService
    ) {
        this.deviceEventService = deviceEventService;
    }

    @PostMapping("/events")
    public ResponseEntity<DeviceEventResponse> receiveEvent(
            @Valid @RequestBody DeviceEventRequest request,
            Authentication authentication
    ) {
        AuthenticatedDevice authenticatedDevice =
                (AuthenticatedDevice) authentication.getPrincipal();

        DeviceEventResponse response =
                deviceEventService.processEvent(
                        authenticatedDevice.deviceUid(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(response);
    }
}