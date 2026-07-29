package com.guardian.cloud.service;

import com.guardian.cloud.dto.auth.ChangePasswordRequest;
import com.guardian.cloud.dto.auth.GuardianSessionResponse;
import com.guardian.cloud.dto.auth.UpdateGuardianProfileRequest;
import com.guardian.cloud.dto.auth.UserResponse;
import com.guardian.cloud.entity.GuardianSession;
import com.guardian.cloud.entity.GuardianUser;
import com.guardian.cloud.exception.CurrentPasswordIncorrectException;
import com.guardian.cloud.exception.GuardianUserNotFoundException;
import com.guardian.cloud.repository.GuardianUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class GuardianAccountService {

    private static final String PASSWORD_CHANGE_REASON =
            "Password changed";

    private static final String LOGOUT_ALL_REASON =
            "Guardian logged out from all sessions";

    private final GuardianUserRepository
            guardianUserRepository;

    private final PasswordEncoder passwordEncoder;

    private final GuardianSessionService
            guardianSessionService;

    public GuardianAccountService(
            GuardianUserRepository guardianUserRepository,
            PasswordEncoder passwordEncoder,
            GuardianSessionService guardianSessionService
    ) {
        this.guardianUserRepository =
                guardianUserRepository;

        this.passwordEncoder =
                passwordEncoder;

        this.guardianSessionService =
                guardianSessionService;
    }

    /**
     * Returns the profile of the authenticated
     * guardian.
     */
    @Transactional(readOnly = true)
    public UserResponse getProfile(
            Long guardianUserId
    ) {
        GuardianUser guardianUser =
                requireGuardianUser(
                        guardianUserId
                );

        return toUserResponse(guardianUser);
    }

    /**
     * Updates the guardian's editable profile
     * fields.
     */
    @Transactional
    public UserResponse updateProfile(
            Long guardianUserId,
            UpdateGuardianProfileRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Profile request is required"
            );
        }

        GuardianUser guardianUser =
                requireGuardianUser(
                        guardianUserId
                );

        guardianUser.setFirstName(
                normalizeRequired(
                        request.firstName(),
                        "First name"
                )
        );

        guardianUser.setLastName(
                normalizeRequired(
                        request.lastName(),
                        "Last name"
                )
        );

        guardianUser.setPhoneNumber(
                normalizeOptional(
                        request.phoneNumber()
                )
        );

        GuardianUser savedGuardian =
                guardianUserRepository.save(
                        guardianUser
                );

        return toUserResponse(savedGuardian);
    }

    /**
     * Changes the guardian's password after
     * confirming the current password.
     *
     * Every session is revoked after a successful
     * password change. The guardian must log in
     * again using the new password.
     */
    @Transactional
    public void changePassword(
            Long guardianUserId,
            ChangePasswordRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Password-change request is required"
            );
        }

        GuardianUser guardianUser =
                requireGuardianUser(
                        guardianUserId
                );

        if (
                !passwordEncoder.matches(
                        request.currentPassword(),
                        guardianUser.getPasswordHash()
                )
        ) {
            throw new CurrentPasswordIncorrectException();
        }

        validateNewPassword(
                request.newPassword()
        );

        /*
         * Prevent changing the password to exactly
         * the current password.
         */
        if (
                passwordEncoder.matches(
                        request.newPassword(),
                        guardianUser.getPasswordHash()
                )
        ) {
            throw new IllegalArgumentException(
                    "New password must be different from the current password"
            );
        }

        guardianUser.setPasswordHash(
                passwordEncoder.encode(
                        request.newPassword()
                )
        );

        guardianUser.setPasswordChangedAt(
                Instant.now()
        );

        /*
         * Invalidates every access token containing
         * the previous accountVersion.
         */
        guardianUser.incrementAccountVersion();

        guardianUserRepository.save(
                guardianUser
        );

        guardianSessionService.revokeAll(
                guardianUserId,
                PASSWORD_CHANGE_REASON
        );
    }

    /**
     * Lists all sessions belonging to the guardian.
     *
     * The current session is identified using the
     * sessionId claim from the access token.
     */
    @Transactional(readOnly = true)
    public List<GuardianSessionResponse> listSessions(
            Long guardianUserId,
            UUID currentSessionId
    ) {
        List<GuardianSession> sessions =
                guardianSessionService.getSessions(
                        guardianUserId
                );

        Instant now = Instant.now();

        return sessions.stream()
                .map(
                        session ->
                                toSessionResponse(
                                        session,
                                        currentSessionId,
                                        now
                                )
                )
                .toList();
    }

    /**
     * Revokes one session belonging to the
     * authenticated guardian.
     */
    @Transactional
    public void revokeSession(
            Long guardianUserId,
            UUID sessionId
    ) {
        if (sessionId == null) {
            throw new IllegalArgumentException(
                    "Session ID is required"
            );
        }

        guardianSessionService.revokeSession(
                guardianUserId,
                sessionId
        );
    }

    /**
     * Revokes all sessions and invalidates every
     * currently issued access token.
     */
    @Transactional
    public void logoutAll(
            Long guardianUserId
    ) {
        GuardianUser guardianUser =
                requireGuardianUser(
                        guardianUserId
                );

        guardianUser.incrementAccountVersion();

        guardianUserRepository.save(
                guardianUser
        );

        guardianSessionService.revokeAll(
                guardianUserId,
                LOGOUT_ALL_REASON
        );
    }

    private GuardianUser requireGuardianUser(
            Long guardianUserId
    ) {
        if (guardianUserId == null) {
            throw new GuardianUserNotFoundException(
                    "null ID"
            );
        }

        return guardianUserRepository
                .findById(guardianUserId)
                .orElseThrow(
                        () ->
                                new GuardianUserNotFoundException(
                                        "ID "
                                                + guardianUserId
                                )
                );
    }

    private UserResponse toUserResponse(
            GuardianUser guardianUser
    ) {
        return new UserResponse(
                guardianUser.getId(),
                guardianUser.getFirstName(),
                guardianUser.getLastName(),
                guardianUser.getEmail(),
                guardianUser.getPhoneNumber(),
                guardianUser.getRole(),
                guardianUser.isEmailVerified()
        );
    }

    private GuardianSessionResponse
    toSessionResponse(
            GuardianSession session,
            UUID currentSessionId,
            Instant now
    ) {
        boolean current =
                currentSessionId != null
                        && currentSessionId.equals(
                        session.getSessionId()
                );

        return new GuardianSessionResponse(
                session.getSessionId(),
                session.getDeviceName(),
                session.getPlatform(),
                session.getIpAddress(),
                session.getUserAgent(),
                current,
                session.isActive(now),
                session.getLastUsedAt(),
                session.getExpiresAt(),
                session.getRevokedAt(),
                session.getCreatedAt()
        );
    }

    private String normalizeRequired(
            String value,
            String fieldName
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }

        return value.trim();
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

    private void validateNewPassword(
            String newPassword
    ) {
        if (
                newPassword == null
                        || newPassword.length() < 8
                        || newPassword.length() > 128
        ) {
            throw new IllegalArgumentException(
                    "Password must be between 8 and 128 characters"
            );
        }
    }
}