package com.guardian.cloud.repository;

import com.guardian.cloud.entity.EmergencyContactAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmergencyContactAttemptRepository
        extends JpaRepository<EmergencyContactAttempt, Long> {

    List<EmergencyContactAttempt>
    findAllByEmergencyEscalationIdOrderByAttemptNumberAsc(
            Long escalationId
    );

    Optional<EmergencyContactAttempt>
    findFirstByEmergencyEscalationIdOrderByAttemptNumberDesc(
            Long escalationId
    );
}