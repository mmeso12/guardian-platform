package com.guardian.cloud.controller;

import com.guardian.cloud.dto.alert.AlertActionRequest;
import com.guardian.cloud.dto.alert.GuardianAlertResponse;
import com.guardian.cloud.entity.AlertStatus;
import com.guardian.cloud.security.AuthenticatedGuardian;
import com.guardian.cloud.service.GuardianAlertService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class GuardianAlertController {

    private final GuardianAlertService guardianAlertService;

    public GuardianAlertController(
            GuardianAlertService guardianAlertService
    ) {
        this.guardianAlertService = guardianAlertService;
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<GuardianAlertResponse>>
    getAllAlerts(
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        return ResponseEntity.ok(
                guardianAlertService.getAllAlerts(
                        guardian.id()
                )
        );
    }

    @GetMapping("/alerts/{alertId}")
    public ResponseEntity<GuardianAlertResponse> getAlert(
            @PathVariable Long alertId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        return ResponseEntity.ok(
                guardianAlertService.getAlert(
                        guardian.id(),
                        alertId
                )
        );
    }

    @GetMapping("/devices/{deviceId}/alerts")
    public ResponseEntity<List<GuardianAlertResponse>>
    getDeviceAlerts(
            @PathVariable Long deviceId,
            @RequestParam(required = false)
            AlertStatus status,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        return ResponseEntity.ok(
                guardianAlertService.getDeviceAlerts(
                        guardian.id(),
                        deviceId,
                        status
                )
        );
    }

    @PostMapping("/alerts/{alertId}/acknowledge")
    public ResponseEntity<GuardianAlertResponse>
    acknowledgeAlert(
            @PathVariable Long alertId,
            @Valid @RequestBody
            AlertActionRequest request,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        return ResponseEntity.ok(
                guardianAlertService.acknowledgeAlert(
                        guardian.id(),
                        alertId,
                        request
                )
        );
    }

    @PostMapping("/alerts/{alertId}/resolve")
    public ResponseEntity<GuardianAlertResponse> resolveAlert(
            @PathVariable Long alertId,
            @Valid @RequestBody
            AlertActionRequest request,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        return ResponseEntity.ok(
                guardianAlertService.resolveAlert(
                        guardian.id(),
                        alertId,
                        request
                )
        );
    }

    private AuthenticatedGuardian authenticatedGuardian(
            Authentication authentication
    ) {
        return (AuthenticatedGuardian)
                authentication.getPrincipal();
    }
}