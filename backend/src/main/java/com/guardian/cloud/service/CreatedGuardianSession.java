package com.guardian.cloud.service;

import com.guardian.cloud.entity.GuardianSession;

public record CreatedGuardianSession(
        GuardianSession session,
        String rawRefreshToken
) {
}