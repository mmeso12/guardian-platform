package com.guardian.cloud.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "emergency_contacts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_emergency_contacts_guardian_priority",
                        columnNames = {
                                "guardian_user_id",
                                "priority"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_emergency_contacts_guardian",
                        columnList = "guardian_user_id"
                ),
                @Index(
                        name = "idx_emergency_contacts_guardian_enabled_priority",
                        columnList = "guardian_user_id, enabled, priority"
                )
        }
)
public class EmergencyContact {

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
            name = "full_name",
            nullable = false,
            length = 150
    )
    private String fullName;

    @Column(
            name = "phone_number",
            length = 30
    )
    private String phoneNumber;

    @Column(
            name = "email",
            length = 255
    )
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "relationship",
            nullable = false,
            length = 40
    )
    private EmergencyContactRelationship relationship;

    @Column(
            name = "priority",
            nullable = false
    )
    private Integer priority;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "preferred_contact_method",
            nullable = false,
            length = 30
    )
    private PreferredContactMethod preferredContactMethod;

    @Column(
            name = "enabled",
            nullable = false
    )
    private boolean enabled = true;

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

    public GuardianUser getGuardianUser() {
        return guardianUser;
    }

    public void setGuardianUser(
            GuardianUser guardianUser
    ) {
        this.guardianUser = guardianUser;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName =
                fullName == null
                        ? null
                        : fullName.trim();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(
            String phoneNumber
    ) {
        this.phoneNumber =
                normalizeOptional(phoneNumber);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        String normalized =
                normalizeOptional(email);

        this.email =
                normalized == null
                        ? null
                        : normalized.toLowerCase();
    }

    public EmergencyContactRelationship
    getRelationship() {
        return relationship;
    }

    public void setRelationship(
            EmergencyContactRelationship relationship
    ) {
        this.relationship = relationship;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public PreferredContactMethod
    getPreferredContactMethod() {
        return preferredContactMethod;
    }

    public void setPreferredContactMethod(
            PreferredContactMethod preferredContactMethod
    ) {
        this.preferredContactMethod =
                preferredContactMethod;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private String normalizeOptional(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}