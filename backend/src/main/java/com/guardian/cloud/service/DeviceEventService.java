package com.guardian.cloud.service;

import com.guardian.cloud.dto.device.DeviceEventRequest;
import com.guardian.cloud.dto.device.DeviceEventResponse;
import com.guardian.cloud.entity.AlertSeverity;
import com.guardian.cloud.entity.Device;
import com.guardian.cloud.entity.DeviceEvent;
import com.guardian.cloud.entity.DeviceStatus;
import com.guardian.cloud.entity.EventType;
import com.guardian.cloud.entity.Geofence;
import com.guardian.cloud.entity.LocationRecord;
import com.guardian.cloud.exception.DeviceNotFoundException;
import com.guardian.cloud.exception.DuplicateDeviceEventException;
import com.guardian.cloud.repository.DeviceEventRepository;
import com.guardian.cloud.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class DeviceEventService {

    private final DeviceRepository deviceRepository;
    private final DeviceEventRepository deviceEventRepository;
    private final GuardianAlertFactory guardianAlertFactory;

    public DeviceEventService(
            DeviceRepository deviceRepository,
            DeviceEventRepository deviceEventRepository,
            GuardianAlertFactory guardianAlertFactory
    ) {
        this.deviceRepository = deviceRepository;
        this.deviceEventRepository = deviceEventRepository;
        this.guardianAlertFactory = guardianAlertFactory;
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

        boolean duplicate = deviceEventRepository
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
        event.setSeverity(
                resolveSeverity(request.eventType())
        );
        event.setLatitude(request.latitude());
        event.setLongitude(request.longitude());
        event.setBatteryLevel(request.batteryLevel());
        event.setRecordedAt(request.recordedAt());
        event.setMetadata(request.metadata());

        DeviceEvent savedEvent =
                deviceEventRepository.save(event);

        guardianAlertFactory.createFromDeviceEvent(
                savedEvent
        );

        applyDeviceState(device, request);

        device.setLastSeenAt(Instant.now());

        if (request.batteryLevel() != null) {
            device.setBatteryLevel(
                    request.batteryLevel()
            );
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

    @Transactional
    public DeviceEvent createGeofenceEntryEvent(
            LocationRecord locationRecord,
            Geofence geofence,
            double distanceMeters
    ) {
        return createGeofenceEvent(
                locationRecord,
                geofence,
                EventType.GEOFENCE_ENTRY,
                distanceMeters
        );
    }

    @Transactional
    public DeviceEvent createGeofenceExitEvent(
            LocationRecord locationRecord,
            Geofence geofence,
            double distanceMeters
    ) {
        return createGeofenceEvent(
                locationRecord,
                geofence,
                EventType.GEOFENCE_EXIT,
                distanceMeters
        );
    }

    private DeviceEvent createGeofenceEvent(
            LocationRecord locationRecord,
            Geofence geofence,
            EventType eventType,
            double distanceMeters
    ) {
        validateGeofenceEventArguments(
                locationRecord,
                geofence,
                eventType
        );

        Device device = locationRecord.getDevice();

        DeviceEvent event = new DeviceEvent();

        event.setDevice(device);
        event.setSequenceNumber(
                generateInternalSequenceNumber(
                        device.getId()
                )
        );
        event.setEventType(eventType);
        event.setSeverity(resolveSeverity(eventType));
        event.setLatitude(
                locationRecord.getLatitude()
        );
        event.setLongitude(
                locationRecord.getLongitude()
        );
        event.setBatteryLevel(
                locationRecord.getBatteryLevel()
        );
        event.setRecordedAt(
                locationRecord.getRecordedAt()
        );
        event.setMetadata(
                buildGeofenceMetadata(
                        locationRecord,
                        geofence,
                        distanceMeters
                )
        );

        DeviceEvent savedEvent =
                deviceEventRepository.save(event);

        guardianAlertFactory.createFromDeviceEvent(
                savedEvent
        );

        return savedEvent;
    }

    private Long generateInternalSequenceNumber(
			Long deviceId
	) {
		long sequenceNumber;

		do {
			long randomValue =
					UUID.randomUUID()
							.getLeastSignificantBits();

			if (randomValue == Long.MIN_VALUE) {
				sequenceNumber = Long.MIN_VALUE + 1;
			} else {
				sequenceNumber =
						-Math.abs(randomValue);
			}

			if (sequenceNumber == 0) {
				sequenceNumber = -1;
			}
		} while (
				deviceEventRepository
						.existsByDeviceIdAndSequenceNumber(
								deviceId,
								sequenceNumber
						)
		);

		return sequenceNumber;
	}

    private String buildGeofenceMetadata(
            LocationRecord locationRecord,
            Geofence geofence,
            double distanceMeters
    ) {
        return """
                {
                  "source": "GEOFENCE_MONITORING",
                  "geofenceId": %d,
                  "geofenceName": "%s",
                  "radiusMeters": %.3f,
                  "distanceMeters": %.3f,
                  "locationRecordId": %d,
                  "locationSequenceNumber": %d
                }
                """.formatted(
                geofence.getId(),
                escapeJson(geofence.getName()),
                geofence.getRadiusMeters(),
                distanceMeters,
                locationRecord.getId(),
                locationRecord.getSequenceNumber()
        );
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void validateGeofenceEventArguments(
            LocationRecord locationRecord,
            Geofence geofence,
            EventType eventType
    ) {
        if (locationRecord == null) {
            throw new IllegalArgumentException(
                    "Location record must not be null"
            );
        }

        if (locationRecord.getId() == null) {
            throw new IllegalArgumentException(
                    "Location record must be persisted"
            );
        }

        if (locationRecord.getDevice() == null) {
            throw new IllegalArgumentException(
                    "Location record must have a device"
            );
        }

        if (locationRecord.getDevice().getId() == null) {
            throw new IllegalArgumentException(
                    "Location record device must be persisted"
            );
        }

        if (locationRecord.getLatitude() == null
                || locationRecord.getLongitude() == null) {
            throw new IllegalArgumentException(
                    "Location record must contain coordinates"
            );
        }

        if (locationRecord.getRecordedAt() == null) {
            throw new IllegalArgumentException(
                    "Location record must contain recordedAt"
            );
        }

        if (geofence == null || geofence.getId() == null) {
            throw new IllegalArgumentException(
                    "Geofence must be persisted"
            );
        }

        if (eventType != EventType.GEOFENCE_ENTRY
                && eventType != EventType.GEOFENCE_EXIT) {
            throw new IllegalArgumentException(
                    "Invalid geofence event type"
            );
        }
    }

    private AlertSeverity resolveSeverity(
            EventType eventType
    ) {
        return switch (eventType) {
            case SOS ->
                    AlertSeverity.EMERGENCY;

            case TAMPER,
                 GEOFENCE_EXIT,
                 LOW_BATTERY ->
                    AlertSeverity.WARNING;

            case DEVICE_ONLINE,
                 DEVICE_OFFLINE,
                 GEOFENCE_ENTRY ->
                    AlertSeverity.INFORMATIONAL;
        };
    }

    private void applyDeviceState(
            Device device,
            DeviceEventRequest request
    ) {
        switch (request.eventType()) {
            case SOS ->
                    device.setStatus(
                            DeviceStatus.EMERGENCY
                    );

            case TAMPER ->
                    device.setStatus(
                            DeviceStatus.TAMPERED
                    );

            case DEVICE_OFFLINE ->
                    device.setStatus(
                            DeviceStatus.OFFLINE
                    );

            case DEVICE_ONLINE,
                 LOW_BATTERY,
                 GEOFENCE_ENTRY,
                 GEOFENCE_EXIT ->
                    device.setStatus(
                            DeviceStatus.ONLINE
                    );
        }
    }

	private void validateAvailabilityEventArguments(
			Device device,
			EventType eventType,
			Instant detectedAt
	) {
		if (device == null || device.getId() == null) {
			throw new IllegalArgumentException(
					"Availability event requires a persisted device"
			);
		}

		if (
				eventType != EventType.DEVICE_OFFLINE
						&& eventType != EventType.DEVICE_ONLINE
		) {
			throw new IllegalArgumentException(
					"Invalid availability event type"
			);
		}

		if (detectedAt == null) {
			throw new IllegalArgumentException(
					"Availability event detectedAt must not be null"
			);
		}
	}

	private String buildAvailabilityMetadata(
			EventType eventType,
			Instant detectedAt,
			Instant previousLastSeenAt
	) {
		String source = eventType == EventType.DEVICE_OFFLINE
				? "DEVICE_OFFLINE_MONITOR"
				: "DEVICE_RECONNECTION";

		String previousSeenValue =
				previousLastSeenAt == null
						? "null"
						: "\""
						+ previousLastSeenAt
						+ "\"";

		return """
				{
				"source": "%s",
				"detectedAt": "%s",
				"previousLastSeenAt": %s
				}
				""".formatted(
				source,
				detectedAt,
				previousSeenValue
		);
	}

	private DeviceEvent createAvailabilityEvent(
			Device device,
			EventType eventType,
			Instant detectedAt,
			Instant previousLastSeenAt
	) {
		validateAvailabilityEventArguments(
				device,
				eventType,
				detectedAt
		);

		DeviceEvent event = new DeviceEvent();

		event.setDevice(device);

		event.setSequenceNumber(
				generateInternalSequenceNumber(
						device.getId()
				)
		);

		event.setEventType(eventType);
		event.setSeverity(resolveSeverity(eventType));
		event.setBatteryLevel(device.getBatteryLevel());
		event.setRecordedAt(detectedAt);

		event.setMetadata(
				buildAvailabilityMetadata(
						eventType,
						detectedAt,
						previousLastSeenAt
				)
		);

		DeviceEvent savedEvent =
				deviceEventRepository.save(event);

		guardianAlertFactory.createFromDeviceEvent(
				savedEvent
		);

		return savedEvent;
	}

	@Transactional
	public DeviceEvent createDeviceOfflineEvent(
			Device device,
			Instant detectedAt,
			Instant lastSeenAt
	) {
		return createAvailabilityEvent(
				device,
				EventType.DEVICE_OFFLINE,
				detectedAt,
				lastSeenAt
		);
	}

	@Transactional
	public DeviceEvent createDeviceOnlineEvent(
			Device device,
			Instant detectedAt,
			Instant previousLastSeenAt
	) {
		return createAvailabilityEvent(
				device,
				EventType.DEVICE_ONLINE,
				detectedAt,
				previousLastSeenAt
		);
	}
}