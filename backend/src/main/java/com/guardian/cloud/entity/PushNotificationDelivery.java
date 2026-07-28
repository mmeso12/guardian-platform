package com.guardian.cloud.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "push_notification_deliveries",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_push_delivery_notification_device",
                        columnNames = {
                                "guardian_notification_id",
                                "guardian_mobile_device_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_push_delivery_notification",
                        columnList = "guardian_notification_id"
                ),
                @Index(
                        name = "idx_push_delivery_mobile_device",
                        columnList = "guardian_mobile_device_id"
                ),
                @Index(
                        name = "idx_push_delivery_retry",
                        columnList = "status, next_retry_at"
                )
        }
)
public class PushNotificationDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "guardian_notification_id",
            nullable = false
    )
    private GuardianNotification guardianNotification;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "guardian_mobile_device_id",
            nullable = false
    )
    private GuardianMobileDevice guardianMobileDevice;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private PushDeliveryStatus status;

    @Column(
            name = "provider",
            nullable = false,
            length = 50
    )
    private String provider;

    @Column(
            name = "attempt_count",
            nullable = false
    )
    private Integer attemptCount = 0;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(
            name = "provider_message_id",
            length = 255
    )
    private String providerMessageId;

    @Column(
            name = "failure_reason",
            length = 1000
    )
    private String failureReason;

    @Column(name = "sent_at")
    private Instant sentAt;

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

        if (status == null) {
            status = PushDeliveryStatus.PENDING;
        }

        if (attemptCount == null) {
            attemptCount = 0;
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

    public GuardianNotification getGuardianNotification() {
        return guardianNotification;
    }

    public void setGuardianNotification(
            GuardianNotification guardianNotification
    ) {
        this.guardianNotification =
                guardianNotification;
    }

    public GuardianMobileDevice getGuardianMobileDevice() {
        return guardianMobileDevice;
    }

    public void setGuardianMobileDevice(
            GuardianMobileDevice guardianMobileDevice
    ) {
        this.guardianMobileDevice =
                guardianMobileDevice;
    }

    public PushDeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(
            PushDeliveryStatus status
    ) {
        this.status = status;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Integer getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(
            Integer attemptCount
    ) {
        this.attemptCount = attemptCount;
    }

    public Instant getNextRetryAt() {
        return nextRetryAt;
    }

    public void setNextRetryAt(
            Instant nextRetryAt
    ) {
        this.nextRetryAt = nextRetryAt;
    }

    public String getProviderMessageId() {
        return providerMessageId;
    }

    public void setProviderMessageId(
            String providerMessageId
    ) {
        this.providerMessageId =
                providerMessageId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(
            String failureReason
    ) {
        this.failureReason = failureReason;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}