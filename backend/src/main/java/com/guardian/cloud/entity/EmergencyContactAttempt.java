package com.guardian.cloud.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "emergency_contact_attempts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_contact_attempt_number",
                        columnNames = {
                                "emergency_escalation_id",
                                "attempt_number"
                        }
                )
        }
)
public class EmergencyContactAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "emergency_escalation_id",
            nullable = false
    )
    private EmergencyEscalation emergencyEscalation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "emergency_contact_id",
            nullable = false
    )
    private EmergencyContact emergencyContact;

    @Column(
            name = "attempt_number",
            nullable = false
    )
    private Integer attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "contact_method",
            nullable = false,
            length = 30
    )
    private PreferredContactMethod contactMethod;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private EmergencyContactAttemptStatus status;

    @Column(
            name = "contact_name",
            nullable = false,
            length = 150
    )
    private String contactName;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(name = "email", length = 255)
    private String email;

    @Column(
            name = "attempted_at",
            nullable = false
    )
    private Instant attemptedAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

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
            status = EmergencyContactAttemptStatus.PENDING;
        }

        if (attemptedAt == null) {
            attemptedAt = now;
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public EmergencyEscalation getEmergencyEscalation() {
        return emergencyEscalation;
    }

    public void setEmergencyEscalation(
            EmergencyEscalation emergencyEscalation
    ) {
        this.emergencyEscalation =
                emergencyEscalation;
    }

    public EmergencyContact getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(
            EmergencyContact emergencyContact
    ) {
        this.emergencyContact = emergencyContact;
    }

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(
            Integer attemptNumber
    ) {
        this.attemptNumber = attemptNumber;
    }

    public PreferredContactMethod getContactMethod() {
        return contactMethod;
    }

    public void setContactMethod(
            PreferredContactMethod contactMethod
    ) {
        this.contactMethod = contactMethod;
    }

    public EmergencyContactAttemptStatus getStatus() {
        return status;
    }

    public void setStatus(
            EmergencyContactAttemptStatus status
    ) {
        this.status = status;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getAttemptedAt() {
        return attemptedAt;
    }

    public void setAttemptedAt(Instant attemptedAt) {
        this.attemptedAt = attemptedAt;
    }

    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(
            Instant acknowledgedAt
    ) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(
            String failureReason
    ) {
        this.failureReason = failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}