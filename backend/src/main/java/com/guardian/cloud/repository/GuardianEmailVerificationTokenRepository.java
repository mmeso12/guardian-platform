package com.guardian.cloud.repository;

import com.guardian.cloud.entity.GuardianEmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuardianEmailVerificationTokenRepository
        extends JpaRepository<
        GuardianEmailVerificationToken,
        Long
        > {

    Optional<GuardianEmailVerificationToken>
    findByTokenHash(String tokenHash);

    List<GuardianEmailVerificationToken>
    findAllByGuardianUserIdAndUsedAtIsNull(
            Long guardianUserId
    );
}