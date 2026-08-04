package com.guardian.cloud.controller;

import com.guardian.cloud.dto.notification.GuardianNotificationPreferenceResponse;
import com.guardian.cloud.dto.notification.UpdateNotificationPreferenceRequest;
import com.guardian.cloud.security.AuthenticatedGuardian;
import com.guardian.cloud.service.GuardianNotificationPreferenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        "/api/v1/notification-preferences"
)
public class GuardianNotificationPreferenceController {

    private final GuardianNotificationPreferenceService
            preferenceService;

    public GuardianNotificationPreferenceController(
            GuardianNotificationPreferenceService
                    preferenceService
    ) {
        this.preferenceService =
                preferenceService;
    }

    @GetMapping
    public ResponseEntity<
            GuardianNotificationPreferenceResponse
            > getPreferences(
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                guardian(authentication);

        return ResponseEntity.ok(
                preferenceService.getPreferences(
                        guardian.id()
                )
        );
    }

    @PutMapping
    public ResponseEntity<
            GuardianNotificationPreferenceResponse
            > updatePreferences(
            @RequestBody
            UpdateNotificationPreferenceRequest request,

            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                guardian(authentication);

        return ResponseEntity.ok(
                preferenceService.updatePreferences(
                        guardian.id(),
                        request
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