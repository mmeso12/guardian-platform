package com.guardian.cloud.repository;

import com.guardian.cloud.entity.Device;
import com.guardian.cloud.entity.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByDeviceUid(String deviceUid);

    boolean existsByDeviceUid(String deviceUid);

    List<Device> findAllByStatus(DeviceStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT d
            FROM Device d
            WHERE d.deviceUid = :deviceUid
            """)
    Optional<Device> findByDeviceUidForUpdate(
            @Param("deviceUid") String deviceUid
);
}