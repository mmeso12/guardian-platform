package com.guardian.cloud.service;

import com.guardian.cloud.dto.notification.GuardianNotificationResponse;
import com.guardian.cloud.dto.notification.NotificationReadAllResponse;
import com.guardian.cloud.dto.notification.UnreadNotificationCountResponse;
import com.guardian.cloud.entity.Device;
import com.guardian.cloud.entity.GuardianAlert;
import com.guardian.cloud.entity.GuardianDeviceAccess;
import com.guardian.cloud.entity.GuardianNotification;
import com.guardian.cloud.entity.GuardianNotificationPreference;
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

    private final PushNotificationDispatcher
            pushNotificationDispatcher;

    private final GuardianNotificationPreferenceService
            preferenceService;

    public GuardianNotificationService(
            GuardianNotificationRepository
                    notificationRepository,
            GuardianDeviceAccessRepository
                    accessRepository,
            PushNotificationDispatcher
                    pushNotificationDispatcher,
            GuardianNotificationPreferenceService
                    preferenceService
    ) {
        this.notificationRepository =
                notificationRepository;

        this.accessRepository =
                accessRepository;

        this.pushNotificationDispatcher =
                pushNotificationDispatcher;

        this.preferenceService =
                preferenceService;
    }

    /**
     * Creates notifications for eligible guardians.
     *
     * A notification may be:
     * - visible in-app only;
     * - sent through push only;
     * - delivered through both channels.
     *
     * Emergency notifications always use both channels.
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

        if (
                accessEntries == null
                        || accessEntries.isEmpty()
        ) {
            return List.of();
        }

        List<GuardianNotification> notifications =
                new ArrayList<>();

        /*
         * This separate list ensures that only
         * push-enabled notifications are dispatched.
         */
        List<GuardianNotification> pushNotifications =
                new ArrayList<>();

        for (GuardianDeviceAccess access : accessEntries) {

            /*
             * Only guardians permitted to manage alerts
             * should receive alert notifications.
             */
            if (!access.isCanManageAlerts()) {
                continue;
            }

            GuardianUser guardian =
                    access.getUser();

            /*
             * This check must happen before guardian.getId().
             */
            if (
                    guardian == null
                            || guardian.getId() == null
                            || !guardian.isEnabled()
            ) {
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

            GuardianNotificationPreference preference =
                    preferenceService.getOrCreate(
                            guardian.getId()
                    );

            boolean visibleInApp =
                    preferenceService
                            .shouldCreateInAppNotification(
                                    preference,
                                    alert.getEventType(),
                                    alert.getSeverity()
                            );

            boolean pushEnabled =
                    preferenceService.shouldSendPush(
                            preference,
                            alert.getEventType(),
                            alert.getSeverity()
                    );

            /*
             * When both channels are disabled for an
             * ordinary event, no record is necessary.
             */
            if (!visibleInApp && !pushEnabled) {
                continue;
            }

            GuardianNotification notification =
                    new GuardianNotification();

            notification.setGuardianUser(
                    guardian
            );

            notification.setGuardianAlert(
                    alert
            );

            notification.setVisibleInApp(
                    visibleInApp
            );

            notifications.add(notification);

            if (pushEnabled) {
                pushNotifications.add(notification);
            }
        }

        if (notifications.isEmpty()) {
            return List.of();
        }

        List<GuardianNotification> savedNotifications =
                notificationRepository.saveAll(
                        notifications
                );

        /*
         * The objects placed in pushNotifications are
         * the same managed entities saved above, so they
         * now contain their generated IDs.
         */
        if (!pushNotifications.isEmpty()) {
            pushNotificationDispatcher.dispatch(
                    pushNotifications
            );
        }

        return savedNotifications;
    }

    /**
     * Returns only notifications intended for the
     * guardian's in-app notification list.
     */
    @Transactional(readOnly = true)
    public List<GuardianNotificationResponse>
    getNotifications(
            Long guardianUserId
    ) {
        return notificationRepository
                .findAllByGuardianUserIdAndVisibleInAppTrueOrderByCreatedAtDesc(
                        guardianUserId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Counts unread notifications that are visible
     * in the application.
     */
    @Transactional(readOnly = true)
    public UnreadNotificationCountResponse
    getUnreadCount(
            Long guardianUserId
    ) {
        long unreadCount =
                notificationRepository
                        .countByGuardianUserIdAndVisibleInAppTrueAndReadAtIsNull(
                                guardianUserId
                        );

        return new UnreadNotificationCountResponse(
                unreadCount
        );
    }

    /**
     * Marks one guardian-owned, visible notification
     * as read.
     */
    @Transactional
    public GuardianNotificationResponse markAsRead(
            Long guardianUserId,
            Long notificationId
    ) {
        GuardianNotification notification =
                notificationRepository
                        .findByIdAndGuardianUserIdAndVisibleInAppTrue(
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
            notification.setReadAt(
                    Instant.now()
            );

            notification =
                    notificationRepository.save(
                            notification
                    );
        }

        return toResponse(notification);
    }

    /**
     * Marks all unread, visible notifications as read.
     */
    @Transactional
    public NotificationReadAllResponse markAllAsRead(
            Long guardianUserId
    ) {
        int updatedCount =
                notificationRepository
                        .markAllVisibleUnreadAsRead(
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

        Device device =
                alert.getDevice();

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

    private void validateAlert(
            GuardianAlert alert
    ) {
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

        if (
                alert.getDevice() == null
                        || alert.getDevice().getId() == null
        ) {
            throw new IllegalArgumentException(
                    "Guardian alert must have a persisted device"
            );
        }

        if (alert.getEventType() == null) {
            throw new IllegalArgumentException(
                    "Guardian alert must have an event type"
            );
        }

        if (alert.getSeverity() == null) {
            throw new IllegalArgumentException(
                    "Guardian alert must have a severity"
            );
        }
    }
}