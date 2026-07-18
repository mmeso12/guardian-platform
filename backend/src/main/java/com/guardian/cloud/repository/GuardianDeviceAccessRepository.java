package com.guardian.cloud.repository;

import com.guardian.cloud.entity.GuardianDeviceAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuardianDeviceAccessRepository
        extends JpaRepository<GuardianDeviceAccess, Long> {

    List<GuardianDeviceAccess> findAllByUserId(Long userId);

    List<GuardianDeviceAccess> findAllByDeviceId(Long deviceId);

    Optional<GuardianDeviceAccess> findByUserIdAndDeviceId(
            Long userId,
            Long deviceId
    );

    boolean existsByUserIdAndDeviceId(
            Long userId,
            Long deviceId
    );
}