package com.guardian.cloud.dto.auth;

import com.guardian.cloud.entity.UserRole;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        UserRole role,
        boolean emailVerified
) {
}