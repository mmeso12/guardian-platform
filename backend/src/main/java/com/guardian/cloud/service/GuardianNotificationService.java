package com.guardian.cloud.service;

import com.guardian.cloud.dto.notification.GuardianNotificationResponse;
import com.guardian.cloud.dto.notification.NotificationReadAllResponse;
import com.guardian.cloud.dto.notification.UnreadNotificationCountResponse;
import com.guardian.cloud.entity.Device;
import com.guardian.cloud.entity.GuardianAlert;
import com.guardian.cloud.entity.GuardianDeviceAccess;
import com.guardian.cloud.entity.GuardianNotification;
import com.guardian.cloud.entity.GuardianUser;
import com.guardian.cloud.exception.GuardianNotificationNotFoundException;
import com.guardian.cloud.repository.GuardianDeviceAccessRepository;
import com.guardian.cloud.repository.GuardianNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class GuardianNotificationService {

    private final GuardianNotificationRepository
            notificationRepository;

    private final GuardianDeviceAccessRepository
            accessRepository;

    public GuardianNotificationService(
            GuardianNotificationRepository
                    notificationRepository,
            GuardianDeviceAccessRepository
                    accessRepository
    ) {
        this.notificationRepository =
                notificationRepository;

        this.accessRepository =
                accessRepository;
    }

    /**
     * Creates one notification for each enabled guardian
     * who has access to the alert's device.
     */
    @Transactional
    public List<GuardianNotification> createForAlert(
            GuardianAlert alert
    ) {
        validateAlert(alert);

        List<GuardianDeviceAccess> accessEntries =
                accessRepository.findAllByDeviceId(
                        alert.getDevice().getId()
                );

        List<GuardianNotification> notifications =
                new ArrayList<>();

        for (GuardianDeviceAccess access : accessEntries) {
            GuardianUser guardian = access.getUser();

            if (guardian == null
                    || guardian.getId() == null
                    || !guardian.isEnabled()) {
                continue;
            }

            boolean alreadyExists =
                    notificationRepository
                            .existsByGuardianUserIdAndGuardianAlertId(
                                    guardian.getId(),
                                    alert.getId()
                            );

            if (alreadyExists) {
                continue;
            }

            GuardianNotification notification =
                    new GuardianNotification();

            notification.setGuardianUser(guardian);
            notification.setGuardianAlert(alert);

            notifications.add(notification);
        }

        if (notifications.isEmpty()) {
            return List.of();
        }

        return notificationRepository.saveAll(
                notifications
        );
    }

    @Transactional(readOnly = true)
    public List<GuardianNotificationResponse>
    getNotifications(Long guardianUserId) {
        return notificationRepository
                .findAllByGuardianUserIdOrderByCreatedAtDesc(
                        guardianUserId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UnreadNotificationCountResponse
    getUnreadCount(Long guardianUserId) {
        long unreadCount = notificationRepository
                .countByGuardianUserIdAndReadAtIsNull(
                        guardianUserId
                );

        return new UnreadNotificationCountResponse(
                unreadCount
        );
    }

    @Transactional
    public GuardianNotificationResponse markAsRead(
            Long guardianUserId,
            Long notificationId
    ) {
        GuardianNotification notification =
                notificationRepository
                        .findByIdAndGuardianUserId(
                                notificationId,
                                guardianUserId
                        )
                        .orElseThrow(
                                () ->
                                        new GuardianNotificationNotFoundException(
                                                notificationId
                                        )
                        );

        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());

            notification =
                    notificationRepository.save(
                            notification
                    );
        }

        return toResponse(notification);
    }

    @Transactional
    public NotificationReadAllResponse markAllAsRead(
            Long guardianUserId
    ) {
        int updatedCount =
                notificationRepository
                        .markAllUnreadAsRead(
                                guardianUserId,
                                Instant.now()
                        );

        return new NotificationReadAllResponse(
                updatedCount
        );
    }

    private GuardianNotificationResponse toResponse(
            GuardianNotification notification
    ) {
        GuardianAlert alert =
                notification.getGuardianAlert();

        Device device = alert.getDevice();

        return new GuardianNotificationResponse(
                notification.getId(),

                alert.getId(),
                device.getId(),
                device.getDeviceUid(),
                device.getDisplayName(),

                alert.getEventType(),
                alert.getSeverity(),
                alert.getStatus(),

                alert.getTitle(),
                alert.getMessage(),

                alert.getLatitude(),
                alert.getLongitude(),

                notification.isRead(),
                notification.getReadAt(),

                alert.getOpenedAt(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }

    private void validateAlert(GuardianAlert alert) {
        if (alert == null) {
            throw new IllegalArgumentException(
                    "Guardian alert must not be null"
            );
        }

        if (alert.getId() == null) {
            throw new IllegalArgumentException(
                    "Guardian alert must be persisted"
            );
        }

        if (alert.getDevice() == null
                || alert.getDevice().getId() == null) {
            throw new IllegalArgumentException(
                    "Guardian alert must have a persisted device"
            );
        }
    }
}