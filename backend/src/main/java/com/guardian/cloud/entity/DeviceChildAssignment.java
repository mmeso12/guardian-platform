package com.guardian.cloud.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "device_child_assignments",
        indexes = {
                @Index(
                        name = "idx_device_child_assignments_device",
                        columnList = "device_id"
                ),
                @Index(
                        name = "idx_device_child_assignments_child",
                        columnList = "child_profile_id"
                ),
                @Index(
                        name = "idx_device_child_assignments_active",
                        columnList = "active"
                )
        }
)
public class DeviceChildAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "assignment_id",
            nullable = false,
            unique = true,
            updatable = false
    )
    private UUID assignmentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "device_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_device_child_assignment_device"
            )
    )
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "child_profile_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_device_child_assignment_child"
            )
    )
    private ChildProfile childProfile;

    @Column(
            name = "assigned_at",
            nullable = false,
            updatable = false
    )
    private Instant assignedAt;

    @Column(name = "unassigned_at")
    private Instant unassignedAt;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public DeviceChildAssignment() {
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        if (assignmentId == null) {
            assignmentId = UUID.randomUUID();
        }

        if (assignedAt == null) {
            assignedAt = now;
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void unassign() {
        this.active = false;
        this.unassignedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public UUID getAssignmentId() {
        return assignmentId;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public ChildProfile getChildProfile() {
        return childProfile;
    }

    public void setChildProfile(
            ChildProfile childProfile
    ) {
        this.childProfile = childProfile;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public Instant getUnassignedAt() {
        return unassignedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}