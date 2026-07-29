package com.guardian.cloud.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(
        prefix = "guardian.account"
)
public class GuardianAccountProperties {

    private Duration refreshTokenExpiration =
            Duration.ofDays(30);

    private Duration emailVerificationExpiration =
            Duration.ofHours(24);

    private Duration passwordResetExpiration =
            Duration.ofMinutes(30);

    public Duration getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public void setRefreshTokenExpiration(
            Duration refreshTokenExpiration
    ) {
        this.refreshTokenExpiration =
                refreshTokenExpiration;
    }

    public Duration
    getEmailVerificationExpiration() {
        return emailVerificationExpiration;
    }

    public void setEmailVerificationExpiration(
            Duration expiration
    ) {
        this.emailVerificationExpiration =
                expiration;
    }

    public Duration getPasswordResetExpiration() {
        return passwordResetExpiration;
    }

    public void setPasswordResetExpiration(
            Duration passwordResetExpiration
    ) {
        this.passwordResetExpiration =
                passwordResetExpiration;
    }
}