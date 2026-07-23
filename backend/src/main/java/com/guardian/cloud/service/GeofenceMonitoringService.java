package com.guardian.cloud.service;

import com.guardian.cloud.entity.Geofence;
import com.guardian.cloud.entity.GeofenceState;
import com.guardian.cloud.entity.LocationRecord;
import com.guardian.cloud.repository.GeofenceRepository;
import com.guardian.cloud.repository.GeofenceStateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class GeofenceMonitoringService {

    private final GeofenceRepository geofenceRepository;
    private final GeofenceStateRepository geofenceStateRepository;
    private final GeofenceDistanceCalculator distanceCalculator;
    private final DeviceEventService deviceEventService;

    public GeofenceMonitoringService(
            GeofenceRepository geofenceRepository,
            GeofenceStateRepository geofenceStateRepository,
            GeofenceDistanceCalculator distanceCalculator,
            DeviceEventService deviceEventService
    ) {
        this.geofenceRepository = geofenceRepository;
        this.geofenceStateRepository =
                geofenceStateRepository;
        this.distanceCalculator = distanceCalculator;
        this.deviceEventService = deviceEventService;
    }

    @Transactional
    public void evaluateLocation(
            LocationRecord locationRecord
    ) {
        validateLocationRecord(locationRecord);

        Long deviceId =
                locationRecord.getDevice().getId();

        List<Geofence> enabledGeofences =
                geofenceRepository
                        .findAllByDeviceIdAndEnabledTrueOrderByCreatedAtAsc(
                                deviceId
                        );

        for (Geofence geofence : enabledGeofences) {
            evaluateGeofence(
                    geofence,
                    locationRecord
            );
        }
    }

    private void evaluateGeofence(
            Geofence geofence,
            LocationRecord locationRecord
    ) {
        double distanceMeters =
                distanceCalculator.calculateMeters(
                        locationRecord.getLatitude(),
                        locationRecord.getLongitude(),
                        geofence.getCenterLatitude(),
                        geofence.getCenterLongitude()
                );

        boolean currentlyInside =
                distanceMeters
                        <= geofence.getRadiusMeters();

        GeofenceState state =
                geofenceStateRepository
                        .findByGeofenceIdForUpdate(
                                geofence.getId()
                        )
                        .orElse(null);

        if (state == null) {
            initializeState(
                    geofence,
                    locationRecord,
                    currentlyInside,
                    distanceMeters
            );

            return;
        }

        if (isStaleLocation(state, locationRecord)) {
            return;
        }

        boolean previouslyInside =
                state.isInside();

        updateState(
                state,
                currentlyInside,
                distanceMeters,
                locationRecord.getRecordedAt()
        );

        if (!previouslyInside && currentlyInside) {
            deviceEventService.createGeofenceEntryEvent(
                    locationRecord,
                    geofence,
                    distanceMeters
            );
        } else if (previouslyInside && !currentlyInside) {
            deviceEventService.createGeofenceExitEvent(
                    locationRecord,
                    geofence,
                    distanceMeters
            );
        }
    }

    private void initializeState(
            Geofence geofence,
            LocationRecord locationRecord,
            boolean currentlyInside,
            double distanceMeters
    ) {
        GeofenceState state = new GeofenceState();

        state.setGeofence(geofence);
        state.setInside(currentlyInside);
        state.setLastDistanceMeters(distanceMeters);
        state.setLastEvaluatedAt(
                locationRecord.getRecordedAt()
        );

        if (currentlyInside) {
            state.setEnteredAt(
                    locationRecord.getRecordedAt()
            );
        } else {
            state.setExitedAt(
                    locationRecord.getRecordedAt()
            );
        }

        geofenceStateRepository.save(state);
    }

    private void updateState(
            GeofenceState state,
            boolean currentlyInside,
            double distanceMeters,
            Instant evaluatedAt
    ) {
        boolean previouslyInside =
                state.isInside();

        state.setInside(currentlyInside);
        state.setLastDistanceMeters(distanceMeters);
        state.setLastEvaluatedAt(evaluatedAt);

        if (!previouslyInside && currentlyInside) {
            state.setEnteredAt(evaluatedAt);
        } else if (previouslyInside && !currentlyInside) {
            state.setExitedAt(evaluatedAt);
        }

        geofenceStateRepository.save(state);
    }

    private boolean isStaleLocation(
            GeofenceState state,
            LocationRecord locationRecord
    ) {
        Instant previousEvaluation =
                state.getLastEvaluatedAt();

        Instant incomingRecordedAt =
                locationRecord.getRecordedAt();

        return previousEvaluation != null
                && incomingRecordedAt.isBefore(
                        previousEvaluation
                );
    }

    private void validateLocationRecord(
            LocationRecord locationRecord
    ) {
        if (locationRecord == null) {
            throw new IllegalArgumentException(
                    "Location record must not be null"
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

        if (
                locationRecord.getLatitude() == null
                        || locationRecord.getLongitude() == null
        ) {
            throw new IllegalArgumentException(
                    "Location coordinates must not be null"
            );
        }

        if (locationRecord.getRecordedAt() == null) {
            throw new IllegalArgumentException(
                    "Location recordedAt must not be null"
            );
        }
    }
}