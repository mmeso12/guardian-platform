package com.guardian.cloud.dto.auth;

import java.util.UUID;

public record AuthResponse(

        String accessToken,
        String refreshToken,

        String tokenType,
        long expiresIn,

        UUID sessionId,

        UserResponse user
) {
}