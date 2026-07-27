package com.guardian.cloud.controller;

import com.guardian.cloud.dto.emergencycontact.*;
import com.guardian.cloud.security.AuthenticatedGuardian;
import com.guardian.cloud.service.EmergencyContactService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/emergency-contacts")
public class EmergencyContactController {

    private final EmergencyContactService
            emergencyContactService;

    public EmergencyContactController(
            EmergencyContactService emergencyContactService
    ) {
        this.emergencyContactService =
                emergencyContactService;
    }

    @PostMapping
    public ResponseEntity<EmergencyContactResponse>
    createContact(
            @Valid
            @RequestBody
            CreateEmergencyContactRequest request,

            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                getGuardian(authentication);

        EmergencyContactResponse response =
                emergencyContactService.createContact(
                        guardian.id(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<
            List<EmergencyContactResponse>
            > getContacts(
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                getGuardian(authentication);

        return ResponseEntity.ok(
                emergencyContactService.getContacts(
                        guardian.id()
                )
        );
    }

    @GetMapping("/enabled")
    public ResponseEntity<
            List<EmergencyContactResponse>
            > getEnabledContacts(
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                getGuardian(authentication);

        return ResponseEntity.ok(
                emergencyContactService
                        .getEnabledContacts(
                                guardian.id()
                        )
        );
    }

    @GetMapping("/{contactId}")
    public ResponseEntity<EmergencyContactResponse>
    getContact(
            @PathVariable Long contactId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                getGuardian(authentication);

        return ResponseEntity.ok(
                emergencyContactService.getContact(
                        guardian.id(),
                        contactId
                )
        );
    }

    @PutMapping("/{contactId}")
    public ResponseEntity<EmergencyContactResponse>
    updateContact(
            @PathVariable Long contactId,

            @Valid
            @RequestBody
            UpdateEmergencyContactRequest request,

            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                getGuardian(authentication);

        return ResponseEntity.ok(
                emergencyContactService.updateContact(
                        guardian.id(),
                        contactId,
                        request
                )
        );
    }

    @PatchMapping("/{contactId}/status")
    public ResponseEntity<EmergencyContactResponse>
    setContactStatus(
            @PathVariable Long contactId,

            @Valid
            @RequestBody
            EmergencyContactStatusRequest request,

            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                getGuardian(authentication);

        return ResponseEntity.ok(
                emergencyContactService.setContactStatus(
                        guardian.id(),
                        contactId,
                        request
                )
        );
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<Void> deleteContact(
            @PathVariable Long contactId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                getGuardian(authentication);

        emergencyContactService.deleteContact(
                guardian.id(),
                contactId
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