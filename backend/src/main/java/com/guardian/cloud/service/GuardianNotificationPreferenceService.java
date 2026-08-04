package com.guardian.cloud.service;

import com.guardian.cloud.dto.notification.GuardianNotificationPreferenceResponse;
import com.guardian.cloud.dto.notification.UpdateNotificationPreferenceRequest;
import com.guardian.cloud.entity.AlertSeverity;
import com.guardian.cloud.entity.EventType;
import com.guardian.cloud.entity.GuardianNotificationPreference;
import com.guardian.cloud.entity.GuardianUser;
import com.guardian.cloud.exception.GuardianUserNotFoundException;
import com.guardian.cloud.repository.GuardianNotificationPreferenceRepository;
import com.guardian.cloud.repository.GuardianUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuardianNotificationPreferenceService {

    private final GuardianNotificationPreferenceRepository
            preferenceRepository;

    private final GuardianUserRepository
            guardianUserRepository;

    public GuardianNotificationPreferenceService(
            GuardianNotificationPreferenceRepository
                    preferenceRepository,
            GuardianUserRepository guardianUserRepository
    ) {
        this.preferenceRepository =
                preferenceRepository;

        this.guardianUserRepository =
                guardianUserRepository;
    }

    /**
     * Returns the guardian's current preferences.
     *
     * A default preference record is automatically
     * created the first time the guardian accesses
     * notification settings.
     */
    @Transactional
    public GuardianNotificationPreferenceResponse
    getPreferences(
            Long guardianUserId
    ) {
        GuardianNotificationPreference preference =
                getOrCreate(guardianUserId);

        return toResponse(preference);
    }

    /**
     * Updates all guardian-controlled preferences.
     *
     * SOS is not included in the update DTO and is
     * always forced to remain enabled.
     */
    @Transactional
    public GuardianNotificationPreferenceResponse
    updatePreferences(
            Long guardianUserId,
            UpdateNotificationPreferenceRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Notification preference request is required"
            );
        }

        GuardianNotificationPreference preference =
                getOrCreate(guardianUserId);

        preference.setInAppEnabled(
                request.inAppEnabled()
        );

        preference.setPushEnabled(
                request.pushEnabled()
        );

        preference.setInformationalEnabled(
                request.informationalEnabled()
        );

        preference.setWarningEnabled(
                request.warningEnabled()
        );

        /*
         * Emergency SOS notifications cannot be
         * disabled by guardian preferences.
         */
        preference.setSosEnabled(true);

        preference.setTamperEnabled(
                request.tamperEnabled()
        );

        preference.setLowBatteryEnabled(
                request.lowBatteryEnabled()
        );

        preference.setDeviceOnlineEnabled(
                request.deviceOnlineEnabled()
        );

        preference.setDeviceOfflineEnabled(
                request.deviceOfflineEnabled()
        );

        preference.setGeofenceEntryEnabled(
                request.geofenceEntryEnabled()
        );

        preference.setGeofenceExitEnabled(
                request.geofenceExitEnabled()
        );

        GuardianNotificationPreference savedPreference =
                preferenceRepository.save(preference);

        return toResponse(savedPreference);
    }

    /**
     * Loads a guardian's preferences or creates
     * the default preference record.
     */
    @Transactional
    public GuardianNotificationPreference getOrCreate(
            Long guardianUserId
    ) {
        if (guardianUserId == null) {
            throw new GuardianUserNotFoundException(
                    "null ID"
            );
        }

        return preferenceRepository
                .findByGuardianUserId(
                        guardianUserId
                )
                .orElseGet(
                        () -> createDefault(
                                guardianUserId
                        )
                );
    }

    /**
     * Determines whether a notification should be
     * shown in the guardian's in-app notification
     * list.
     */
    public boolean shouldCreateInAppNotification(
            GuardianNotificationPreference preference,
            EventType eventType,
            AlertSeverity severity
    ) {
        requireEvaluationArguments(
                preference,
                eventType,
                severity
        );

        /*
         * Emergency notifications always appear
         * in-app regardless of guardian settings.
         */
        if (severity == AlertSeverity.EMERGENCY) {
            return true;
        }

        return preference.isInAppEnabled()
                && isEventEnabled(
                        preference,
                        eventType,
                        severity
                );
    }

    /**
     * Determines whether push delivery should be
     * attempted for a notification.
     */
    public boolean shouldSendPush(
            GuardianNotificationPreference preference,
            EventType eventType,
            AlertSeverity severity
    ) {
        requireEvaluationArguments(
                preference,
                eventType,
                severity
        );

        /*
         * Emergency notifications always receive
         * push delivery.
         */
        if (severity == AlertSeverity.EMERGENCY) {
            return true;
        }

        return preference.isPushEnabled()
                && isEventEnabled(
                        preference,
                        eventType,
                        severity
                );
    }

    /**
     * Convenience overload used when filtering
     * persisted notifications before push dispatch.
     */
    @Transactional
    public boolean shouldSendPush(
            Long guardianUserId,
            EventType eventType,
            AlertSeverity severity
    ) {
        GuardianNotificationPreference preference =
                getOrCreate(guardianUserId);

        return shouldSendPush(
                preference,
                eventType,
                severity
        );
    }

    private boolean isEventEnabled(
            GuardianNotificationPreference preference,
            EventType eventType,
            AlertSeverity severity
    ) {
        boolean severityEnabled =
                switch (severity) {
                    case INFORMATIONAL ->
                            preference
                                    .isInformationalEnabled();

                    case WARNING ->
                            preference
                                    .isWarningEnabled();

                    case EMERGENCY -> true;
                };

        if (!severityEnabled) {
            return false;
        }

        return switch (eventType) {
            case SOS ->
                    preference.isSosEnabled();

            case TAMPER ->
                    preference.isTamperEnabled();

            case LOW_BATTERY ->
                    preference.isLowBatteryEnabled();

            case DEVICE_ONLINE ->
                    preference.isDeviceOnlineEnabled();

            case DEVICE_OFFLINE ->
                    preference.isDeviceOfflineEnabled();

            case GEOFENCE_ENTRY ->
                    preference.isGeofenceEntryEnabled();

            case GEOFENCE_EXIT ->
                    preference.isGeofenceExitEnabled();
        };
    }

    private GuardianNotificationPreference createDefault(
            Long guardianUserId
    ) {
        GuardianUser guardianUser =
                guardianUserRepository
                        .findById(guardianUserId)
                        .orElseThrow(
                                () ->
                                        new GuardianUserNotFoundException(
                                                "ID "
                                                        + guardianUserId
                                        )
                        );

        GuardianNotificationPreference preference =
                new GuardianNotificationPreference();

        preference.setGuardianUser(
                guardianUser
        );

        /*
         * The entity's field defaults supply the
         * default notification settings.
         */
        preference.setSosEnabled(true);

        return preferenceRepository.save(preference);
    }

    private void requireEvaluationArguments(
            GuardianNotificationPreference preference,
            EventType eventType,
            AlertSeverity severity
    ) {
        if (preference == null) {
            throw new IllegalArgumentException(
                    "Notification preference is required"
            );
        }

        if (eventType == null) {
            throw new IllegalArgumentException(
                    "Notification event type is required"
            );
        }

        if (severity == null) {
            throw new IllegalArgumentException(
                    "Notification severity is required"
            );
        }
    }

    private GuardianNotificationPreferenceResponse
    toResponse(
            GuardianNotificationPreference preference
    ) {
        return new GuardianNotificationPreferenceResponse(
                preference.isInAppEnabled(),
                preference.isPushEnabled(),
                preference.isInformationalEnabled(),
                preference.isWarningEnabled(),
                preference.isSosEnabled(),
                preference.isTamperEnabled(),
                preference.isLowBatteryEnabled(),
                preference.isDeviceOnlineEnabled(),
                preference.isDeviceOfflineEnabled(),
                preference.isGeofenceEntryEnabled(),
                preference.isGeofenceExitEnabled(),
                preference.getCreatedAt(),
                preference.getUpdatedAt()
        );
    }
}