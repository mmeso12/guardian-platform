package com.guardian.cloud.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "guardian_notifications",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_guardian_notification_user_alert",
                        columnNames = {
                                "guardian_user_id",
                                "guardian_alert_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_guardian_notification_user",
                        columnList = "guardian_user_id"
                ),
                @Index(
                        name = "idx_guardian_notification_alert",
                        columnList = "guardian_alert_id"
                ),
                @Index(
                        name = "idx_guardian_notification_user_read",
                        columnList = "guardian_user_id, read_at"
                ),
                @Index(
                        name = "idx_guardian_notification_created_at",
                        columnList = "created_at"
                )
        }
)
public class GuardianNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "guardian_user_id",
            nullable = false
    )
    private GuardianUser guardianUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "guardian_alert_id",
            nullable = false
    )
    private GuardianAlert guardianAlert;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
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

    public GuardianAlert getGuardianAlert() {
        return guardianAlert;
    }

    public void setGuardianAlert(
            GuardianAlert guardianAlert
    ) {
        this.guardianAlert = guardianAlert;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void setReadAt(Instant readAt) {
        this.readAt = readAt;
    }

    public boolean isRead() {
        return readAt != null;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}