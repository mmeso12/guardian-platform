package com.guardian.cloud.repository;

import com.guardian.cloud.entity.GuardianPasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuardianPasswordResetTokenRepository
        extends JpaRepository<
        GuardianPasswordResetToken,
        Long
        > {

    Optional<GuardianPasswordResetToken>
    findByTokenHash(String tokenHash);

    List<GuardianPasswordResetToken>
    findAllByGuardianUserIdAndUsedAtIsNull(
            Long guardianUserId
    );
}