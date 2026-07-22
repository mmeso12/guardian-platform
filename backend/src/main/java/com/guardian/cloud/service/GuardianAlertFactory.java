package com.guardian.cloud.service;

import com.guardian.cloud.entity.AlertStatus;
import com.guardian.cloud.entity.Device;
import com.guardian.cloud.entity.DeviceEvent;
import com.guardian.cloud.entity.EventType;
import com.guardian.cloud.entity.GuardianAlert;
import com.guardian.cloud.repository.GuardianAlertRepository;
import org.springframework.stereotype.Service;

@Service
public class GuardianAlertFactory {

    private final GuardianAlertRepository guardianAlertRepository;

    public GuardianAlertFactory(
            GuardianAlertRepository guardianAlertRepository
    ) {
        this.guardianAlertRepository = guardianAlertRepository;
    }

    public GuardianAlert createFromDeviceEvent(
            DeviceEvent event
    ) {
        boolean alreadyExists =
                guardianAlertRepository
                        .existsByDeviceEventId(event.getId());

        if (alreadyExists) {
            return null;
        }

        GuardianAlert alert = new GuardianAlert();

        alert.setDeviceEvent(event);
        alert.setDevice(event.getDevice());
        alert.setEventType(event.getEventType());
        alert.setSeverity(event.getSeverity());
        alert.setStatus(AlertStatus.OPEN);
        alert.setTitle(resolveTitle(event.getEventType()));
        alert.setMessage(resolveMessage(event));
        alert.setLatitude(event.getLatitude());
        alert.setLongitude(event.getLongitude());
        alert.setOpenedAt(event.getRecordedAt());

        return guardianAlertRepository.save(alert);
    }

    private String resolveTitle(EventType eventType) {
        return switch (eventType) {
            case SOS ->
                    "Emergency SOS activated";

            case TAMPER ->
                    "Device tampering detected";

            case LOW_BATTERY ->
                    "Device battery is low";

            case DEVICE_ONLINE ->
                    "Device is online";

            case DEVICE_OFFLINE ->
                    "Device is offline";

            case GEOFENCE_ENTRY ->
                    "Device entered a monitored zone";

            case GEOFENCE_EXIT ->
                    "Device left a monitored zone";
        };
    }

    private String resolveMessage(DeviceEvent event) {
        String deviceName =
                resolveDeviceName(event.getDevice());

        return switch (event.getEventType()) {
            case SOS ->
                    "An emergency SOS signal was received from "
                            + deviceName + ".";

            case TAMPER ->
                    "Possible tampering was detected on "
                            + deviceName + ".";

            case LOW_BATTERY ->
                    deviceName + " reported a low battery level.";

            case DEVICE_ONLINE ->
                    deviceName + " connected to Guardian Cloud.";

            case DEVICE_OFFLINE ->
                    deviceName + " disconnected from Guardian Cloud.";

            case GEOFENCE_ENTRY ->
                    deviceName + " entered a monitored zone.";

            case GEOFENCE_EXIT ->
                    deviceName + " left a monitored zone.";
        };
    }

    private String resolveDeviceName(Device device) {
        if (
                device.getDisplayName() != null
                        && !device.getDisplayName().isBlank()
        ) {
            return device.getDisplayName();
        }

        return device.getDeviceUid();
    }
}