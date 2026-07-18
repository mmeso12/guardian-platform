package com.guardian.cloud.service;

import com.guardian.cloud.dto.device.DeviceEventRequest;
import com.guardian.cloud.dto.device.DeviceEventResponse;
import com.guardian.cloud.entity.AlertSeverity;
import com.guardian.cloud.entity.Device;
import com.guardian.cloud.entity.DeviceEvent;
import com.guardian.cloud.entity.DeviceStatus;
import com.guardian.cloud.entity.EventType;
import com.guardian.cloud.exception.DeviceNotFoundException;
import com.guardian.cloud.exception.DuplicateDeviceEventException;
import com.guardian.cloud.repository.DeviceEventRepository;
import com.guardian.cloud.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class DeviceEventService {

    private final DeviceRepository deviceRepository;
    private final DeviceEventRepository deviceEventRepository;

    public DeviceEventService(
            DeviceRepository deviceRepository,
            DeviceEventRepository deviceEventRepository
    ) {
        this.deviceRepository = deviceRepository;
        this.deviceEventRepository = deviceEventRepository;
    }

    @Transactional
    public DeviceEventResponse processEvent(
            String authenticatedDeviceUid,
            DeviceEventRequest request
    ) {
        Device device = deviceRepository
                .findByDeviceUid(authenticatedDeviceUid)
                .orElseThrow(
                        () -> new DeviceNotFoundException(
                                authenticatedDeviceUid
                        )
                );

        boolean duplicate =
                deviceEventRepository
                        .existsByDeviceIdAndSequenceNumber(
                                device.getId(),
                                request.sequenceNumber()
                        );

        if (duplicate) {
            throw new DuplicateDeviceEventException(
                    authenticatedDeviceUid,
                    request.sequenceNumber()
            );
        }

        DeviceEvent event = new DeviceEvent();

        event.setDevice(device);
        event.setSequenceNumber(request.sequenceNumber());
        event.setEventType(request.eventType());
        event.setSeverity(resolveSeverity(request.eventType()));
        event.setLatitude(request.latitude());
        event.setLongitude(request.longitude());
        event.setBatteryLevel(request.batteryLevel());
        event.setRecordedAt(request.recordedAt());
        event.setMetadata(request.metadata());

        DeviceEvent savedEvent =
                deviceEventRepository.save(event);

        applyDeviceState(device, request);
        device.setLastSeenAt(Instant.now());

        if (request.batteryLevel() != null) {
            device.setBatteryLevel(request.batteryLevel());
        }

        deviceRepository.save(device);

        return new DeviceEventResponse(
                savedEvent.getId(),
                device.getDeviceUid(),
                savedEvent.getSequenceNumber(),
                savedEvent.getEventType(),
                savedEvent.getSeverity(),
                "ACCEPTED",
                savedEvent.getReceivedAt()
        );
    }

    private AlertSeverity resolveSeverity(EventType eventType) {
        return switch (eventType) {
            case SOS -> AlertSeverity.EMERGENCY;

            case TAMPER,
                 GEOFENCE_EXIT,
                 LOW_BATTERY -> AlertSeverity.WARNING;

            case DEVICE_ONLINE,
                 DEVICE_OFFLINE,
                 GEOFENCE_ENTRY -> AlertSeverity.INFORMATIONAL;
        };
    }

    private void applyDeviceState(
            Device device,
            DeviceEventRequest request
    ) {
        switch (request.eventType()) {
            case SOS ->
                    device.setStatus(DeviceStatus.EMERGENCY);

            case TAMPER ->
                    device.setStatus(DeviceStatus.TAMPERED);

            case DEVICE_OFFLINE ->
                    device.setStatus(DeviceStatus.OFFLINE);

            case DEVICE_ONLINE,
                 LOW_BATTERY,
                 GEOFENCE_ENTRY,
                 GEOFENCE_EXIT ->
                    device.setStatus(DeviceStatus.ONLINE);
        }
    }
}