package com.guardian.cloud.controller;

import com.guardian.cloud.dto.auth.MessageResponse;
import com.guardian.cloud.dto.child.ChildResponse;
import com.guardian.cloud.dto.child.CreateChildRequest;
import com.guardian.cloud.dto.child.UpdateChildRequest;
import com.guardian.cloud.security.AuthenticatedGuardian;
import com.guardian.cloud.service.ChildProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/children")
public class ChildProfileController {

    private final ChildProfileService
            childProfileService;

    public ChildProfileController(
            ChildProfileService childProfileService
    ) {
        this.childProfileService =
                childProfileService;
    }

    /**
     * Creates a new child profile.
     */
    @PostMapping
    public ResponseEntity<ChildResponse> createChild(
            Authentication authentication,
            @Valid @RequestBody
            CreateChildRequest request
    ) {
        AuthenticatedGuardian guardian =
                requireAuthenticatedGuardian(
                        authentication
                );

        ChildResponse child =
                childProfileService.createChild(
                        guardian.id(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(child);
    }

    /**
     * Returns active children only.
     */
    @GetMapping
    public ResponseEntity<List<ChildResponse>>
    getActiveChildren(
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                requireAuthenticatedGuardian(
                        authentication
                );

        return ResponseEntity.ok(
                childProfileService
                        .getActiveChildren(
                                guardian.id()
                        )
        );
    }

    /**
     * Returns active and deactivated children.
     */
    @GetMapping("/all")
    public ResponseEntity<List<ChildResponse>>
    getAllChildren(
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                requireAuthenticatedGuardian(
                        authentication
                );

        return ResponseEntity.ok(
                childProfileService.getAllChildren(
                        guardian.id()
                )
        );
    }

    /**
     * Returns one active child.
     */
    @GetMapping("/{childId}")
    public ResponseEntity<ChildResponse> getChild(
            Authentication authentication,
            @PathVariable UUID childId
    ) {
        AuthenticatedGuardian guardian =
                requireAuthenticatedGuardian(
                        authentication
                );

        return ResponseEntity.ok(
                childProfileService.getChild(
                        guardian.id(),
                        childId
                )
        );
    }

    /**
     * Updates one active child.
     */
    @PutMapping("/{childId}")
    public ResponseEntity<ChildResponse> updateChild(
            Authentication authentication,
            @PathVariable UUID childId,
            @Valid @RequestBody
            UpdateChildRequest request
    ) {
        AuthenticatedGuardian guardian =
                requireAuthenticatedGuardian(
                        authentication
                );

        return ResponseEntity.ok(
                childProfileService.updateChild(
                        guardian.id(),
                        childId,
                        request
                )
        );
    }

    /**
     * Soft-deletes one active child.
     */
    @DeleteMapping("/{childId}")
    public ResponseEntity<MessageResponse>
    deactivateChild(
            Authentication authentication,
            @PathVariable UUID childId
    ) {
        AuthenticatedGuardian guardian =
                requireAuthenticatedGuardian(
                        authentication
                );

        childProfileService.deactivateChild(
                guardian.id(),
                childId
        );

        return ResponseEntity.ok(
                new MessageResponse(
                        "Child profile deactivated successfully."
                )
        );
    }

    /**
     * Restores a deactivated child.
     */
    @PostMapping("/{childId}/reactivate")
    public ResponseEntity<ChildResponse>
    reactivateChild(
            Authentication authentication,
            @PathVariable UUID childId
    ) {
        AuthenticatedGuardian guardian =
                requireAuthenticatedGuardian(
                        authentication
                );

        return ResponseEntity.ok(
                childProfileService.reactivateChild(
                        guardian.id(),
                        childId
                )
        );
    }

    private AuthenticatedGuardian
    requireAuthenticatedGuardian(
            Authentication authentication
    ) {
        if (
                authentication == null
                        || !authentication.isAuthenticated()
                        || !(authentication.getPrincipal()
                        instanceof AuthenticatedGuardian guardian)
        ) {
            throw new IllegalStateException(
                    "Authenticated guardian is required"
            );
        }

        return guardian;
    }
}