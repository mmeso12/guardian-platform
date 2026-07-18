package com.guardian.cloud.security;

public record AuthenticatedDevice(
        Long id,
        String deviceUid
) {
}