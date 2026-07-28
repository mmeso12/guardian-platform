package com.guardian.cloud.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "device_commands",
        indexes = {
                @Index(
                        name = "idx_device_commands_device_created",
                        columnList = "device_id, created_at"
                ),
                @Index(
                        name = "idx_device_commands_pending",
                        columnList = "device_id, status, expires_at"
                ),
                @Index(
                        name = "idx_device_commands_expiration",
                        columnList = "status, expires_at"
                ),
                @Index(
                        name = "idx_device_commands_guardian",
                        columnList = "created_by_guardian_id, created_at"
                )
        }
)
public class DeviceCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "device_id",
            nullable = false
    )
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "created_by_guardian_id",
            nullable = false
    )
    private GuardianUser createdByGuardian;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "command_type",
            nullable = false,
            length = 50
    )
    private DeviceCommandType commandType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private DeviceCommandStatus status =
            DeviceCommandStatus.PENDING;

    @Column(name = "payload_json", columnDefinition = "TEXT")
    private String payloadJson;

    @Column(name = "result_json", columnDefinition = "TEXT")
    private String resultJson;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "received_at")
    private Instant receivedAt;

    @Column(name = "execution_started_at")
    private Instant executionStartedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @PrePersist
    private void prePersist() {
        Instant now = Instant.now();

        if (status == null) {
            status = DeviceCommandStatus.PENDING;
        }

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

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public GuardianUser getCreatedByGuardian() {
        return createdByGuardian;
    }

    public void setCreatedByGuardian(
            GuardianUser createdByGuardian
    ) {
        this.createdByGuardian = createdByGuardian;
    }

    public DeviceCommandType getCommandType() {
        return commandType;
    }

    public void setCommandType(
            DeviceCommandType commandType
    ) {
        this.commandType = commandType;
    }

    public DeviceCommandStatus getStatus() {
        return status;
    }

    public void setStatus(
            DeviceCommandStatus status
    ) {
        this.status = status;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    public String getResultJson() {
        return resultJson;
    }

    public void setResultJson(String resultJson) {
        this.resultJson = resultJson;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(
            String failureReason
    ) {
        this.failureReason = failureReason;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(
            Instant deliveredAt
    ) {
        this.deliveredAt = deliveredAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(
            Instant receivedAt
    ) {
        this.receivedAt = receivedAt;
    }

    public Instant getExecutionStartedAt() {
        return executionStartedAt;
    }

    public void setExecutionStartedAt(
            Instant executionStartedAt
    ) {
        this.executionStartedAt =
                executionStartedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(
            Instant completedAt
    ) {
        this.completedAt = completedAt;
    }

    public Instant getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(Instant failedAt) {
        this.failedAt = failedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(
            Instant cancelledAt
    ) {
        this.cancelledAt = cancelledAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(
            Instant expiresAt
    ) {
        this.expiresAt = expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }
}