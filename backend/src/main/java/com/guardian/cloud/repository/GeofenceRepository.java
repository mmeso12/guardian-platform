package com.guardian.cloud.repository;

import com.guardian.cloud.entity.Geofence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GeofenceRepository
        extends JpaRepository<Geofence, Long> {

    List<Geofence>
    findAllByGuardianUserIdOrderByCreatedAtDesc(
            Long guardianUserId
    );

    List<Geofence>
    findAllByGuardianUserIdAndDeviceIdOrderByCreatedAtDesc(
            Long guardianUserId,
            Long deviceId
    );

    Optional<Geofence>
    findByIdAndGuardianUserId(
            Long geofenceId,
            Long guardianUserId
    );

    boolean existsByGuardianUserIdAndDeviceIdAndNameIgnoreCase(
            Long guardianUserId,
            Long deviceId,
            String name
    );

    boolean
    existsByGuardianUserIdAndDeviceIdAndNameIgnoreCaseAndIdNot(
            Long guardianUserId,
            Long deviceId,
            String name,
            Long geofenceId
    );
}