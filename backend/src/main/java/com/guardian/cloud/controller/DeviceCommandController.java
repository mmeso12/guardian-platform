package com.guardian.cloud.controller;

import com.guardian.cloud.dto.command.DeviceCommandFailureRequest;
import com.guardian.cloud.dto.command.DeviceCommandResponse;
import com.guardian.cloud.dto.command.DeviceCommandResultRequest;
import com.guardian.cloud.security.AuthenticatedDevice;
import com.guardian.cloud.service.DeviceCommandService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/device/commands")
public class DeviceCommandController {

    private final DeviceCommandService
            deviceCommandService;

    public DeviceCommandController(
            DeviceCommandService deviceCommandService
    ) {
        this.deviceCommandService =
                deviceCommandService;
    }

    @GetMapping("/pending")
    public ResponseEntity<
            List<DeviceCommandResponse>
            > fetchPendingCommands(
            Authentication authentication
    ) {
        AuthenticatedDevice device =
                device(authentication);

        return ResponseEntity.ok(
                deviceCommandService
                        .fetchPendingCommands(
                                device.id()
                        )
        );
    }

    @PatchMapping("/{commandId}/received")
    public ResponseEntity<DeviceCommandResponse>
    markReceived(
            @PathVariable Long commandId,
            Authentication authentication
    ) {
        AuthenticatedDevice device =
                device(authentication);

        return ResponseEntity.ok(
                deviceCommandService
                        .markReceived(
                                device.id(),
                                commandId
                        )
        );
    }

    @PatchMapping("/{commandId}/executing")
    public ResponseEntity<DeviceCommandResponse>
    markExecuting(
            @PathVariable Long commandId,
            Authentication authentication
    ) {
        AuthenticatedDevice device =
                device(authentication);

        return ResponseEntity.ok(
                deviceCommandService
                        .markExecuting(
                                device.id(),
                                commandId
                        )
        );
    }

    @PatchMapping("/{commandId}/complete")
    public ResponseEntity<DeviceCommandResponse>
    completeCommand(
            @PathVariable Long commandId,

            @RequestBody(required = false)
            DeviceCommandResultRequest request,

            Authentication authentication
    ) {
        AuthenticatedDevice device =
                device(authentication);

        DeviceCommandResultRequest safeRequest =
                request == null
                        ? new DeviceCommandResultRequest(
                        null
                )
                        : request;

        return ResponseEntity.ok(
                deviceCommandService
                        .completeCommand(
                                device.id(),
                                commandId,
                                safeRequest
                        )
        );
    }

    @PatchMapping("/{commandId}/fail")
    public ResponseEntity<DeviceCommandResponse>
    failCommand(
            @PathVariable Long commandId,

            @Valid
            @RequestBody
            DeviceCommandFailureRequest request,

            Authentication authentication
    ) {
        AuthenticatedDevice device =
                device(authentication);

        return ResponseEntity.ok(
                deviceCommandService
                        .failCommand(
                                device.id(),
                                commandId,
                                request
                        )
        );
    }

    private AuthenticatedDevice device(
            Authentication authentication
    ) {
        return (AuthenticatedDevice)
                authentication.getPrincipal();
    }
}