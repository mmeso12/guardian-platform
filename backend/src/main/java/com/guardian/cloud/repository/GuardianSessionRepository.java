package com.guardian.cloud.repository;

import com.guardian.cloud.entity.GuardianSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GuardianSessionRepository
        extends JpaRepository<GuardianSession, Long> {

    Optional<GuardianSession>
    findByRefreshTokenHash(String refreshTokenHash);

    Optional<GuardianSession>
    findBySessionId(UUID sessionId);

    Optional<GuardianSession>
    findBySessionIdAndGuardianUserId(
            UUID sessionId,
            Long guardianUserId
    );

    List<GuardianSession>
    findAllByGuardianUserIdOrderByCreatedAtDesc(
            Long guardianUserId
    );

    @Modifying
    @Query("""
            UPDATE GuardianSession session
               SET session.revokedAt = :now,
                   session.revocationReason = :reason
             WHERE session.guardianUser.id = :guardianUserId
               AND session.revokedAt IS NULL
            """)
    int revokeAllForGuardian(
            @Param("guardianUserId")
            Long guardianUserId,

            @Param("now")
            Instant now,

            @Param("reason")
            String reason
    );
}