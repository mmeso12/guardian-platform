package com.guardian.cloud.service;

import com.guardian.cloud.dto.geofence.*;
import com.guardian.cloud.entity.Device;
import com.guardian.cloud.entity.Geofence;
import com.guardian.cloud.entity.GuardianDeviceAccess;
import com.guardian.cloud.entity.GuardianUser;
import com.guardian.cloud.exception.DuplicateGeofenceNameException;
import com.guardian.cloud.exception.GeofenceAccessDeniedException;
import com.guardian.cloud.exception.GeofenceNotFoundException;
import com.guardian.cloud.exception.GuardianUserNotFoundException;
import com.guardian.cloud.repository.GeofenceRepository;
import com.guardian.cloud.repository.GuardianDeviceAccessRepository;
import com.guardian.cloud.repository.GuardianUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GeofenceService {

    private final GeofenceRepository geofenceRepository;
    private final GuardianDeviceAccessRepository accessRepository;
    private final GuardianUserRepository guardianUserRepository;

    public GeofenceService(
            GeofenceRepository geofenceRepository,
            GuardianDeviceAccessRepository accessRepository,
            GuardianUserRepository guardianUserRepository
    ) {
        this.geofenceRepository = geofenceRepository;
        this.accessRepository = accessRepository;
        this.guardianUserRepository = guardianUserRepository;
    }

    @Transactional
    public GeofenceResponse createGeofence(
            Long guardianUserId,
            CreateGeofenceRequest request
    ) {
        GuardianDeviceAccess access = requireDeviceAccess(
                guardianUserId,
                request.deviceId()
        );

        String normalizedName = normalizeRequiredText(
                request.name()
        );

        validateUniqueNameForCreate(
                guardianUserId,
                request.deviceId(),
                normalizedName
        );

        GuardianUser guardianUser = findGuardian(
                guardianUserId
        );

        Device device = access.getDevice();

        Geofence geofence = new Geofence();

        geofence.setGuardianUser(guardianUser);
        geofence.setDevice(device);
        geofence.setName(normalizedName);
        geofence.setDescription(
                normalizeOptionalText(request.description())
        );
        geofence.setCenterLatitude(
                request.centerLatitude()
        );
        geofence.setCenterLongitude(
                request.centerLongitude()
        );
        geofence.setRadiusMeters(
                request.radiusMeters()
        );
        geofence.setEnabled(true);

        return toResponse(
                geofenceRepository.save(geofence)
        );
    }

    @Transactional(readOnly = true)
    public List<GeofenceResponse> getAllGeofences(
            Long guardianUserId
    ) {
        return geofenceRepository
                .findAllByGuardianUserIdOrderByCreatedAtDesc(
                        guardianUserId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GeofenceResponse> getDeviceGeofences(
            Long guardianUserId,
            Long deviceId
    ) {
        requireDeviceAccess(
                guardianUserId,
                deviceId
        );

        return geofenceRepository
                .findAllByGuardianUserIdAndDeviceIdOrderByCreatedAtDesc(
                        guardianUserId,
                        deviceId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GeofenceResponse getGeofence(
            Long guardianUserId,
            Long geofenceId
    ) {
        return toResponse(
                findOwnedGeofence(
                        guardianUserId,
                        geofenceId
                )
        );
    }

    @Transactional
    public GeofenceResponse updateGeofence(
            Long guardianUserId,
            Long geofenceId,
            UpdateGeofenceRequest request
    ) {
        Geofence geofence = findOwnedGeofence(
                guardianUserId,
                geofenceId
        );

        requireDeviceAccess(
                guardianUserId,
                geofence.getDevice().getId()
        );

        String normalizedName = normalizeRequiredText(
                request.name()
        );

        validateUniqueNameForUpdate(
                guardianUserId,
                geofence.getDevice().getId(),
                normalizedName,
                geofenceId
        );

        geofence.setName(normalizedName);
        geofence.setDescription(
                normalizeOptionalText(request.description())
        );
        geofence.setCenterLatitude(
                request.centerLatitude()
        );
        geofence.setCenterLongitude(
                request.centerLongitude()
        );
        geofence.setRadiusMeters(
                request.radiusMeters()
        );

        return toResponse(
                geofenceRepository.save(geofence)
        );
    }

    @Transactional
    public GeofenceResponse setGeofenceStatus(
            Long guardianUserId,
            Long geofenceId,
            GeofenceStatusRequest request
    ) {
        Geofence geofence = findOwnedGeofence(
                guardianUserId,
                geofenceId
        );

        requireDeviceAccess(
                guardianUserId,
                geofence.getDevice().getId()
        );

        geofence.setEnabled(request.enabled());

        return toResponse(
                geofenceRepository.save(geofence)
        );
    }

    @Transactional
    public void deleteGeofence(
            Long guardianUserId,
            Long geofenceId
    ) {
        Geofence geofence = findOwnedGeofence(
                guardianUserId,
                geofenceId
        );

        requireDeviceAccess(
                guardianUserId,
                geofence.getDevice().getId()
        );

        geofenceRepository.delete(geofence);
    }

    private GuardianDeviceAccess requireDeviceAccess(
            Long guardianUserId,
            Long deviceId
    ) {
        return accessRepository
                .findByUserIdAndDeviceId(
                        guardianUserId,
                        deviceId
                )
                .orElseThrow(
                        () -> new GeofenceAccessDeniedException(
                                deviceId
                        )
                );
    }

    private GuardianUser findGuardian(
            Long guardianUserId
    ) {
        return guardianUserRepository
                .findById(guardianUserId)
                .orElseThrow(
                        () -> new GuardianUserNotFoundException(
                                "ID " + guardianUserId
                        )
                );
    }

    private Geofence findOwnedGeofence(
            Long guardianUserId,
            Long geofenceId
    ) {
        return geofenceRepository
                .findByIdAndGuardianUserId(
                        geofenceId,
                        guardianUserId
                )
                .orElseThrow(
                        () -> new GeofenceNotFoundException(
                                geofenceId
                        )
                );
    }

    private void validateUniqueNameForCreate(
            Long guardianUserId,
            Long deviceId,
            String name
    ) {
        boolean duplicate =
                geofenceRepository
                        .existsByGuardianUserIdAndDeviceIdAndNameIgnoreCase(
                                guardianUserId,
                                deviceId,
                                name
                        );

        if (duplicate) {
            throw new DuplicateGeofenceNameException(name);
        }
    }

    private void validateUniqueNameForUpdate(
            Long guardianUserId,
            Long deviceId,
            String name,
            Long geofenceId
    ) {
        boolean duplicate =
                geofenceRepository
                        .existsByGuardianUserIdAndDeviceIdAndNameIgnoreCaseAndIdNot(
                                guardianUserId,
                                deviceId,
                                name,
                                geofenceId
                        );

        if (duplicate) {
            throw new DuplicateGeofenceNameException(name);
        }
    }

    private String normalizeRequiredText(String value) {
        return value.trim();
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private GeofenceResponse toResponse(
            Geofence geofence
    ) {
        Device device = geofence.getDevice();

        return new GeofenceResponse(
                geofence.getId(),
                device.getId(),
                device.getDeviceUid(),
                device.getDisplayName(),
                geofence.getName(),
                geofence.getDescription(),
                geofence.getCenterLatitude(),
                geofence.getCenterLongitude(),
                geofence.getRadiusMeters(),
                geofence.isEnabled(),
                geofence.getCreatedAt(),
                geofence.getUpdatedAt()
        );
    }
}