package com.guardian.cloud.controller;

import com.guardian.cloud.dto.auth.AuthResponse;
import com.guardian.cloud.dto.auth.LoginRequest;
import com.guardian.cloud.dto.auth.RegisterRequest;
import com.guardian.cloud.dto.auth.UserResponse;
import com.guardian.cloud.security.AuthenticatedGuardian;
import com.guardian.cloud.service.GuardianAuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class GuardianAuthController {

    private final GuardianAuthService guardianAuthService;

    public GuardianAuthController(
            GuardianAuthService guardianAuthService
    ) {
        this.guardianAuthService = guardianAuthService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(guardianAuthService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                guardianAuthService.login(request)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(
            Authentication authentication
    ) {
        AuthenticatedGuardian guardian =
                (AuthenticatedGuardian) authentication.getPrincipal();

        return ResponseEntity.ok(
                guardianAuthService.getCurrentUser(guardian.id())
        );
    }
}