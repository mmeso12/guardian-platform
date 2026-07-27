package com.guardian.cloud.controller;

import com.guardian.cloud.dto.escalation.*;
import com.guardian.cloud.security.AuthenticatedGuardian;
import com.guardian.cloud.service.EmergencyEscalationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/emergency-escalations")
public class EmergencyEscalationController {

    private final EmergencyEscalationService
            escalationService;

    public EmergencyEscalationController(
            EmergencyEscalationService escalationService
    ) {
        this.escalationService = escalationService;
    }

    @GetMapping
    public ResponseEntity<
            List<EmergencyEscalationResponse>
            > getEscalations(
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        return ResponseEntity.ok(
                escalationService.getEscalations(
                        guardian.id()
                )
        );
    }

    @GetMapping("/{escalationId}")
    public ResponseEntity<EmergencyEscalationResponse>
    getEscalation(
            @PathVariable Long escalationId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        return ResponseEntity.ok(
                escalationService.getEscalation(
                        guardian.id(),
                        escalationId
                )
        );
    }

    @PostMapping("/{escalationId}/acknowledge")
    public ResponseEntity<EmergencyEscalationResponse>
    acknowledge(
            @PathVariable Long escalationId,

            @Valid
            @RequestBody
            EmergencyEscalationActionRequest request,

            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        return ResponseEntity.ok(
                escalationService.acknowledge(
                        guardian.id(),
                        escalationId,
                        request
                )
        );
    }

    @PostMapping("/{escalationId}/resolve")
    public ResponseEntity<EmergencyEscalationResponse>
    resolve(
            @PathVariable Long escalationId,

            @Valid
            @RequestBody
            EmergencyEscalationActionRequest request,

            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        return ResponseEntity.ok(
                escalationService.resolve(
                        guardian.id(),
                        escalationId,
                        request
                )
        );
    }

    @PostMapping("/{escalationId}/escalate")
    public ResponseEntity<EmergencyEscalationResponse>
    escalateNow(
            @PathVariable Long escalationId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        return ResponseEntity.ok(
                escalationService.escalateNow(
                        guardian.id(),
                        escalationId
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