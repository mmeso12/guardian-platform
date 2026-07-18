package com.guardian.cloud.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "guardian_device_access",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_guardian_device_access_user_device",
                        columnNames = {"user_id", "device_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_guardian_device_access_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_guardian_device_access_device",
                        columnList = "device_id"
                )
        }
)
public class GuardianDeviceAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private GuardianUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "access_role",
            nullable = false,
            length = 30
    )
    private DeviceAccessRole accessRole;

    @Column(name = "can_view_location", nullable = false)
    private boolean canViewLocation = true;

    @Column(name = "can_manage_alerts", nullable = false)
    private boolean canManageAlerts = true;

    @Column(name = "can_manage_device", nullable = false)
    private boolean canManageDevice = false;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @PrePersist
    void beforeInsert() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public GuardianUser getUser() {
        return user;
    }

    public void setUser(GuardianUser user) {
        this.user = user;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public DeviceAccessRole getAccessRole() {
        return accessRole;
    }

    public void setAccessRole(DeviceAccessRole accessRole) {
        this.accessRole = accessRole;
    }

    public boolean isCanViewLocation() {
        return canViewLocation;
    }

    public void setCanViewLocation(boolean canViewLocation) {
        this.canViewLocation = canViewLocation;
    }

    public boolean isCanManageAlerts() {
        return canManageAlerts;
    }

    public void setCanManageAlerts(boolean canManageAlerts) {
        this.canManageAlerts = canManageAlerts;
    }

    public boolean isCanManageDevice() {
        return canManageDevice;
    }

    public void setCanManageDevice(boolean canManageDevice) {
        this.canManageDevice = canManageDevice;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}