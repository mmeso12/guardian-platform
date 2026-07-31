package com.guardian.cloud.repository;

import com.guardian.cloud.entity.ChildProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChildProfileRepository
        extends JpaRepository<ChildProfile, Long> {

    List<ChildProfile>
    findAllByGuardianUserIdAndActiveTrueOrderByCreatedAtDesc(
            Long guardianUserId
    );

    List<ChildProfile>
    findAllByGuardianUserIdOrderByCreatedAtDesc(
            Long guardianUserId
    );

    Optional<ChildProfile>
    findByChildIdAndGuardianUserId(
            UUID childId,
            Long guardianUserId
    );

    Optional<ChildProfile>
    findByChildIdAndGuardianUserIdAndActiveTrue(
            UUID childId,
            Long guardianUserId
    );

    boolean existsByChildIdAndGuardianUserId(
            UUID childId,
            Long guardianUserId
    );

    long countByGuardianUserIdAndActiveTrue(
            Long guardianUserId
    );
}