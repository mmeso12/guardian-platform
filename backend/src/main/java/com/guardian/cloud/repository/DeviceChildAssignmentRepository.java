package com.guardian.cloud.repository;

import com.guardian.cloud.entity.DeviceChildAssignment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceChildAssignmentRepository
        extends JpaRepository<DeviceChildAssignment, Long> {

    Optional<DeviceChildAssignment>
    findByAssignmentId(
            UUID assignmentId
    );

    Optional<DeviceChildAssignment>
    findByDeviceIdAndActiveTrue(
            Long deviceId
    );

    List<DeviceChildAssignment>
    findAllByChildProfileIdAndActiveTrueOrderByAssignedAtDesc(
            Long childProfileId
    );

    List<DeviceChildAssignment>
    findAllByDeviceIdOrderByAssignedAtDesc(
            Long deviceId
    );

    List<DeviceChildAssignment>
    findAllByChildProfileIdOrderByAssignedAtDesc(
            Long childProfileId
    );

    boolean existsByDeviceIdAndActiveTrue(
            Long deviceId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT assignment
            FROM DeviceChildAssignment assignment
            WHERE assignment.device.id = :deviceId
              AND assignment.active = true
            """)
    Optional<DeviceChildAssignment>
    findActiveByDeviceIdForUpdate(
            @Param("deviceId")
            Long deviceId
    );
}