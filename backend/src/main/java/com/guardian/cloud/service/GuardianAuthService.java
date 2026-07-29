package com.guardian.cloud.service;

import com.guardian.cloud.dto.auth.AuthResponse;
import com.guardian.cloud.dto.auth.LoginRequest;
import com.guardian.cloud.dto.auth.RefreshTokenRequest;
import com.guardian.cloud.dto.auth.RegisterRequest;
import com.guardian.cloud.dto.auth.UserResponse;
import com.guardian.cloud.entity.GuardianSession;
import com.guardian.cloud.entity.GuardianUser;
import com.guardian.cloud.entity.UserRole;
import com.guardian.cloud.exception.EmailAlreadyExistsException;
import com.guardian.cloud.exception.GuardianUserNotFoundException;
import com.guardian.cloud.exception.InvalidCredentialsException;
import com.guardian.cloud.repository.GuardianUserRepository;
import com.guardian.cloud.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class GuardianAuthService {

    private final GuardianUserRepository
            guardianUserRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final GuardianSessionService
            guardianSessionService;

    private final GuardianAccountTokenService
            guardianAccountTokenService;

    public GuardianAuthService(
            GuardianUserRepository guardianUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            GuardianSessionService guardianSessionService,
            GuardianAccountTokenService
                    guardianAccountTokenService
    ) {
        this.guardianUserRepository =
                guardianUserRepository;

        this.passwordEncoder =
                passwordEncoder;

        this.jwtService =
                jwtService;

        this.guardianSessionService =
                guardianSessionService;

        this.guardianAccountTokenService =
                guardianAccountTokenService;
    }

    @Transactional
    public AuthResponse register(
            RegisterRequest request,
            ClientContext clientContext
    ) {
        String normalizedEmail =
                request.email()
                        .trim()
                        .toLowerCase();

        if (
                guardianUserRepository
                        .existsByEmailIgnoreCase(
                                normalizedEmail
                        )
        ) {
            throw new EmailAlreadyExistsException(
                    normalizedEmail
            );
        }

        GuardianUser user =
                new GuardianUser();

        user.setFirstName(
                request.firstName()
        );

        user.setLastName(
                request.lastName()
        );

        user.setEmail(normalizedEmail);

        user.setPhoneNumber(
                request.phoneNumber()
        );

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.password()
                )
        );

        user.setRole(UserRole.PARENT);
        user.setEnabled(true);
        user.setEmailVerified(false);

        /*
         * Initial account version for newly
         * issued access tokens.
         */
        user.setAccountVersion(0L);

        GuardianUser savedUser =
                guardianUserRepository.save(user);

        /*
         * Creates and logs/sends the initial
         * email-verification token.
         */
        guardianAccountTokenService
                .issueEmailVerification(
                        savedUser
                );

        return createAuthResponse(
                savedUser,
                request.deviceName(),
                request.platform(),
                clientContext
        );
    }

    /*
     * This must not be readOnly because login:
     *
     * 1. updates lastLoginAt;
     * 2. saves the guardian;
     * 3. creates a GuardianSession.
     */
    @Transactional
    public AuthResponse login(
            LoginRequest request,
            ClientContext clientContext
    ) {
        String normalizedEmail =
                request.email()
                        .trim()
                        .toLowerCase();

        GuardianUser user =
                guardianUserRepository
                        .findByEmailIgnoreCase(
                                normalizedEmail
                        )
                        .orElseThrow(
                                InvalidCredentialsException::new
                        );

        if (
                !user.isEnabled()
                        || !passwordEncoder.matches(
                        request.password(),
                        user.getPasswordHash()
                )
        ) {
            throw new InvalidCredentialsException();
        }

        user.setLastLoginAt(
                Instant.now()
        );

        GuardianUser savedUser =
                guardianUserRepository.save(user);

        return createAuthResponse(
                savedUser,
                request.deviceName(),
                request.platform(),
                clientContext
        );
    }

    @Transactional
    public AuthResponse refresh(
            RefreshTokenRequest request
    ) {
        CreatedGuardianSession rotated =
                guardianSessionService.rotate(
                        request.refreshToken()
                );

        GuardianSession session =
                rotated.session();

        GuardianUser user =
                session.getGuardianUser();

        if (!user.isEnabled()) {
            session.revoke(
                    "Guardian account is disabled"
            );

            throw new InvalidCredentialsException();
        }

        String accessToken =
                jwtService.generateToken(
                        user,
                        session.getSessionId()
                );

        return new AuthResponse(
                accessToken,
                rotated.rawRefreshToken(),
                "Bearer",
                jwtService.getExpirationSeconds(),
                session.getSessionId(),
                toUserResponse(user)
        );
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(
            Long userId
    ) {
        GuardianUser user =
                guardianUserRepository
                        .findById(userId)
                        .orElseThrow(
                                () ->
                                        new GuardianUserNotFoundException(
                                                "ID "
                                                        + userId
                                        )
                        );

        return toUserResponse(user);
    }

    private AuthResponse createAuthResponse(
            GuardianUser user,
            String deviceName,
            String platform,
            ClientContext clientContext
    ) {
        ClientContext safeContext =
                clientContext == null
                        ? new ClientContext(
                        null,
                        null
                )
                        : clientContext;

        CreatedGuardianSession createdSession =
                guardianSessionService
                        .createSession(
                                user,
                                normalizeOptional(
                                        deviceName
                                ),
                                normalizeOptional(
                                        platform
                                ),
                                normalizeOptional(
                                        safeContext
                                                .ipAddress()
                                ),
                                normalizeOptional(
                                        safeContext
                                                .userAgent()
                                )
                        );

        String accessToken =
                jwtService.generateToken(
                        user,
                        createdSession
                                .session()
                                .getSessionId()
                );

        return new AuthResponse(
                accessToken,
                createdSession
                        .rawRefreshToken(),
                "Bearer",
                jwtService
                        .getExpirationSeconds(),
                createdSession
                        .session()
                        .getSessionId(),
                toUserResponse(user)
        );
    }

    private UserResponse toUserResponse(
            GuardianUser user
    ) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.isEmailVerified()
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
}