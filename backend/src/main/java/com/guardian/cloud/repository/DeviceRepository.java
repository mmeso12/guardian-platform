package com.guardian.cloud.repository;

import com.guardian.cloud.entity.Device;
import com.guardian.cloud.entity.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByDeviceUid(String deviceUid);

    boolean existsByDeviceUid(String deviceUid);

    List<Device> findAllByStatus(DeviceStatus status);
}