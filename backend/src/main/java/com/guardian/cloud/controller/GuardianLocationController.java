package com.guardian.cloud.controller;

import com.guardian.cloud.dto.location.DeviceLocationResponse;
import com.guardian.cloud.dto.location.LocationResponse;
import com.guardian.cloud.security.AuthenticatedGuardian;
import com.guardian.cloud.service.GuardianLocationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/devices/{deviceId}/locations")
public class GuardianLocationController {

    private final GuardianLocationService guardianLocationService;

    public GuardianLocationController(
            GuardianLocationService guardianLocationService
    ) {
        this.guardianLocationService = guardianLocationService;
    }

    @GetMapping("/latest")
    public ResponseEntity<DeviceLocationResponse> getLatestLocation(
            @PathVariable Long deviceId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                getGuardian(authentication);

        return ResponseEntity.ok(
                guardianLocationService.getLatestLocation(
                        guardian.id(),
                        deviceId
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<LocationResponse>> getRecentHistory(
            @PathVariable Long deviceId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                getGuardian(authentication);

        return ResponseEntity.ok(
                guardianLocationService.getRecentLocationHistory(
                        guardian.id(),
                        deviceId
                )
        );
    }

    @GetMapping("/history")
    public ResponseEntity<List<LocationResponse>> getHistory(
            @PathVariable Long deviceId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant from,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant to,

            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                getGuardian(authentication);

        return ResponseEntity.ok(
                guardianLocationService.getLocationHistory(
                        guardian.id(),
                        deviceId,
                        from,
                        to
                )
        );
    }

    private AuthenticatedGuardian getGuardian(
            Authentication authentication
    ) {
        return (AuthenticatedGuardian)
                authentication.getPrincipal();
    }
}