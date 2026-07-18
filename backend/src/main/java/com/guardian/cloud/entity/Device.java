package com.guardian.cloud.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "devices",
        indexes = {
                @Index(name = "idx_devices_device_uid", columnList = "device_uid")
        }
)
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_uid", nullable = false, unique = true, length = 100)
    private String deviceUid;

    @Column(name = "display_name", length = 150)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DeviceStatus status = DeviceStatus.UNPAIRED;

    @Column(name = "battery_level")
    private Integer batteryLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "motion_state", nullable = false, length = 30)
    private MotionState motionState = MotionState.UNKNOWN;

    @Column(name = "last_sequence_number")
    private Long lastSequenceNumber;

    @Column(name = "firmware_version", length = 50)
    private String firmwareVersion;

    @Column(name = "device_key_hash", length = 64)
    private String deviceKeyHash;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "paired", nullable = false)
    private boolean paired = false;

    @Column(name = "paired_at")
    private Instant pairedAt;

    @Column(name = "pairing_code_hash", length = 64)
    private String pairingCodeHash;

    @PrePersist
    void beforeInsert() {
        Instant now = Instant.now();

        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = DeviceStatus.UNPAIRED;
        }

        if (motionState == null) {
            motionState = MotionState.UNKNOWN;
        }
    }

    @PreUpdate
    void beforeUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getDeviceUid() {
        return deviceUid;
    }

    public void setDeviceUid(String deviceUid) {
        this.deviceUid = deviceUid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public MotionState getMotionState() {
        return motionState;
    }

    public void setMotionState(MotionState motionState) {
        this.motionState = motionState;
    }

    public Long getLastSequenceNumber() {
        return lastSequenceNumber;
    }

    public void setLastSequenceNumber(Long lastSequenceNumber) {
        this.lastSequenceNumber = lastSequenceNumber;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getDeviceKeyHash() {
        return deviceKeyHash;
    }

    public void setDeviceKeyHash(String deviceKeyHash) {
        this.deviceKeyHash = deviceKeyHash;
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

    public boolean isPaired() {
        return paired;
    }

    public void setPaired(boolean paired) {
        this.paired = paired;
    }

    public Instant getPairedAt() {
        return pairedAt;
    }

    public void setPairedAt(Instant pairedAt) {
        this.pairedAt = pairedAt;
    }

    public String getPairingCodeHash() {
        return pairingCodeHash;
    }

    public void setPairingCodeHash(String pairingCodeHash) {
        this.pairingCodeHash = pairingCodeHash;
    }
}