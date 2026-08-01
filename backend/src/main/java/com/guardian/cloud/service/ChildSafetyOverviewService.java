package com.guardian.cloud.service;

import com.guardian.cloud.dto.overview.ChildAlertSummaryResponse;
import com.guardian.cloud.dto.overview.ChildDeviceOverviewResponse;
import com.guardian.cloud.dto.overview.ChildGeofenceSummaryResponse;
import com.guardian.cloud.dto.overview.ChildLatestAlertResponse;
import com.guardian.cloud.dto.overview.ChildLatestLocationResponse;
import com.guardian.cloud.dto.overview.ChildOverviewProfileResponse;
import com.guardian.cloud.dto.overview.ChildSafetyOverviewResponse;
import com.guardian.cloud.entity.AlertSeverity;
import com.guardian.cloud.entity.AlertStatus;
import com.guardian.cloud.entity.ChildProfile;
import com.guardian.cloud.entity.Device;
import com.guardian.cloud.entity.DeviceChildAssignment;
import com.guardian.cloud.entity.GuardianAlert;
import com.guardian.cloud.entity.LocationRecord;
import com.guardian.cloud.exception.ChildDeviceNotAssignedException;
import com.guardian.cloud.exception.ChildProfileNotFoundException;
import com.guardian.cloud.repository.ChildProfileRepository;
import com.guardian.cloud.repository.DeviceChildAssignmentRepository;
import com.guardian.cloud.repository.GeofenceRepository;
import com.guardian.cloud.repository.GuardianAlertRepository;
import com.guardian.cloud.repository.LocationRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChildSafetyOverviewService {

    private final ChildProfileRepository
            childProfileRepository;

    private final DeviceChildAssignmentRepository
            assignmentRepository;

    private final LocationRecordRepository
            locationRecordRepository;

    private final GuardianAlertRepository
            guardianAlertRepository;

    private final GeofenceRepository
            geofenceRepository;

    public ChildSafetyOverviewService(
            ChildProfileRepository childProfileRepository,
            DeviceChildAssignmentRepository assignmentRepository,
            LocationRecordRepository locationRecordRepository,
            GuardianAlertRepository guardianAlertRepository,
            GeofenceRepository geofenceRepository
    ) {
        this.childProfileRepository =
                childProfileRepository;

        this.assignmentRepository =
                assignmentRepository;

        this.locationRecordRepository =
                locationRecordRepository;

        this.guardianAlertRepository =
                guardianAlertRepository;

        this.geofenceRepository =
                geofenceRepository;
    }

    @Transactional(readOnly = true)
    public ChildSafetyOverviewResponse getSafetyOverview(
            Long guardianUserId,
            UUID childId
    ) {
        ChildProfile child =
                requireActiveOwnedChild(
                        guardianUserId,
                        childId
                );

        Optional<DeviceChildAssignment>
                assignmentOptional =
                assignmentRepository
                        .findFirstByChildProfileIdAndActiveTrueOrderByAssignedAtDesc(
                                child.getId()
                        );

        if (assignmentOptional.isEmpty()) {
            return new ChildSafetyOverviewResponse(
                    toChildResponse(child),
                    false,
                    null,
                    null,
                    emptyAlertSummary(),
                    new ChildGeofenceSummaryResponse(0),
                    Instant.now()
            );
        }

        DeviceChildAssignment assignment =
                assignmentOptional.get();

        Device device = assignment.getDevice();

        ChildLatestLocationResponse latestLocation =
                locationRecordRepository
                        .findFirstByDeviceIdOrderByRecordedAtDesc(
                                device.getId()
                        )
                        .map(this::toLocationResponse)
                        .orElse(null);

        long openAlertCount =
                guardianAlertRepository
                        .countByDeviceIdAndStatus(
                                device.getId(),
                                AlertStatus.OPEN
                        );

        ChildLatestAlertResponse latestAlert =
                guardianAlertRepository
                        .findFirstByDeviceIdOrderByCreatedAtDesc(
                                device.getId()
                        )
                        .map(this::toAlertResponse)
                        .orElse(null);

        ChildLatestAlertResponse latestOpenAlert =
                guardianAlertRepository
                        .findFirstByDeviceIdAndStatusOrderByCreatedAtDesc(
                                device.getId(),
                                AlertStatus.OPEN
                        )
                        .map(this::toAlertResponse)
                        .orElse(null);

        boolean emergencyActive =
                latestOpenAlert != null
                        && latestOpenAlert.severity()
                        == AlertSeverity.EMERGENCY;

        long enabledGeofenceCount =
                geofenceRepository
                        .countByGuardianUserIdAndDeviceIdAndEnabledTrue(
                                guardianUserId,
                                device.getId()
                        );

        return new ChildSafetyOverviewResponse(
                toChildResponse(child),
                true,
                toDeviceResponse(
                        device,
                        assignment
                ),
                latestLocation,
                new ChildAlertSummaryResponse(
                        openAlertCount,
                        emergencyActive,
                        latestAlert,
                        latestOpenAlert
                ),
                new ChildGeofenceSummaryResponse(
                        enabledGeofenceCount
                ),
                Instant.now()
        );
    }

    @Transactional(readOnly = true)
    public ChildLatestLocationResponse getLatestLocation(
            Long guardianUserId,
            UUID childId
    ) {
        Device device =
                requireAssignedDevice(
                        guardianUserId,
                        childId
                );

        return locationRecordRepository
                .findFirstByDeviceIdOrderByRecordedAtDesc(
                        device.getId()
                )
                .map(this::toLocationResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public ChildAlertSummaryResponse getAlertSummary(
            Long guardianUserId,
            UUID childId
    ) {
        Device device =
                requireAssignedDevice(
                        guardianUserId,
                        childId
                );

        long openCount =
                guardianAlertRepository
                        .countByDeviceIdAndStatus(
                                device.getId(),
                                AlertStatus.OPEN
                        );

        ChildLatestAlertResponse latestAlert =
                guardianAlertRepository
                        .findFirstByDeviceIdOrderByCreatedAtDesc(
                                device.getId()
                        )
                        .map(this::toAlertResponse)
                        .orElse(null);

        ChildLatestAlertResponse latestOpenAlert =
                guardianAlertRepository
                        .findFirstByDeviceIdAndStatusOrderByCreatedAtDesc(
                                device.getId(),
                                AlertStatus.OPEN
                        )
                        .map(this::toAlertResponse)
                        .orElse(null);

        boolean emergencyActive =
                latestOpenAlert != null
                        && latestOpenAlert.severity()
                        == AlertSeverity.EMERGENCY;

        return new ChildAlertSummaryResponse(
                openCount,
                emergencyActive,
                latestAlert,
                latestOpenAlert
        );
    }

    @Transactional(readOnly = true)
    public ChildDeviceOverviewResponse getDeviceStatus(
            Long guardianUserId,
            UUID childId
    ) {
        ChildProfile child =
                requireActiveOwnedChild(
                        guardianUserId,
                        childId
                );

        DeviceChildAssignment assignment =
                assignmentRepository
                        .findFirstByChildProfileIdAndActiveTrueOrderByAssignedAtDesc(
                                child.getId()
                        )
                        .orElse(null);

        if (assignment == null) {
            return null;
        }

        return toDeviceResponse(
                assignment.getDevice(),
                assignment
        );
    }

    private ChildProfile requireActiveOwnedChild(
            Long guardianUserId,
            UUID childId
    ) {
        if (
                guardianUserId == null
                        || childId == null
        ) {
            throw new ChildProfileNotFoundException(
                    childId
            );
        }

        return childProfileRepository
                .findByChildIdAndGuardianUserIdAndActiveTrue(
                        childId,
                        guardianUserId
                )
                .orElseThrow(
                        () ->
                                new ChildProfileNotFoundException(
                                        childId
                                )
                );
    }

    private Device requireAssignedDevice(
            Long guardianUserId,
            UUID childId
    ) {
        ChildProfile child =
                requireActiveOwnedChild(
                        guardianUserId,
                        childId
                );

        return assignmentRepository
                .findFirstByChildProfileIdAndActiveTrueOrderByAssignedAtDesc(
                        child.getId()
                )
                .map(
                        DeviceChildAssignment::getDevice
                )
                .orElseThrow(
                        () ->
                                new ChildDeviceNotAssignedException(
                                        childId
                                )
                );
    }

    private ChildOverviewProfileResponse toChildResponse(
            ChildProfile child
    ) {
        return new ChildOverviewProfileResponse(
                child.getChildId(),
                child.getFirstName(),
                child.getLastName(),
                child.getDateOfBirth(),
                child.getGender(),
                child.getProfileImageUrl()
        );
    }

    private ChildDeviceOverviewResponse toDeviceResponse(
            Device device,
            DeviceChildAssignment assignment
    ) {
        return new ChildDeviceOverviewResponse(
                device.getId(),
                device.getDeviceUid(),
                device.getDisplayName(),
                device.getStatus(),
                device.getBatteryLevel(),
                device.getMotionState(),
                device.getFirmwareVersion(),
                device.getLastSeenAt(),
                assignment.getAssignedAt()
        );
    }

    private ChildLatestLocationResponse toLocationResponse(
            LocationRecord location
    ) {
        return new ChildLatestLocationResponse(
                location.getId(),
                location.getLatitude(),
                location.getLongitude(),
                location.getAccuracyMeters(),
                location.getSpeedMetersPerSecond(),
                location.getHeadingDegrees(),
                location.getBatteryLevel(),
                location.getMotionState(),
                location.getRecordedAt(),
                location.getReceivedAt()
        );
    }

    private ChildLatestAlertResponse toAlertResponse(
            GuardianAlert alert
    ) {
        return new ChildLatestAlertResponse(
                alert.getId(),
                alert.getEventType(),
                alert.getSeverity(),
                alert.getStatus(),
                alert.getTitle(),
                alert.getMessage(),
                alert.getLatitude(),
                alert.getLongitude(),
                alert.getOpenedAt(),
                alert.getCreatedAt()
        );
    }

    private ChildAlertSummaryResponse emptyAlertSummary() {
        return new ChildAlertSummaryResponse(
                0,
                false,
                null,
                null
        );
    }
}