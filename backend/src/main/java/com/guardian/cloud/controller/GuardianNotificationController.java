package com.guardian.cloud.controller;

import com.guardian.cloud.dto.notification.GuardianNotificationResponse;
import com.guardian.cloud.dto.notification.NotificationReadAllResponse;
import com.guardian.cloud.dto.notification.UnreadNotificationCountResponse;
import com.guardian.cloud.security.AuthenticatedGuardian;
import com.guardian.cloud.service.GuardianNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class GuardianNotificationController {

    private final GuardianNotificationService
            notificationService;

    public GuardianNotificationController(
            GuardianNotificationService
                    notificationService
    ) {
        this.notificationService =
                notificationService;
    }

    @GetMapping
    public ResponseEntity<
            List<GuardianNotificationResponse>
            > getNotifications(
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        return ResponseEntity.ok(
                notificationService.getNotifications(
                        guardian.id()
                )
        );
    }

    @GetMapping("/unread-count")
    public ResponseEntity<
            UnreadNotificationCountResponse
            > getUnreadCount(
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        return ResponseEntity.ok(
                notificationService.getUnreadCount(
                        guardian.id()
                )
        );
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<
            GuardianNotificationResponse
            > markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        return ResponseEntity.ok(
                notificationService.markAsRead(
                        guardian.id(),
                        notificationId
                )
        );
    }

    @PatchMapping("/read-all")
    public ResponseEntity<
            NotificationReadAllResponse
            > markAllAsRead(
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(authentication);

        return ResponseEntity.ok(
                notificationService.markAllAsRead(
                        guardian.id()
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