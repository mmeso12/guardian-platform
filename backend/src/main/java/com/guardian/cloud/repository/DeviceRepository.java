package com.guardian.cloud.repository;

import com.guardian.cloud.entity.Device;
import com.guardian.cloud.entity.DeviceStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DeviceRepository
        extends JpaRepository<Device, Long> {

    Optional<Device> findByDeviceUid(
            String deviceUid
    );

    boolean existsByDeviceUid(
            String deviceUid
    );

    List<Device> findAllByStatus(
            DeviceStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT device
            FROM Device device
            WHERE device.deviceUid = :deviceUid
            """)
    Optional<Device> findByDeviceUidForUpdate(
            @Param("deviceUid")
            String deviceUid
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT device
            FROM Device device
            WHERE device.status = :status
              AND device.lastSeenAt IS NOT NULL
              AND device.lastSeenAt < :cutoff
            ORDER BY device.lastSeenAt ASC
            """)
    List<Device> findStaleDevicesForUpdate(
            @Param("status")
            DeviceStatus status,

            @Param("cutoff")
            Instant cutoff
    );
}