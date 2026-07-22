package com.guardian.cloud.controller;

import com.guardian.cloud.dto.geofence.*;
import com.guardian.cloud.security.AuthenticatedGuardian;
import com.guardian.cloud.service.GeofenceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class GeofenceController {

    private final GeofenceService geofenceService;

    public GeofenceController(
            GeofenceService geofenceService
    ) {
        this.geofenceService = geofenceService;
    }

    @PostMapping("/geofences")
    public ResponseEntity<GeofenceResponse> createGeofence(
            @Valid @RequestBody
            CreateGeofenceRequest request,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        GeofenceResponse response =
                geofenceService.createGeofence(
                        guardian.id(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/geofences")
    public ResponseEntity<List<GeofenceResponse>>
    getAllGeofences(
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        return ResponseEntity.ok(
                geofenceService.getAllGeofences(
                        guardian.id()
                )
        );
    }

    @GetMapping("/geofences/{geofenceId}")
    public ResponseEntity<GeofenceResponse> getGeofence(
            @PathVariable Long geofenceId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        return ResponseEntity.ok(
                geofenceService.getGeofence(
                        guardian.id(),
                        geofenceId
                )
        );
    }

    @GetMapping("/devices/{deviceId}/geofences")
    public ResponseEntity<List<GeofenceResponse>>
    getDeviceGeofences(
            @PathVariable Long deviceId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        return ResponseEntity.ok(
                geofenceService.getDeviceGeofences(
                        guardian.id(),
                        deviceId
                )
        );
    }

    @PutMapping("/geofences/{geofenceId}")
    public ResponseEntity<GeofenceResponse> updateGeofence(
            @PathVariable Long geofenceId,
            @Valid @RequestBody
            UpdateGeofenceRequest request,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        return ResponseEntity.ok(
                geofenceService.updateGeofence(
                        guardian.id(),
                        geofenceId,
                        request
                )
        );
    }

    @PatchMapping("/geofences/{geofenceId}/status")
    public ResponseEntity<GeofenceResponse>
    setGeofenceStatus(
            @PathVariable Long geofenceId,
            @Valid @RequestBody
            GeofenceStatusRequest request,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        return ResponseEntity.ok(
                geofenceService.setGeofenceStatus(
                        guardian.id(),
                        geofenceId,
                        request
                )
        );
    }

    @DeleteMapping("/geofences/{geofenceId}")
    public ResponseEntity<Void> deleteGeofence(
            @PathVariable Long geofenceId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        geofenceService.deleteGeofence(
                guardian.id(),
                geofenceId
        );

        return ResponseEntity.noContent().build();
    }

    private AuthenticatedGuardian authenticatedGuardian(
            Authentication authentication
    ) {
        return (AuthenticatedGuardian)
                authentication.getPrincipal();
    }
}