package com.guardian.cloud.controller;

import com.guardian.cloud.dto.push.GuardianMobileDeviceResponse;
import com.guardian.cloud.dto.push.PushNotificationDeliveryResponse;
import com.guardian.cloud.dto.push.RegisterMobileDeviceRequest;
import com.guardian.cloud.security.AuthenticatedGuardian;
import com.guardian.cloud.service.GuardianMobileDeviceService;
import com.guardian.cloud.service.PushNotificationDispatcher;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mobile-devices")
public class GuardianMobileDeviceController {

    private final GuardianMobileDeviceService
            mobileDeviceService;

    private final PushNotificationDispatcher
            pushNotificationDispatcher;

    public GuardianMobileDeviceController(
            GuardianMobileDeviceService
                    mobileDeviceService,
            PushNotificationDispatcher
                    pushNotificationDispatcher
    ) {
        this.mobileDeviceService =
                mobileDeviceService;

        this.pushNotificationDispatcher =
                pushNotificationDispatcher;
    }

    @PostMapping
    public ResponseEntity<GuardianMobileDeviceResponse>
    register(
            @Valid
            @RequestBody
            RegisterMobileDeviceRequest request,

            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        mobileDeviceService.register(
                                guardian.id(),
                                request
                        )
                );
    }

    @GetMapping
    public ResponseEntity<
            List<GuardianMobileDeviceResponse>
            > getDevices(
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        return ResponseEntity.ok(
                mobileDeviceService.getDevices(
                        guardian.id()
                )
        );
    }

    @GetMapping("/deliveries")
    public ResponseEntity<
            List<PushNotificationDeliveryResponse>
            > getDeliveries(
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        return ResponseEntity.ok(
                pushNotificationDispatcher
                        .getGuardianDeliveries(
                                guardian.id()
                        )
        );
    }

    @PatchMapping("/{mobileDeviceId}/enable")
    public ResponseEntity<GuardianMobileDeviceResponse>
    enable(
            @PathVariable Long mobileDeviceId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        return ResponseEntity.ok(
                mobileDeviceService.enable(
                        guardian.id(),
                        mobileDeviceId
                )
        );
    }

    @PatchMapping("/{mobileDeviceId}/disable")
    public ResponseEntity<GuardianMobileDeviceResponse>
    disable(
            @PathVariable Long mobileDeviceId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        return ResponseEntity.ok(
                mobileDeviceService.disable(
                        guardian.id(),
                        mobileDeviceId
                )
        );
    }

    @DeleteMapping("/{mobileDeviceId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long mobileDeviceId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        mobileDeviceService.delete(
                guardian.id(),
                mobileDeviceId
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