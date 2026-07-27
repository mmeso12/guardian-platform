package com.guardian.cloud.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "emergency_escalations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_emergency_escalation_alert_guardian",
                        columnNames = {
                                "guardian_alert_id",
                                "guardian_user_id"
                        }
                )
        }
)
public class EmergencyEscalation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "guardian_alert_id",
            nullable = false
    )
    private GuardianAlert guardianAlert;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "guardian_user_id",
            nullable = false
    )
    private GuardianUser guardianUser;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private EmergencyEscalationStatus status;

    @Column(name = "current_priority")
    private Integer currentPriority;

    @Column(
            name = "current_attempt_number",
            nullable = false
    )
    private Integer currentAttemptNumber = 0;

    @Column(name = "next_action_at")
    private Instant nextActionAt;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(
            name = "acknowledgement_note",
            length = 1000
    )
    private String acknowledgementNote;

    @Column(
            name = "resolution_note",
            length = 1000
    )
    private String resolutionNote;

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
            status = EmergencyEscalationStatus.IN_PROGRESS;
        }

        if (startedAt == null) {
            startedAt = now;
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

    public GuardianAlert getGuardianAlert() {
        return guardianAlert;
    }

    public void setGuardianAlert(
            GuardianAlert guardianAlert
    ) {
        this.guardianAlert = guardianAlert;
    }

    public GuardianUser getGuardianUser() {
        return guardianUser;
    }

    public void setGuardianUser(
            GuardianUser guardianUser
    ) {
        this.guardianUser = guardianUser;
    }

    public EmergencyEscalationStatus getStatus() {
        return status;
    }

    public void setStatus(
            EmergencyEscalationStatus status
    ) {
        this.status = status;
    }

    public Integer getCurrentPriority() {
        return currentPriority;
    }

    public void setCurrentPriority(
            Integer currentPriority
    ) {
        this.currentPriority = currentPriority;
    }

    public Integer getCurrentAttemptNumber() {
        return currentAttemptNumber;
    }

    public void setCurrentAttemptNumber(
            Integer currentAttemptNumber
    ) {
        this.currentAttemptNumber =
                currentAttemptNumber;
    }

    public Instant getNextActionAt() {
        return nextActionAt;
    }

    public void setNextActionAt(
            Instant nextActionAt
    ) {
        this.nextActionAt = nextActionAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(
            Instant acknowledgedAt
    ) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getAcknowledgementNote() {
        return acknowledgementNote;
    }

    public void setAcknowledgementNote(
            String acknowledgementNote
    ) {
        this.acknowledgementNote =
                acknowledgementNote;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public void setResolutionNote(
            String resolutionNote
    ) {
        this.resolutionNote = resolutionNote;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}