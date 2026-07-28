package com.guardian.cloud.repository;

import com.guardian.cloud.entity.GuardianMobileDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuardianMobileDeviceRepository
        extends JpaRepository<GuardianMobileDevice, Long> {

    List<GuardianMobileDevice>
    findAllByGuardianUserIdOrderByCreatedAtDesc(
            Long guardianUserId
    );

    List<GuardianMobileDevice>
    findAllByGuardianUserIdAndEnabledTrue(
            Long guardianUserId
    );

    Optional<GuardianMobileDevice>
    findByIdAndGuardianUserId(
            Long mobileDeviceId,
            Long guardianUserId
    );

    Optional<GuardianMobileDevice>
    findByPushToken(String pushToken);
}