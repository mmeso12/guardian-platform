package com.guardian.cloud.controller;

import com.guardian.cloud.dto.overview.ChildAlertSummaryResponse;
import com.guardian.cloud.dto.overview.ChildDeviceOverviewResponse;
import com.guardian.cloud.dto.overview.ChildLatestLocationResponse;
import com.guardian.cloud.dto.overview.ChildSafetyOverviewResponse;
import com.guardian.cloud.security.AuthenticatedGuardian;
import com.guardian.cloud.service.ChildSafetyOverviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/children/{childId}")
public class ChildSafetyOverviewController {

    private final ChildSafetyOverviewService
            childSafetyOverviewService;

    public ChildSafetyOverviewController(
            ChildSafetyOverviewService
                    childSafetyOverviewService
    ) {
        this.childSafetyOverviewService =
                childSafetyOverviewService;
    }

    @GetMapping("/safety-overview")
    public ResponseEntity<ChildSafetyOverviewResponse>
    getSafetyOverview(
            @PathVariable UUID childId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(
                        authentication
                );

        return ResponseEntity.ok(
                childSafetyOverviewService
                        .getSafetyOverview(
                                guardian.id(),
                                childId
                        )
        );
    }

    @GetMapping("/locations/latest")
    public ResponseEntity<ChildLatestLocationResponse>
    getLatestLocation(
            @PathVariable UUID childId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(
                        authentication
                );

        ChildLatestLocationResponse response =
                childSafetyOverviewService
                        .getLatestLocation(
                                guardian.id(),
                                childId
                        );

        if (response == null) {
            return ResponseEntity
                    .noContent()
                    .build();
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/alert-summary")
    public ResponseEntity<ChildAlertSummaryResponse>
    getAlertSummary(
            @PathVariable UUID childId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(
                        authentication
                );

        return ResponseEntity.ok(
                childSafetyOverviewService
                        .getAlertSummary(
                                guardian.id(),
                                childId
                        )
        );
    }

    @GetMapping("/device-status")
    public ResponseEntity<ChildDeviceOverviewResponse>
    getDeviceStatus(
            @PathVariable UUID childId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(
                        authentication
                );

        ChildDeviceOverviewResponse response =
                childSafetyOverviewService
                        .getDeviceStatus(
                                guardian.id(),
                                childId
                        );

        if (response == null) {
            return ResponseEntity
                    .noContent()
                    .build();
        }

        return ResponseEntity.ok(response);
    }

    private AuthenticatedGuardian
    authenticatedGuardian(
            Authentication authentication
    ) {
        return (AuthenticatedGuardian)
                authentication.getPrincipal();
    }
}