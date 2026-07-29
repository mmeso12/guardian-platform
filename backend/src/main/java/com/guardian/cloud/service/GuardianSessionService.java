package com.guardian.cloud.service;

import com.guardian.cloud.config.GuardianAccountProperties;
import com.guardian.cloud.entity.GuardianSession;
import com.guardian.cloud.entity.GuardianUser;
import com.guardian.cloud.exception.InvalidRefreshTokenException;
import com.guardian.cloud.repository.GuardianSessionRepository;
import com.guardian.cloud.security.SecureTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class GuardianSessionService {

    private static final String LOGOUT_REASON =
            "Guardian logged out";

    private static final String SESSION_REVOKED_REASON =
            "Guardian revoked session";

    private static final String LOGOUT_ALL_REASON =
            "All guardian sessions revoked";

    private final GuardianSessionRepository
            guardianSessionRepository;

    private final SecureTokenService secureTokenService;

    private final GuardianAccountProperties
            guardianAccountProperties;

    public GuardianSessionService(
            GuardianSessionRepository guardianSessionRepository,
            SecureTokenService secureTokenService,
            GuardianAccountProperties guardianAccountProperties
    ) {
        this.guardianSessionRepository =
                guardianSessionRepository;

        this.secureTokenService =
                secureTokenService;

        this.guardianAccountProperties =
                guardianAccountProperties;
    }

    /**
     * Creates a new persistent session for a guardian.
     *
     * The raw refresh token is returned only once.
     * Only its SHA-256 hash is stored in the database.
     */
    @Transactional
    public CreatedGuardianSession createSession(
            GuardianUser guardianUser,
            String deviceName,
            String platform,
            String ipAddress,
            String userAgent
    ) {
        if (guardianUser == null) {
            throw new IllegalArgumentException(
                    "Guardian user is required"
            );
        }

        String rawRefreshToken =
                secureTokenService.generateToken();

        String refreshTokenHash =
                secureTokenService.hash(
                        rawRefreshToken
                );

        Instant now = Instant.now();

        GuardianSession session =
                new GuardianSession();

        session.setGuardianUser(
                guardianUser
        );

        session.setRefreshTokenHash(
                refreshTokenHash
        );

        session.setDeviceName(
                normalizeOptional(deviceName)
        );

        session.setPlatform(
                normalizeOptional(platform)
        );

        session.setIpAddress(
                normalizeOptional(ipAddress)
        );

        session.setUserAgent(
                truncate(
                        normalizeOptional(userAgent),
                        500
                )
        );

        session.setLastUsedAt(now);

        session.setExpiresAt(
                now.plus(
                        guardianAccountProperties
                                .getRefreshTokenExpiration()
                )
        );

        GuardianSession savedSession =
                guardianSessionRepository.save(
                        session
                );

        return new CreatedGuardianSession(
                savedSession,
                rawRefreshToken
        );
    }

    /**
     * Rotates a refresh token.
     *
     * The previous refresh token is replaced with a
     * newly generated token and becomes unusable.
     */
    @Transactional
    public CreatedGuardianSession rotate(
            String rawRefreshToken
    ) {
        GuardianSession session =
                requireActiveSession(
                        rawRefreshToken
                );

        String replacementToken =
                secureTokenService.generateToken();

        String replacementTokenHash =
                secureTokenService.hash(
                        replacementToken
                );

        Instant now = Instant.now();

        session.setRefreshTokenHash(
                replacementTokenHash
        );

        session.setLastUsedAt(now);

        /*
         * Sliding expiration:
         * every successful refresh gives the
         * session a new configured lifetime.
         */
        session.setExpiresAt(
                now.plus(
                        guardianAccountProperties
                                .getRefreshTokenExpiration()
                )
        );

        GuardianSession savedSession =
                guardianSessionRepository.save(
                        session
                );

        return new CreatedGuardianSession(
                savedSession,
                replacementToken
        );
    }

    /**
     * Revokes the session identified by a raw
     * refresh token.
     *
     * This operation is intentionally idempotent:
     * an unknown or already invalid token does not
     * expose whether a matching session existed.
     */
    @Transactional
    public void revoke(
            String rawRefreshToken,
            String reason
    ) {
        if (
                rawRefreshToken == null
                        || rawRefreshToken.isBlank()
        ) {
            return;
        }

        String tokenHash =
                secureTokenService.hash(
                        rawRefreshToken.trim()
                );

        guardianSessionRepository
                .findByRefreshTokenHash(tokenHash)
                .ifPresent(session -> {
                    if (session.getRevokedAt() == null) {
                        session.revoke(
                                normalizeReason(
                                        reason,
                                        LOGOUT_REASON
                                )
                        );

                        guardianSessionRepository.save(
                                session
                        );
                    }
                });
    }

    /**
     * Convenience overload for normal logout.
     */
    @Transactional
    public void revoke(
            String rawRefreshToken
    ) {
        revoke(
                rawRefreshToken,
                LOGOUT_REASON
        );
    }

    /**
     * Revokes a particular session belonging to
     * the authenticated guardian.
     *
     * The guardian ID is included in the repository
     * lookup so one guardian cannot revoke another
     * guardian's session.
     */
    @Transactional
    public void revokeSession(
            Long guardianUserId,
            UUID sessionId
    ) {
        if (
                guardianUserId == null
                        || sessionId == null
        ) {
            throw new InvalidRefreshTokenException();
        }

        GuardianSession session =
                guardianSessionRepository
                        .findBySessionIdAndGuardianUserId(
                                sessionId,
                                guardianUserId
                        )
                        .orElseThrow(
                                InvalidRefreshTokenException::new
                        );

        if (session.getRevokedAt() == null) {
            session.revoke(
                    SESSION_REVOKED_REASON
            );

            guardianSessionRepository.save(
                    session
            );
        }
    }

    /**
     * Revokes every active session belonging to a
     * guardian.
     */
    @Transactional
    public int revokeAll(
            Long guardianUserId
    ) {
        return revokeAll(
                guardianUserId,
                LOGOUT_ALL_REASON
        );
    }

    /**
     * Returns all sessions belonging to the
     * guardian, newest first.
     */
    @Transactional(readOnly = true)
    public List<GuardianSession> getSessions(
            Long guardianUserId
    ) {
        if (guardianUserId == null) {
            throw new IllegalArgumentException(
                    "Guardian user ID is required"
            );
        }

        return guardianSessionRepository
                .findAllByGuardianUserIdOrderByCreatedAtDesc(
                        guardianUserId
                );
    }

    /**
     * Finds and validates a session using its raw
     * refresh token.
     */
    @Transactional(readOnly = true)
    public GuardianSession requireActiveSession(
            String rawRefreshToken
    ) {
        if (
                rawRefreshToken == null
                        || rawRefreshToken.isBlank()
        ) {
            throw new InvalidRefreshTokenException();
        }

        String tokenHash =
                secureTokenService.hash(
                        rawRefreshToken.trim()
                );

        GuardianSession session =
                guardianSessionRepository
                        .findByRefreshTokenHash(
                                tokenHash
                        )
                        .orElseThrow(
                                InvalidRefreshTokenException::new
                        );

        if (!session.isActive(Instant.now())) {
            throw new InvalidRefreshTokenException();
        }

        GuardianUser guardianUser =
                session.getGuardianUser();

        if (
                guardianUser == null
                        || !guardianUser.isEnabled()
        ) {
            throw new InvalidRefreshTokenException();
        }

        return session;
    }

    /**
     * Checks whether a particular session belonging
     * to a guardian is currently active.
     */
    @Transactional(readOnly = true)
    public boolean isSessionActive(
            Long guardianUserId,
            UUID sessionId
    ) {
        if (
                guardianUserId == null
                        || sessionId == null
        ) {
            return false;
        }

        return guardianSessionRepository
                .findBySessionIdAndGuardianUserId(
                        sessionId,
                        guardianUserId
                )
                .map(
                        session -> session.isActive(
                                Instant.now()
                        )
                )
                .orElse(false);
    }

    @Transactional
    public int revokeAll(
            Long guardianUserId,
            String reason
    ) {
        if (guardianUserId == null) {
            throw new IllegalArgumentException(
                    "Guardian user ID is required"
            );
        }

        String finalReason =
                normalizeReason(
                        reason,
                        LOGOUT_ALL_REASON
                );

        return guardianSessionRepository
                .revokeAllForGuardian(
                        guardianUserId,
                        Instant.now(),
                        finalReason
                );
    }

    private String normalizeOptional(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    private String normalizeReason(
            String suppliedReason,
            String defaultReason
    ) {
        String normalized =
                normalizeOptional(
                        suppliedReason
                );

        String selectedReason =
                normalized == null
                        ? defaultReason
                        : normalized;

        return truncate(
                selectedReason,
                255
        );
    }

    private String truncate(
            String value,
            int maximumLength
    ) {
        if (
                value == null
                        || value.length()
                        <= maximumLength
        ) {
            return value;
        }

        return value.substring(
                0,
                maximumLength
        );
    }
}