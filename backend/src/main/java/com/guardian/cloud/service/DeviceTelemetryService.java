package com.guardian.cloud.service;

import com.guardian.cloud.dto.device.DeviceTelemetryRequest;
import com.guardian.cloud.dto.device.DeviceTelemetryResponse;
import com.guardian.cloud.dto.device.LocationPayload;
import com.guardian.cloud.entity.Device;
import com.guardian.cloud.entity.DeviceStatus;
import com.guardian.cloud.entity.LocationRecord;
import com.guardian.cloud.exception.DeviceNotFoundException;
import com.guardian.cloud.exception.DuplicateTelemetryException;
import com.guardian.cloud.repository.DeviceRepository;
import com.guardian.cloud.repository.LocationRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class DeviceTelemetryService {

    private final DeviceRepository deviceRepository;
    private final LocationRecordRepository locationRecordRepository;
    private final GeofenceMonitoringService geofenceMonitoringService;
    private final DeviceEventService deviceEventService;

    public DeviceTelemetryService(
            DeviceRepository deviceRepository,
            LocationRecordRepository locationRecordRepository,
            GeofenceMonitoringService geofenceMonitoringService,
            DeviceEventService deviceEventService
    ) {
        this.deviceRepository = deviceRepository;
        this.locationRecordRepository = locationRecordRepository;
        this.geofenceMonitoringService = geofenceMonitoringService;
        this.deviceEventService = deviceEventService;
    }

    @Transactional
    public DeviceTelemetryResponse processTelemetry(
            DeviceTelemetryRequest request
    ) {
        Device device = deviceRepository
                .findByDeviceUid(request.deviceUid())
                .orElseThrow(
                        () -> new DeviceNotFoundException(
                                request.deviceUid()
                        )
                );
        DeviceStatus previousStatus =
                device.getStatus();

        Instant previousLastSeenAt =
                device.getLastSeenAt();

        boolean duplicateExists = locationRecordRepository
                .existsByDeviceIdAndSequenceNumber(
                        device.getId(),
                        request.sequenceNumber()
                );

        if (duplicateExists) {
            throw new DuplicateTelemetryException(
                    request.deviceUid(),
                    request.sequenceNumber()
            );
        }

        Long lastSequenceNumber =
                device.getLastSequenceNumber();

        if (
                lastSequenceNumber != null
                        && request.sequenceNumber()
                        <= lastSequenceNumber
        ) {
            throw new DuplicateTelemetryException(
                    request.deviceUid(),
                    request.sequenceNumber()
            );
        }

        Instant receivedAt = Instant.now();

        LocationRecord locationRecord =
                createLocationRecord(device, request);

        LocationRecord savedLocationRecord =
                locationRecordRepository.save(
                        locationRecord
                );

        /*
         * Evaluate all enabled geofences after the
         * location record has been persisted.
         */
        geofenceMonitoringService.evaluateLocation(
                savedLocationRecord
        );

        device.setBatteryLevel(
                request.batteryLevel()
        );
        device.setMotionState(
                request.motionState()
        );
        device.setFirmwareVersion(
                request.firmwareVersion()
        );
        device.setLastSequenceNumber(
                request.sequenceNumber()
        );
        device.setLastSeenAt(receivedAt);
        device.setStatus(DeviceStatus.ONLINE);

        deviceRepository.save(device);

        if (previousStatus == DeviceStatus.OFFLINE) {
        deviceEventService.createDeviceOnlineEvent(
                device,
                receivedAt,
                previousLastSeenAt
        );
        }

        return new DeviceTelemetryResponse(
                device.getDeviceUid(),
                request.sequenceNumber(),
                "ACCEPTED",
                receivedAt
        );
    }

    private LocationRecord createLocationRecord(
            Device device,
            DeviceTelemetryRequest request
    ) {
        LocationPayload location =
                request.location();

        LocationRecord record =
                new LocationRecord();

        record.setDevice(device);
        record.setSequenceNumber(
                request.sequenceNumber()
        );
        record.setLatitude(
                location.latitude()
        );
        record.setLongitude(
                location.longitude()
        );
        record.setAccuracyMeters(
                location.accuracyMeters()
        );
        record.setSpeedMetersPerSecond(
                location.speedMetersPerSecond()
        );
        record.setHeadingDegrees(
                location.headingDegrees()
        );
        record.setBatteryLevel(
                request.batteryLevel()
        );
        record.setMotionState(
                request.motionState()
        );
        record.setRecordedAt(
                request.recordedAt()
        );

        return record;
    }
}