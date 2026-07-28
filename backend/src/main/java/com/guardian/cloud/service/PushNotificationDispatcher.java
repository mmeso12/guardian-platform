package com.guardian.cloud.service;

import com.guardian.cloud.config.PushNotificationProperties;
import com.guardian.cloud.dto.push.PushNotificationDeliveryResponse;
import com.guardian.cloud.entity.*;
import com.guardian.cloud.repository.GuardianMobileDeviceRepository;
import com.guardian.cloud.repository.PushNotificationDeliveryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PushNotificationDispatcher {

    private final GuardianMobileDeviceRepository
            mobileDeviceRepository;

    private final PushNotificationDeliveryRepository
            deliveryRepository;

    private final PushNotificationProvider provider;

    private final PushNotificationProperties properties;

    public PushNotificationDispatcher(
            GuardianMobileDeviceRepository
                    mobileDeviceRepository,
            PushNotificationDeliveryRepository
                    deliveryRepository,
            PushNotificationProvider provider,
            PushNotificationProperties properties
    ) {
        this.mobileDeviceRepository =
                mobileDeviceRepository;

        this.deliveryRepository =
                deliveryRepository;

        this.provider = provider;
        this.properties = properties;
    }

    @Transactional
    public void dispatch(
            List<GuardianNotification> notifications
    ) {
        if (
                notifications == null
                        || notifications.isEmpty()
        ) {
            return;
        }

        for (GuardianNotification notification :
                notifications) {
            dispatch(notification);
        }
    }

    @Transactional
    public void dispatch(
            GuardianNotification notification
    ) {
        if (
                notification == null
                        || notification.getId() == null
                        || notification.getGuardianUser() == null
                        || notification
                        .getGuardianUser()
                        .getId() == null
        ) {
            throw new IllegalArgumentException(
                    "Push dispatch requires a persisted guardian notification"
            );
        }

        List<GuardianMobileDevice> mobileDevices =
                mobileDeviceRepository
                        .findAllByGuardianUserIdAndEnabledTrue(
                                notification
                                        .getGuardianUser()
                                        .getId()
                        );

        for (GuardianMobileDevice mobileDevice :
                mobileDevices) {
            createAndSend(
                    notification,
                    mobileDevice
            );
        }
    }

    @Transactional
    public int retryDueDeliveries() {
        Instant now = Instant.now();

        List<PushNotificationDelivery> deliveries =
                deliveryRepository
                        .findDueDeliveriesForUpdate(
                                PushDeliveryStatus.FAILED,
                                now
                        );

        int processed = 0;

        for (PushNotificationDelivery delivery :
                deliveries) {
            if (
                    delivery.getGuardianMobileDevice()
                            .isEnabled()
            ) {
                attemptDelivery(delivery);
            } else {
                delivery.setNextRetryAt(null);
                delivery.setFailureReason(
                        "Mobile device is disabled"
                );
                deliveryRepository.save(delivery);
            }

            processed++;
        }

        return processed;
    }

    @Transactional(readOnly = true)
    public List<PushNotificationDeliveryResponse>
    getGuardianDeliveries(Long guardianUserId) {
        return deliveryRepository
                .findAllByGuardianMobileDeviceGuardianUserIdOrderByCreatedAtDesc(
                        guardianUserId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void createAndSend(
            GuardianNotification notification,
            GuardianMobileDevice mobileDevice
    ) {
        boolean exists =
                deliveryRepository
                        .existsByGuardianNotificationIdAndGuardianMobileDeviceId(
                                notification.getId(),
                                mobileDevice.getId()
                        );

        if (exists) {
            return;
        }

        PushNotificationDelivery delivery =
                new PushNotificationDelivery();

        delivery.setGuardianNotification(
                notification
        );
        delivery.setGuardianMobileDevice(
                mobileDevice
        );
        delivery.setStatus(
                PushDeliveryStatus.PENDING
        );
        delivery.setProvider(
                provider.providerName()
        );
        delivery.setAttemptCount(0);

        delivery =
                deliveryRepository.save(delivery);

        attemptDelivery(delivery);
    }

    private void attemptDelivery(
            PushNotificationDelivery delivery
    ) {
        int newAttemptCount =
                delivery.getAttemptCount() + 1;

        delivery.setAttemptCount(newAttemptCount);
        delivery.setNextRetryAt(null);
        delivery.setFailureReason(null);

        PushDeliveryResult result;

        try {
            result = provider.send(
                    buildMessage(delivery)
            );
        } catch (RuntimeException exception) {
            result = PushDeliveryResult.failure(
                    exception.getMessage() == null
                            ? "Unexpected provider failure"
                            : exception.getMessage()
            );
        }

        Instant now = Instant.now();

        if (result.successful()) {
            delivery.setStatus(
                    PushDeliveryStatus.SENT
            );
            delivery.setProviderMessageId(
                    result.providerMessageId()
            );
            delivery.setFailureReason(null);
            delivery.setSentAt(now);
            delivery.setNextRetryAt(null);

            deliveryRepository.save(delivery);
            return;
        }

        delivery.setProviderMessageId(null);
        delivery.setFailureReason(
                result.failureReason()
        );

        if (result.invalidToken()) {
            delivery.setStatus(
                    PushDeliveryStatus.INVALID_TOKEN
            );
            delivery.setNextRetryAt(null);

            GuardianMobileDevice mobileDevice =
                    delivery.getGuardianMobileDevice();

            mobileDevice.setEnabled(false);

            mobileDeviceRepository.save(
                    mobileDevice
            );

            deliveryRepository.save(delivery);
            return;
        }

        delivery.setStatus(
                PushDeliveryStatus.FAILED
        );

        if (
                newAttemptCount
                        < properties.getMaxAttempts()
        ) {
            delivery.setNextRetryAt(
                    now.plus(
                            properties.getRetryDelay()
                    )
            );
        } else {
            delivery.setNextRetryAt(null);
        }

        deliveryRepository.save(delivery);
    }

    private PushNotificationMessage buildMessage(
            PushNotificationDelivery delivery
    ) {
        GuardianNotification notification =
                delivery.getGuardianNotification();

        GuardianAlert alert =
                notification.getGuardianAlert();

        Device device = alert.getDevice();

        Map<String, String> data =
                new LinkedHashMap<>();

        data.put(
                "notificationId",
                notification.getId().toString()
        );

        data.put(
                "alertId",
                alert.getId().toString()
        );

        data.put(
                "eventType",
                alert.getEventType().name()
        );

        data.put(
                "severity",
                alert.getSeverity().name()
        );

        data.put(
                "deviceId",
                device.getId().toString()
        );

        data.put(
                "deviceUid",
                device.getDeviceUid()
        );

        if (alert.getLatitude() != null) {
            data.put(
                    "latitude",
                    alert.getLatitude().toString()
            );
        }

        if (alert.getLongitude() != null) {
            data.put(
                    "longitude",
                    alert.getLongitude().toString()
            );
        }

        GuardianMobileDevice mobileDevice =
                delivery.getGuardianMobileDevice();

        return new PushNotificationMessage(
                mobileDevice.getPushToken(),
                mobileDevice.getPlatform(),
                alert.getTitle(),
                alert.getMessage(),
                data
        );
    }

    private PushNotificationDeliveryResponse toResponse(
            PushNotificationDelivery delivery
    ) {
        GuardianMobileDevice mobileDevice =
                delivery.getGuardianMobileDevice();

        return new PushNotificationDeliveryResponse(
                delivery.getId(),
                delivery
                        .getGuardianNotification()
                        .getId(),
                mobileDevice.getId(),
                mobileDevice.getDeviceName(),
                mobileDevice.getPlatform(),
                delivery.getStatus(),
                delivery.getProvider(),
                delivery.getAttemptCount(),
                delivery.getProviderMessageId(),
                delivery.getFailureReason(),
                delivery.getNextRetryAt(),
                delivery.getSentAt(),
                delivery.getCreatedAt(),
                delivery.getUpdatedAt()
        );
    }
}