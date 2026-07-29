package com.guardian.cloud.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "guardian_sessions",
        indexes = {
                @Index(
                        name = "idx_guardian_sessions_user",
                        columnList = "guardian_user_id, created_at"
                ),
                @Index(
                        name = "idx_guardian_sessions_active",
                        columnList =
                                "guardian_user_id, revoked_at, expires_at"
                )
        }
)
public class GuardianSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "session_id",
            nullable = false,
            unique = true
    )
    private UUID sessionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "guardian_user_id",
            nullable = false
    )
    private GuardianUser guardianUser;

    @Column(
            name = "refresh_token_hash",
            nullable = false,
            unique = true,
            length = 64
    )
    private String refreshTokenHash;

    @Column(name = "device_name", length = 150)
    private String deviceName;

    @Column(name = "platform", length = 50)
    private String platform;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revocation_reason", length = 255)
    private String revocationReason;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void beforeInsert() {
        Instant now = Instant.now();

        if (sessionId == null) {
            sessionId = UUID.randomUUID();
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void beforeUpdate() {
        updatedAt = Instant.now();
    }

    public boolean isActive(Instant now) {
        return revokedAt == null
                && expiresAt != null
                && expiresAt.isAfter(now);
    }

    public void revoke(String reason) {
        if (revokedAt == null) {
            revokedAt = Instant.now();
            revocationReason = reason;
        }
    }

    public Long getId() {
        return id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public GuardianUser getGuardianUser() {
        return guardianUser;
    }

    public void setGuardianUser(
            GuardianUser guardianUser
    ) {
        this.guardianUser = guardianUser;
    }

    public String getRefreshTokenHash() {
        return refreshTokenHash;
    }

    public void setRefreshTokenHash(
            String refreshTokenHash
    ) {
        this.refreshTokenHash = refreshTokenHash;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(Instant lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public String getRevocationReason() {
        return revocationReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}