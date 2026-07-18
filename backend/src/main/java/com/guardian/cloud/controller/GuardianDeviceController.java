package com.guardian.cloud.controller;

import com.guardian.cloud.dto.device.GuardianDeviceResponse;
import com.guardian.cloud.dto.device.PairDeviceRequest;
import com.guardian.cloud.security.AuthenticatedGuardian;
import com.guardian.cloud.service.DevicePairingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
public class GuardianDeviceController {

    private final DevicePairingService devicePairingService;

    public GuardianDeviceController(
            DevicePairingService devicePairingService
    ) {
        this.devicePairingService = devicePairingService;
    }

    @PostMapping("/pair")
    public ResponseEntity<GuardianDeviceResponse> pairDevice(
            @Valid @RequestBody PairDeviceRequest request,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                getGuardian(authentication);

        GuardianDeviceResponse response =
                devicePairingService.pairDevice(
                        guardian.id(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<GuardianDeviceResponse>> getDevices(
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                getGuardian(authentication);

        return ResponseEntity.ok(
                devicePairingService.getDevices(guardian.id())
        );
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<GuardianDeviceResponse> getDevice(
            @PathVariable Long deviceId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                getGuardian(authentication);

        return ResponseEntity.ok(
                devicePairingService.getDevice(
                        guardian.id(),
                        deviceId
                )
        );
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> unpairDevice(
            @PathVariable Long deviceId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                getGuardian(authentication);

        devicePairingService.unpairDevice(
                guardian.id(),
                deviceId
        );

        return ResponseEntity.noContent().build();
    }

    private AuthenticatedGuardian getGuardian(
            Authentication authentication
    ) {
        return (AuthenticatedGuardian)
                authentication.getPrincipal();
    }
}