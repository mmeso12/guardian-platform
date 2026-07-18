package com.guardian.cloud.security;

import com.guardian.cloud.entity.UserRole;

public record AuthenticatedGuardian(
        Long id,
        String email,
        UserRole role
) {
}