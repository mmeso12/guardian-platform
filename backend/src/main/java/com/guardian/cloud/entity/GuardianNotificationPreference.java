package com.guardian.cloud.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "guardian_notification_preferences",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_notification_preferences_guardian",
                        columnNames = "guardian_user_id"
                )
        }
)
public class GuardianNotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "guardian_user_id",
            nullable = false
    )
    private GuardianUser guardianUser;

    @Column(name = "in_app_enabled", nullable = false)
    private boolean inAppEnabled = true;

    @Column(name = "push_enabled", nullable = false)
    private boolean pushEnabled = true;

    @Column(
            name = "informational_enabled",
            nullable = false
    )
    private boolean informationalEnabled = true;

    @Column(
            name = "warning_enabled",
            nullable = false
    )
    private boolean warningEnabled = true;

    @Column(name = "sos_enabled", nullable = false)
    private boolean sosEnabled = true;

    @Column(name = "tamper_enabled", nullable = false)
    private boolean tamperEnabled = true;

    @Column(
            name = "low_battery_enabled",
            nullable = false
    )
    private boolean lowBatteryEnabled = true;

    @Column(
            name = "device_online_enabled",
            nullable = false
    )
    private boolean deviceOnlineEnabled = false;

    @Column(
            name = "device_offline_enabled",
            nullable = false
    )
    private boolean deviceOfflineEnabled = true;

    @Column(
            name = "geofence_entry_enabled",
            nullable = false
    )
    private boolean geofenceEntryEnabled = true;

    @Column(
            name = "geofence_exit_enabled",
            nullable = false
    )
    private boolean geofenceExitEnabled = true;

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

    public boolean isInAppEnabled() {
        return inAppEnabled;
    }

    public void setInAppEnabled(
            boolean inAppEnabled
    ) {
        this.inAppEnabled = inAppEnabled;
    }

    public boolean isPushEnabled() {
        return pushEnabled;
    }

    public void setPushEnabled(boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }

    public boolean isInformationalEnabled() {
        return informationalEnabled;
    }

    public void setInformationalEnabled(
            boolean informationalEnabled
    ) {
        this.informationalEnabled =
                informationalEnabled;
    }

    public boolean isWarningEnabled() {
        return warningEnabled;
    }

    public void setWarningEnabled(
            boolean warningEnabled
    ) {
        this.warningEnabled = warningEnabled;
    }

    public boolean isSosEnabled() {
        return sosEnabled;
    }

    public void setSosEnabled(boolean sosEnabled) {
        this.sosEnabled = sosEnabled;
    }

    public boolean isTamperEnabled() {
        return tamperEnabled;
    }

    public void setTamperEnabled(
            boolean tamperEnabled
    ) {
        this.tamperEnabled = tamperEnabled;
    }

    public boolean isLowBatteryEnabled() {
        return lowBatteryEnabled;
    }

    public void setLowBatteryEnabled(
            boolean lowBatteryEnabled
    ) {
        this.lowBatteryEnabled =
                lowBatteryEnabled;
    }

    public boolean isDeviceOnlineEnabled() {
        return deviceOnlineEnabled;
    }

    public void setDeviceOnlineEnabled(
            boolean deviceOnlineEnabled
    ) {
        this.deviceOnlineEnabled =
                deviceOnlineEnabled;
    }

    public boolean isDeviceOfflineEnabled() {
        return deviceOfflineEnabled;
    }

    public void setDeviceOfflineEnabled(
            boolean deviceOfflineEnabled
    ) {
        this.deviceOfflineEnabled =
                deviceOfflineEnabled;
    }

    public boolean isGeofenceEntryEnabled() {
        return geofenceEntryEnabled;
    }

    public void setGeofenceEntryEnabled(
            boolean geofenceEntryEnabled
    ) {
        this.geofenceEntryEnabled =
                geofenceEntryEnabled;
    }

    public boolean isGeofenceExitEnabled() {
        return geofenceExitEnabled;
    }

    public void setGeofenceExitEnabled(
            boolean geofenceExitEnabled
    ) {
        this.geofenceExitEnabled =
                geofenceExitEnabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}