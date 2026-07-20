package com.guardian.cloud.service;

import com.guardian.cloud.dto.location.DeviceLocationResponse;
import com.guardian.cloud.dto.location.LocationResponse;
import com.guardian.cloud.entity.Device;
import com.guardian.cloud.entity.GuardianDeviceAccess;
import com.guardian.cloud.entity.LocationRecord;
import com.guardian.cloud.exception.InvalidLocationRangeException;
import com.guardian.cloud.exception.LocationAccessDeniedException;
import com.guardian.cloud.repository.GuardianDeviceAccessRepository;
import com.guardian.cloud.repository.LocationRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class GuardianLocationService {

    private static final Duration MAX_HISTORY_RANGE =
            Duration.ofDays(30);

    private final GuardianDeviceAccessRepository accessRepository;
    private final LocationRecordRepository locationRecordRepository;

    public GuardianLocationService(
            GuardianDeviceAccessRepository accessRepository,
            LocationRecordRepository locationRecordRepository
    ) {
        this.accessRepository = accessRepository;
        this.locationRecordRepository = locationRecordRepository;
    }

    @Transactional(readOnly = true)
    public DeviceLocationResponse getLatestLocation(
            Long userId,
            Long deviceId
    ) {
        GuardianDeviceAccess access =
                requireLocationAccess(userId, deviceId);

        Device device = access.getDevice();

        LocationResponse latestLocation =
                locationRecordRepository
                        .findTopByDeviceIdOrderByRecordedAtDesc(deviceId)
                        .map(this::toResponse)
                        .orElse(null);

        return new DeviceLocationResponse(
                device.getId(),
                device.getDeviceUid(),
                device.getDisplayName(),
                device.getStatus(),
                device.getLastSeenAt(),
                latestLocation
        );
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> getRecentLocationHistory(
            Long userId,
            Long deviceId
    ) {
        requireLocationAccess(userId, deviceId);

        return locationRecordRepository
                .findTop100ByDeviceIdOrderByRecordedAtDesc(deviceId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> getLocationHistory(
            Long userId,
            Long deviceId,
            Instant from,
            Instant to
    ) {
        requireLocationAccess(userId, deviceId);
        validateRange(from, to);

        return locationRecordRepository
                .findAllByDeviceIdAndRecordedAtBetweenOrderByRecordedAtDesc(
                        deviceId,
                        from,
                        to
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private GuardianDeviceAccess requireLocationAccess(
            Long userId,
            Long deviceId
    ) {
        GuardianDeviceAccess access =
                accessRepository
                        .findByUserIdAndDeviceId(userId, deviceId)
                        .orElseThrow(
                                () -> new LocationAccessDeniedException(
                                        deviceId
                                )
                        );

        if (!access.isCanViewLocation()) {
            throw new LocationAccessDeniedException(deviceId);
        }

        return access;
    }

    private void validateRange(
            Instant from,
            Instant to
    ) {
        if (from == null || to == null) {
            throw new InvalidLocationRangeException(
                    "Both from and to are required"
            );
        }

        if (from.isAfter(to)) {
            throw new InvalidLocationRangeException(
                    "from must be earlier than to"
            );
        }

        if (Duration.between(from, to).compareTo(MAX_HISTORY_RANGE) > 0) {
            throw new InvalidLocationRangeException(
                    "Location history range cannot exceed 30 days"
            );
        }
    }

    private LocationResponse toResponse(
            LocationRecord record
    ) {
        return new LocationResponse(
                record.getId(),
                record.getSequenceNumber(),
                record.getLatitude(),
                record.getLongitude(),
                record.getAccuracyMeters(),
                record.getSpeedMetersPerSecond(),
                record.getHeadingDegrees(),
                record.getBatteryLevel(),
                record.getMotionState(),
                record.getRecordedAt(),
                record.getReceivedAt()
        );
    }
}