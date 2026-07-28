package com.guardian.cloud.controller;

import com.guardian.cloud.dto.command.CreateDeviceCommandRequest;
import com.guardian.cloud.dto.command.DeviceCommandResponse;
import com.guardian.cloud.security.AuthenticatedGuardian;
import com.guardian.cloud.service.DeviceCommandService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/v1/devices/{deviceId}/commands"
)
public class GuardianDeviceCommandController {

    private final DeviceCommandService
            deviceCommandService;

    public GuardianDeviceCommandController(
            DeviceCommandService deviceCommandService
    ) {
        this.deviceCommandService =
                deviceCommandService;
    }

    @PostMapping
    public ResponseEntity<DeviceCommandResponse>
    createCommand(
            @PathVariable Long deviceId,

            @Valid
            @RequestBody
            CreateDeviceCommandRequest request,

            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                guardian(authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        deviceCommandService
                                .createCommand(
                                        guardian.id(),
                                        deviceId,
                                        request
                                )
                );
    }

    @GetMapping
    public ResponseEntity<
            List<DeviceCommandResponse>
            > getCommands(
            @PathVariable Long deviceId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                guardian(authentication);

        return ResponseEntity.ok(
                deviceCommandService
                        .getGuardianCommands(
                                guardian.id(),
                                deviceId
                        )
        );
    }

    @GetMapping("/{commandId}")
    public ResponseEntity<DeviceCommandResponse>
    getCommand(
            @PathVariable Long deviceId,
            @PathVariable Long commandId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                guardian(authentication);

        return ResponseEntity.ok(
                deviceCommandService
                        .getGuardianCommand(
                                guardian.id(),
                                deviceId,
                                commandId
                        )
        );
    }

    @PostMapping("/{commandId}/cancel")
    public ResponseEntity<DeviceCommandResponse>
    cancelCommand(
            @PathVariable Long deviceId,
            @PathVariable Long commandId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                guardian(authentication);

        return ResponseEntity.ok(
                deviceCommandService
                        .cancelCommand(
                                guardian.id(),
                                deviceId,
                                commandId
                        )
        );
    }

    private AuthenticatedGuardian guardian(
            Authentication authentication
    ) {
        return (AuthenticatedGuardian)
                authentication.getPrincipal();
    }
}