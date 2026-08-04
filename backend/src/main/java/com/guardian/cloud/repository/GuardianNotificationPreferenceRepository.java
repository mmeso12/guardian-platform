package com.guardian.cloud.repository;

import com.guardian.cloud.entity.GuardianNotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuardianNotificationPreferenceRepository
        extends JpaRepository<
                GuardianNotificationPreference,
                Long
                > {

    Optional<GuardianNotificationPreference>
    findByGuardianUserId(
            Long guardianUserId
    );
}