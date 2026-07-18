package com.guardian.cloud.service;

import com.guardian.cloud.dto.auth.AuthResponse;
import com.guardian.cloud.dto.auth.LoginRequest;
import com.guardian.cloud.dto.auth.RegisterRequest;
import com.guardian.cloud.dto.auth.UserResponse;
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

@Service
public class GuardianAuthService {

    private final GuardianUserRepository guardianUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public GuardianAuthService(
            GuardianUserRepository guardianUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.guardianUserRepository = guardianUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail =
                request.email().trim().toLowerCase();

        if (
                guardianUserRepository
                        .existsByEmailIgnoreCase(normalizedEmail)
        ) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        GuardianUser user = new GuardianUser();

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(normalizedEmail);
        user.setPhoneNumber(request.phoneNumber());
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        user.setRole(UserRole.PARENT);
        user.setEnabled(true);
        user.setEmailVerified(false);

        GuardianUser savedUser =
                guardianUserRepository.save(user);

        return createAuthResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        GuardianUser user = guardianUserRepository
                .findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(InvalidCredentialsException::new);

        if (
                !user.isEnabled()
                || !passwordEncoder.matches(
                        request.password(),
                        user.getPasswordHash()
                )
        ) {
            throw new InvalidCredentialsException();
        }

        return createAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        GuardianUser user = guardianUserRepository
                .findById(userId)
                .orElseThrow(
                        () -> new GuardianUserNotFoundException(
                                "ID " + userId
                        )
                );

        return toUserResponse(user);
    }

    private AuthResponse createAuthResponse(GuardianUser user) {
        return new AuthResponse(
                jwtService.generateToken(user),
                "Bearer",
                jwtService.getExpirationSeconds(),
                toUserResponse(user)
        );
    }

    private UserResponse toUserResponse(GuardianUser user) {
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
}