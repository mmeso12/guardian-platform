package com.guardian.cloud.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "location_records",
        indexes = {
                @Index(
                        name = "idx_location_records_device_recorded_at",
                        columnList = "device_id, recorded_at"
                )
        }
)
public class LocationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "sequence_number", nullable = false)
    private Long sequenceNumber;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "accuracy_meters")
    private Double accuracyMeters;

    @Column(name = "speed_meters_per_second")
    private Double speedMetersPerSecond;

    @Column(name = "heading_degrees")
    private Double headingDegrees;

    @Column(name = "battery_level")
    private Integer batteryLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "motion_state", nullable = false, length = 30)
    private MotionState motionState = MotionState.UNKNOWN;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    @PrePersist
    void beforeInsert() {
        if (receivedAt == null) {
            receivedAt = Instant.now();
        }

        if (motionState == null) {
            motionState = MotionState.UNKNOWN;
        }
    }

    public Long getId() {
        return id;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getAccuracyMeters() {
        return accuracyMeters;
    }

    public void setAccuracyMeters(Double accuracyMeters) {
        this.accuracyMeters = accuracyMeters;
    }

    public Double getSpeedMetersPerSecond() {
        return speedMetersPerSecond;
    }

    public void setSpeedMetersPerSecond(Double speedMetersPerSecond) {
        this.speedMetersPerSecond = speedMetersPerSecond;
    }

    public Double getHeadingDegrees() {
        return headingDegrees;
    }

    public void setHeadingDegrees(Double headingDegrees) {
        this.headingDegrees = headingDegrees;
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

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }
}