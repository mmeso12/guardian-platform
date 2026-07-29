package com.guardian.cloud.service;

public record ClientContext(
        String ipAddress,
        String userAgent
) {
}