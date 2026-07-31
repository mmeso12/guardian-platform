package com.guardian.cloud.controller;

import com.guardian.cloud.dto.device.AssignDeviceToChildRequest;
import com.guardian.cloud.dto.device.DeviceChildAssignmentResponse;
import com.guardian.cloud.security.AuthenticatedGuardian;
import com.guardian.cloud.service.DeviceChildAssignmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class DeviceChildAssignmentController {

    private final DeviceChildAssignmentService
            assignmentService;

    public DeviceChildAssignmentController(
            DeviceChildAssignmentService assignmentService
    ) {
        this.assignmentService = assignmentService;
    }

    @PostMapping("/devices/{deviceId}/child")
    public ResponseEntity<DeviceChildAssignmentResponse>
    assignDevice(
            @PathVariable Long deviceId,
            @Valid @RequestBody
            AssignDeviceToChildRequest request,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                getGuardian(authentication);

        DeviceChildAssignmentResponse response =
                assignmentService.assignDevice(
                        guardian.id(),
                        deviceId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/devices/{deviceId}/child")
    public ResponseEntity<DeviceChildAssignmentResponse>
    reassignDevice(
            @PathVariable Long deviceId,
            @Valid @RequestBody
            AssignDeviceToChildRequest request,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                getGuardian(authentication);

        return ResponseEntity.ok(
                assignmentService.reassignDevice(
                        guardian.id(),
                        deviceId,
                        request
                )
        );
    }

    @GetMapping("/devices/{deviceId}/child")
    public ResponseEntity<DeviceChildAssignmentResponse>
    getDeviceAssignment(
            @PathVariable Long deviceId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                getGuardian(authentication);

        return ResponseEntity.ok(
                assignmentService.getDeviceAssignment(
                        guardian.id(),
                        deviceId
                )
        );
    }

    @DeleteMapping("/devices/{deviceId}/child")
    public ResponseEntity<Void> unassignDevice(
            @PathVariable Long deviceId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                getGuardian(authentication);

        assignmentService.unassignDevice(
                guardian.id(),
                deviceId
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/devices/{deviceId}/child/history")
    public ResponseEntity<List<DeviceChildAssignmentResponse>>
    getDeviceAssignmentHistory(
            @PathVariable Long deviceId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                getGuardian(authentication);

        return ResponseEntity.ok(
                assignmentService
                        .getDeviceAssignmentHistory(
                                guardian.id(),
                                deviceId
                        )
        );
    }

    @GetMapping("/children/{childId}/devices")
    public ResponseEntity<List<DeviceChildAssignmentResponse>>
    getChildDevices(
            @PathVariable UUID childId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                getGuardian(authentication);

        return ResponseEntity.ok(
                assignmentService.getChildDevices(
                        guardian.id(),
                        childId
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