package com.guardian.cloud.controller;

import com.guardian.cloud.dto.auth.ChangePasswordRequest;
import com.guardian.cloud.dto.auth.GuardianSessionResponse;
import com.guardian.cloud.dto.auth.MessageResponse;
import com.guardian.cloud.dto.auth.UpdateGuardianProfileRequest;
import com.guardian.cloud.dto.auth.UserResponse;
import com.guardian.cloud.security.AuthenticatedGuardian;
import com.guardian.cloud.security.JwtService;
import com.guardian.cloud.service.GuardianAccountService;
import com.guardian.cloud.service.GuardianAccountTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/account")
public class GuardianAccountController {

    private final GuardianAccountService guardianAccountService;
    private final GuardianAccountTokenService guardianAccountTokenService;
    private final JwtService jwtService;

    public GuardianAccountController(
            GuardianAccountService guardianAccountService,
            GuardianAccountTokenService guardianAccountTokenService,
            JwtService jwtService
    ) {
        this.guardianAccountService = guardianAccountService;
        this.guardianAccountTokenService = guardianAccountTokenService;
        this.jwtService = jwtService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                requireAuthenticatedGuardian(authentication);

        return ResponseEntity.ok(
                guardianAccountService.getProfile(
                        guardian.id()
                )
        );
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody
            UpdateGuardianProfileRequest request
    ) {
        AuthenticatedGuardian guardian =
                requireAuthenticatedGuardian(authentication);

        return ResponseEntity.ok(
                guardianAccountService.updateProfile(
                        guardian.id(),
                        request
                )
        );
    }

    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
            Authentication authentication,
            @Valid @RequestBody
            ChangePasswordRequest request
    ) {
        AuthenticatedGuardian guardian =
                requireAuthenticatedGuardian(authentication);

        guardianAccountService.changePassword(
                guardian.id(),
                request
        );

        return ResponseEntity.ok(
                new MessageResponse(
                        "Password changed successfully. Please log in again."
                )
        );
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                requireAuthenticatedGuardian(authentication);

        guardianAccountTokenService.resendEmailVerification(
                guardian.id()
        );

        return ResponseEntity.ok(
                new MessageResponse(
                        "Verification email sent if verification is still required."
                )
        );
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<GuardianSessionResponse>> getSessions(
            Authentication authentication,
            HttpServletRequest request
    ) {
        AuthenticatedGuardian guardian =
                requireAuthenticatedGuardian(authentication);

        String accessToken =
                extractBearerToken(request);

        UUID currentSessionId =
                jwtService.extractSessionId(
                        accessToken
                );

        List<GuardianSessionResponse> sessions =
                guardianAccountService.listSessions(
                        guardian.id(),
                        currentSessionId
                );

        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<MessageResponse> revokeSession(
            Authentication authentication,
            @PathVariable UUID sessionId
    ) {
        AuthenticatedGuardian guardian =
                requireAuthenticatedGuardian(authentication);

        guardianAccountService.revokeSession(
                guardian.id(),
                sessionId
        );

        return ResponseEntity.ok(
                new MessageResponse(
                        "Session revoked successfully."
                )
        );
    }

    @PostMapping("/logout-all")
    public ResponseEntity<MessageResponse> logoutAll(
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                requireAuthenticatedGuardian(authentication);

        guardianAccountService.logoutAll(
                guardian.id()
        );

        return ResponseEntity.ok(
                new MessageResponse(
                        "All sessions logged out successfully."
                )
        );
    }

    private AuthenticatedGuardian requireAuthenticatedGuardian(
            Authentication authentication
    ) {
        if (
                authentication == null
                        || !authentication.isAuthenticated()
                        || !(authentication.getPrincipal()
                        instanceof AuthenticatedGuardian guardian)
        ) {
            throw new IllegalStateException(
                    "Authenticated guardian is required"
            );
        }

        return guardian;
    }

    private String extractBearerToken(
            HttpServletRequest request
    ) {
        String authorizationHeader =
                request.getHeader("Authorization");

        if (
                authorizationHeader == null
                        || !authorizationHeader.startsWith("Bearer ")
        ) {
            throw new IllegalArgumentException(
                    "Bearer access token is required"
            );
        }

        String accessToken =
                authorizationHeader
                        .substring(7)
                        .trim();

        if (accessToken.isEmpty()) {
            throw new IllegalArgumentException(
                    "Bearer access token is required"
            );
        }

        return accessToken;
    }
}