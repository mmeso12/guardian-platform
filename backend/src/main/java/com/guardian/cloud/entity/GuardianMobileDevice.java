package com.guardian.cloud.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "guardian_mobile_devices",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_guardian_mobile_device_push_token",
                        columnNames = "push_token"
                )
        },
        indexes = {
                @Index(
                        name = "idx_guardian_mobile_device_user",
                        columnList = "guardian_user_id"
                ),
                @Index(
                        name = "idx_guardian_mobile_device_user_enabled",
                        columnList = "guardian_user_id, enabled"
                )
        }
)
public class GuardianMobileDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "guardian_user_id",
            nullable = false
    )
    private GuardianUser guardianUser;

    @Column(
            name = "push_token",
            nullable = false,
            unique = true,
            length = 512
    )
    private String pushToken;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "platform",
            nullable = false,
            length = 20
    )
    private MobilePlatform platform;

    @Column(name = "device_name", length = 150)
    private String deviceName;

    @Column(name = "app_version", length = 50)
    private String appVersion;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    private void prePersist() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (lastSeenAt == null) {
            lastSeenAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public GuardianUser getGuardianUser() {
        return guardianUser;
    }

    public void setGuardianUser(
            GuardianUser guardianUser
    ) {
        this.guardianUser = guardianUser;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = normalize(pushToken);
    }

    public MobilePlatform getPlatform() {
        return platform;
    }

    public void setPlatform(
            MobilePlatform platform
    ) {
        this.platform = platform;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = normalizeOptional(deviceName);
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = normalizeOptional(appVersion);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}