package com.guardian.cloud.repository;

import com.guardian.cloud.entity.EmergencyEscalation;
import com.guardian.cloud.entity.EmergencyEscalationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EmergencyEscalationRepository
        extends JpaRepository<EmergencyEscalation, Long> {

    boolean existsByGuardianAlertIdAndGuardianUserId(
            Long alertId,
            Long guardianUserId
    );

    List<EmergencyEscalation>
    findAllByGuardianUserIdOrderByCreatedAtDesc(
            Long guardianUserId
    );

    Optional<EmergencyEscalation>
    findByIdAndGuardianUserId(
            Long escalationId,
            Long guardianUserId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT escalation
            FROM EmergencyEscalation escalation
            WHERE escalation.id = :escalationId
              AND escalation.guardianUser.id = :guardianUserId
            """)
    Optional<EmergencyEscalation>
    findOwnedByIdForUpdate(
            @Param("escalationId")
            Long escalationId,

            @Param("guardianUserId")
            Long guardianUserId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT escalation
            FROM EmergencyEscalation escalation
            WHERE escalation.status = :status
              AND escalation.nextActionAt IS NOT NULL
              AND escalation.nextActionAt <= :now
            ORDER BY escalation.nextActionAt ASC
            """)
    List<EmergencyEscalation>
    findDueEscalationsForUpdate(
            @Param("status")
            EmergencyEscalationStatus status,

            @Param("now")
            Instant now
    );
}