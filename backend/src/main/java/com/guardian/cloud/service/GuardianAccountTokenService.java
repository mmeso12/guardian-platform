package com.guardian.cloud.service;

import com.guardian.cloud.config.GuardianAccountProperties;
import com.guardian.cloud.entity.GuardianEmailVerificationToken;
import com.guardian.cloud.entity.GuardianPasswordResetToken;
import com.guardian.cloud.entity.GuardianUser;
import com.guardian.cloud.exception.InvalidAccountTokenException;
import com.guardian.cloud.repository.GuardianEmailVerificationTokenRepository;
import com.guardian.cloud.repository.GuardianPasswordResetTokenRepository;
import com.guardian.cloud.repository.GuardianUserRepository;
import com.guardian.cloud.security.SecureTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class GuardianAccountTokenService {

    private static final String PASSWORD_RESET_SESSION_REASON =
            "Password reset";

    private final GuardianUserRepository guardianUserRepository;

    private final GuardianEmailVerificationTokenRepository
            emailVerificationTokenRepository;

    private final GuardianPasswordResetTokenRepository
            passwordResetTokenRepository;

    private final SecureTokenService secureTokenService;

    private final PasswordEncoder passwordEncoder;

    private final GuardianSessionService guardianSessionService;

    private final GuardianAccountProperties
            guardianAccountProperties;

    private final GuardianAccountMessageService
            guardianAccountMessageService;

    public GuardianAccountTokenService(
            GuardianUserRepository guardianUserRepository,
            GuardianEmailVerificationTokenRepository
                    emailVerificationTokenRepository,
            GuardianPasswordResetTokenRepository
                    passwordResetTokenRepository,
            SecureTokenService secureTokenService,
            PasswordEncoder passwordEncoder,
            GuardianSessionService guardianSessionService,
            GuardianAccountProperties guardianAccountProperties,
            GuardianAccountMessageService guardianAccountMessageService
    ) {
        this.guardianUserRepository =
                guardianUserRepository;

        this.emailVerificationTokenRepository =
                emailVerificationTokenRepository;

        this.passwordResetTokenRepository =
                passwordResetTokenRepository;

        this.secureTokenService =
                secureTokenService;

        this.passwordEncoder =
                passwordEncoder;

        this.guardianSessionService =
                guardianSessionService;

        this.guardianAccountProperties =
                guardianAccountProperties;

        this.guardianAccountMessageService =
                guardianAccountMessageService;
    }

    /**
     * Creates a new email-verification token.
     *
     * Any previous unused verification tokens for
     * the guardian are invalidated first.
     *
     * Only the token hash is stored. The raw token
     * is passed to the message-delivery service.
     */
    @Transactional
    public void issueEmailVerification(
            GuardianUser guardianUser
    ) {
        if (
                guardianUser == null
                        || guardianUser.getId() == null
        ) {
            throw new IllegalArgumentException(
                    "Persisted guardian user is required"
            );
        }

        if (guardianUser.isEmailVerified()) {
            return;
        }

        invalidateUnusedEmailVerificationTokens(
                guardianUser.getId()
        );

        String rawToken =
                secureTokenService.generateToken();

        GuardianEmailVerificationToken token =
                new GuardianEmailVerificationToken();

        token.setGuardianUser(guardianUser);

        token.setTokenHash(
                secureTokenService.hash(rawToken)
        );

        token.setExpiresAt(
                Instant.now().plus(
                        guardianAccountProperties
                                .getEmailVerificationExpiration()
                )
        );

        emailVerificationTokenRepository.save(token);

        guardianAccountMessageService
                .sendEmailVerification(
                        guardianUser.getEmail(),
                        rawToken
                );
    }

    /**
     * Confirms an email-verification token.
     *
     * A token may be used only once and must not
     * be expired.
     */
    @Transactional
    public void verifyEmail(
            String rawToken
    ) {
        GuardianEmailVerificationToken token =
                requireUsableEmailVerificationToken(
                        rawToken
                );

        GuardianUser guardianUser =
                token.getGuardianUser();

        if (
                guardianUser == null
                        || guardianUser.getId() == null
        ) {
            throw new InvalidAccountTokenException();
        }

        guardianUser.setEmailVerified(true);

        token.setUsedAt(Instant.now());

        guardianUserRepository.save(guardianUser);
        emailVerificationTokenRepository.save(token);

        /*
         * Invalidates any other unused verification
         * tokens belonging to this guardian.
         */
        invalidateUnusedEmailVerificationTokensExcept(
                guardianUser.getId(),
                token.getId()
        );
    }

    /**
     * Resends email verification for an
     * authenticated guardian.
     */
    @Transactional
    public void resendEmailVerification(
            Long guardianUserId
    ) {
        GuardianUser guardianUser =
                guardianUserRepository
                        .findById(guardianUserId)
                        .orElseThrow(
                                InvalidAccountTokenException::new
                        );

        issueEmailVerification(guardianUser);
    }

    /**
     * Starts the password-reset workflow.
     *
     * The method deliberately does nothing when
     * the email does not exist. This allows the
     * controller to return the same public response
     * for registered and unregistered addresses.
     */
    @Transactional
    public void issuePasswordReset(
            String email
    ) {
        String normalizedEmail =
                normalizeEmail(email);

        if (normalizedEmail == null) {
            return;
        }

        GuardianUser guardianUser =
                guardianUserRepository
                        .findByEmailIgnoreCase(
                                normalizedEmail
                        )
                        .orElse(null);

        /*
         * Do not reveal that the account does not
         * exist or is disabled.
         */
        if (
                guardianUser == null
                        || !guardianUser.isEnabled()
        ) {
            return;
        }

        invalidateUnusedPasswordResetTokens(
                guardianUser.getId()
        );

        String rawToken =
                secureTokenService.generateToken();

        GuardianPasswordResetToken token =
                new GuardianPasswordResetToken();

        token.setGuardianUser(guardianUser);

        token.setTokenHash(
                secureTokenService.hash(rawToken)
        );

        token.setExpiresAt(
                Instant.now().plus(
                        guardianAccountProperties
                                .getPasswordResetExpiration()
                )
        );

        passwordResetTokenRepository.save(token);

        guardianAccountMessageService
                .sendPasswordReset(
                        guardianUser.getEmail(),
                        rawToken
                );
    }

    /**
     * Resets the guardian password and invalidates
     * every existing login session.
     */
    @Transactional
    public void resetPassword(
            String rawToken,
            String newPassword
    ) {
        validateNewPassword(newPassword);

        GuardianPasswordResetToken token =
                requireUsablePasswordResetToken(
                        rawToken
                );

        GuardianUser guardianUser =
                token.getGuardianUser();

        if (
                guardianUser == null
                        || guardianUser.getId() == null
                        || !guardianUser.isEnabled()
        ) {
            throw new InvalidAccountTokenException();
        }

        Instant now = Instant.now();

        guardianUser.setPasswordHash(
                passwordEncoder.encode(
                        newPassword
                )
        );

        guardianUser.setPasswordChangedAt(now);

        /*
         * Invalidates access tokens carrying the
         * previous accountVersion claim.
         */
        guardianUser.incrementAccountVersion();

        token.setUsedAt(now);

        guardianUserRepository.save(guardianUser);
        passwordResetTokenRepository.save(token);

        invalidateUnusedPasswordResetTokensExcept(
                guardianUser.getId(),
                token.getId()
        );

        guardianSessionService.revokeAll(
                guardianUser.getId(),
                PASSWORD_RESET_SESSION_REASON
        );
    }

    private GuardianEmailVerificationToken
    requireUsableEmailVerificationToken(
            String rawToken
    ) {
        String tokenHash =
                hashRequiredToken(rawToken);

        GuardianEmailVerificationToken token =
                emailVerificationTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(
                                InvalidAccountTokenException::new
                        );

        if (!token.isUsable(Instant.now())) {
            throw new InvalidAccountTokenException();
        }

        return token;
    }

    private GuardianPasswordResetToken
    requireUsablePasswordResetToken(
            String rawToken
    ) {
        String tokenHash =
                hashRequiredToken(rawToken);

        GuardianPasswordResetToken token =
                passwordResetTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(
                                InvalidAccountTokenException::new
                        );

        if (!token.isUsable(Instant.now())) {
            throw new InvalidAccountTokenException();
        }

        return token;
    }

    private void invalidateUnusedEmailVerificationTokens(
            Long guardianUserId
    ) {
        List<GuardianEmailVerificationToken> tokens =
                emailVerificationTokenRepository
                        .findAllByGuardianUserIdAndUsedAtIsNull(
                                guardianUserId
                        );

        if (tokens.isEmpty()) {
            return;
        }

        Instant now = Instant.now();

        for (
                GuardianEmailVerificationToken token
                : tokens
        ) {
            token.setUsedAt(now);
        }

        emailVerificationTokenRepository.saveAll(tokens);
    }

    private void
    invalidateUnusedEmailVerificationTokensExcept(
            Long guardianUserId,
            Long retainedTokenId
    ) {
        List<GuardianEmailVerificationToken> tokens =
                emailVerificationTokenRepository
                        .findAllByGuardianUserIdAndUsedAtIsNull(
                                guardianUserId
                        );

        Instant now = Instant.now();
        boolean changed = false;

        for (
                GuardianEmailVerificationToken token
                : tokens
        ) {
            if (
                    token.getId() != null
                            && token.getId()
                            .equals(retainedTokenId)
            ) {
                continue;
            }

            token.setUsedAt(now);
            changed = true;
        }

        if (changed) {
            emailVerificationTokenRepository
                    .saveAll(tokens);
        }
    }

    private void invalidateUnusedPasswordResetTokens(
            Long guardianUserId
    ) {
        List<GuardianPasswordResetToken> tokens =
                passwordResetTokenRepository
                        .findAllByGuardianUserIdAndUsedAtIsNull(
                                guardianUserId
                        );

        if (tokens.isEmpty()) {
            return;
        }

        Instant now = Instant.now();

        for (
                GuardianPasswordResetToken token
                : tokens
        ) {
            token.setUsedAt(now);
        }

        passwordResetTokenRepository.saveAll(tokens);
    }

    private void
    invalidateUnusedPasswordResetTokensExcept(
            Long guardianUserId,
            Long retainedTokenId
    ) {
        List<GuardianPasswordResetToken> tokens =
                passwordResetTokenRepository
                        .findAllByGuardianUserIdAndUsedAtIsNull(
                                guardianUserId
                        );

        Instant now = Instant.now();
        boolean changed = false;

        for (
                GuardianPasswordResetToken token
                : tokens
        ) {
            if (
                    token.getId() != null
                            && token.getId()
                            .equals(retainedTokenId)
            ) {
                continue;
            }

            token.setUsedAt(now);
            changed = true;
        }

        if (changed) {
            passwordResetTokenRepository
                    .saveAll(tokens);
        }
    }

    private String hashRequiredToken(
            String rawToken
    ) {
        if (
                rawToken == null
                        || rawToken.isBlank()
        ) {
            throw new InvalidAccountTokenException();
        }

        return secureTokenService.hash(
                rawToken.trim()
        );
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

    private String normalizeEmail(
            String email
    ) {
        if (email == null) {
            return null;
        }

        String normalized =
                email.trim()
                        .toLowerCase(Locale.ROOT);

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}