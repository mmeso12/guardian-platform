package com.guardian.cloud.repository;

import com.guardian.cloud.entity.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmergencyContactRepository
        extends JpaRepository<EmergencyContact, Long> {

    List<EmergencyContact>
    findAllByGuardianUserIdOrderByPriorityAsc(
            Long guardianUserId
    );

    List<EmergencyContact>
    findAllByGuardianUserIdAndEnabledTrueOrderByPriorityAsc(
            Long guardianUserId
    );

    Optional<EmergencyContact>
    findByIdAndGuardianUserId(
            Long contactId,
            Long guardianUserId
    );

    boolean existsByGuardianUserIdAndPriority(
            Long guardianUserId,
            Integer priority
    );

    boolean existsByGuardianUserIdAndPriorityAndIdNot(
            Long guardianUserId,
            Integer priority,
            Long contactId
    );
}