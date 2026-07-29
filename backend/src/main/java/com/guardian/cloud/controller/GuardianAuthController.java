package com.guardian.cloud.controller;

import com.guardian.cloud.dto.auth.AuthResponse;
import com.guardian.cloud.dto.auth.ForgotPasswordRequest;
import com.guardian.cloud.dto.auth.LoginRequest;
import com.guardian.cloud.dto.auth.LogoutRequest;
import com.guardian.cloud.dto.auth.MessageResponse;
import com.guardian.cloud.dto.auth.RefreshTokenRequest;
import com.guardian.cloud.dto.auth.RegisterRequest;
import com.guardian.cloud.dto.auth.ResetPasswordRequest;
import com.guardian.cloud.dto.auth.UserResponse;
import com.guardian.cloud.dto.auth.VerifyEmailRequest;
import com.guardian.cloud.security.AuthenticatedGuardian;
import com.guardian.cloud.service.ClientContext;
import com.guardian.cloud.service.GuardianAccountTokenService;
import com.guardian.cloud.service.GuardianAuthService;
import com.guardian.cloud.service.GuardianSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class GuardianAuthController {

    private final GuardianAuthService
            guardianAuthService;

    private final GuardianSessionService
            guardianSessionService;

    private final GuardianAccountTokenService
            guardianAccountTokenService;

    public GuardianAuthController(
            GuardianAuthService guardianAuthService,
            GuardianSessionService guardianSessionService,
            GuardianAccountTokenService guardianAccountTokenService
    ) {
        this.guardianAuthService =
                guardianAuthService;

        this.guardianSessionService =
                guardianSessionService;

        this.guardianAccountTokenService =
                guardianAccountTokenService;
    }

    /*
     * Public registration endpoint.
     *
     * Creates:
     * - the guardian account;
     * - an email-verification token;
     * - a guardian session;
     * - an access token;
     * - a refresh token.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid
            @RequestBody
            RegisterRequest request,

            HttpServletRequest httpRequest
    ) {
        AuthResponse response =
                guardianAuthService.register(
                        request,
                        clientContext(httpRequest)
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /*
     * Public login endpoint.
     *
     * Creates a new persistent guardian session
     * and returns both access and refresh tokens.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid
            @RequestBody
            LoginRequest request,

            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(
                guardianAuthService.login(
                        request,
                        clientContext(httpRequest)
                )
        );
    }

    /*
     * Rotates the refresh token and issues a
     * new access token.
     *
     * The previous refresh token becomes invalid.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid
            @RequestBody
            RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(
                guardianAuthService.refresh(request)
        );
    }

    /*
     * Revokes the session associated with the
     * supplied refresh token.
     *
     * This endpoint is intentionally idempotent.
     * Logging out using an already-revoked token
     * still returns a successful response.
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @Valid
            @RequestBody
            LogoutRequest request
    ) {
        guardianSessionService.revoke(
                request.refreshToken(),
                "Guardian logged out"
        );

        return ResponseEntity.ok(
                new MessageResponse(
                        "Logout successful"
                )
        );
    }

    /*
     * Verifies the guardian's email address using
     * the raw token delivered to the guardian.
     */
    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse>
    verifyEmail(
            @Valid
            @RequestBody
            VerifyEmailRequest request
    ) {
        guardianAccountTokenService.verifyEmail(
                request.token()
        );

        return ResponseEntity.ok(
                new MessageResponse(
                        "Email address verified successfully"
                )
        );
    }

    /*
     * Starts the password-reset workflow.
     *
     * Always returns the same response, regardless
     * of whether the supplied email exists. This
     * prevents account enumeration.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse>
    forgotPassword(
            @Valid
            @RequestBody
            ForgotPasswordRequest request
    ) {
        guardianAccountTokenService
                .issuePasswordReset(
                        request.email()
                );

        return ResponseEntity.ok(
                new MessageResponse(
                        "If an account exists for that email, "
                                + "password-reset instructions were sent."
                )
        );
    }

    /*
     * Resets the guardian's password.
     *
     * The token can only be used once. A successful
     * reset also revokes all existing sessions and
     * invalidates previously issued access tokens.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse>
    resetPassword(
            @Valid
            @RequestBody
            ResetPasswordRequest request
    ) {
        guardianAccountTokenService.resetPassword(
                request.token(),
                request.newPassword()
        );

        return ResponseEntity.ok(
                new MessageResponse(
                        "Password reset successfully. "
                                + "Please log in with your new password."
                )
        );
    }

    /*
     * Returns the currently authenticated guardian.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                authenticatedGuardian(
                        authentication
                );

        return ResponseEntity.ok(
                guardianAuthService
                        .getCurrentUser(
                                guardian.id()
                        )
        );
    }

    private AuthenticatedGuardian
    authenticatedGuardian(
            Authentication authentication
    ) {
        return (AuthenticatedGuardian)
                authentication.getPrincipal();
    }

    /*
     * Captures information used to identify the
     * guardian session.
     *
     * X-Forwarded-For is checked first because the
     * backend may run behind a reverse proxy or
     * load balancer.
     */
    private ClientContext clientContext(
            HttpServletRequest request
    ) {
        String ipAddress =
                extractClientIpAddress(request);

        String userAgent =
                normalizeOptional(
                        request.getHeader(
                                "User-Agent"
                        )
                );

        return new ClientContext(
                ipAddress,
                userAgent
        );
    }

    private String extractClientIpAddress(
            HttpServletRequest request
    ) {
        String forwardedFor =
                request.getHeader(
                        "X-Forwarded-For"
                );

        if (
                forwardedFor != null
                        && !forwardedFor.isBlank()
        ) {
            /*
             * X-Forwarded-For may contain multiple
             * addresses. The first value represents
             * the original client.
             */
            String firstAddress =
                    forwardedFor
                            .split(",")[0]
                            .trim();

            if (!firstAddress.isEmpty()) {
                return firstAddress;
            }
        }

        String realIp =
                request.getHeader(
                        "X-Real-IP"
                );

        if (
                realIp != null
                        && !realIp.isBlank()
        ) {
            return realIp.trim();
        }

        return normalizeOptional(
                request.getRemoteAddr()
        );
    }

    private String normalizeOptional(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}