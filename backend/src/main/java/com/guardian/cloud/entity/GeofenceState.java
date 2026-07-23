package com.guardian.cloud.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "geofence_states",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_geofence_states_geofence",
                        columnNames = "geofence_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_geofence_states_last_evaluated_at",
                        columnList = "last_evaluated_at"
                )
        }
)
public class GeofenceState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "geofence_id",
            nullable = false,
            unique = true
    )
    private Geofence geofence;

    @Column(name = "inside", nullable = false)
    private boolean inside;

    @Column(
            name = "last_distance_meters",
            nullable = false
    )
    private Double lastDistanceMeters;

    @Column(
            name = "last_evaluated_at",
            nullable = false
    )
    private Instant lastEvaluatedAt;

    @Column(name = "entered_at")
    private Instant enteredAt;

    @Column(name = "exited_at")
    private Instant exitedAt;

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

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Geofence getGeofence() {
        return geofence;
    }

    public void setGeofence(Geofence geofence) {
        this.geofence = geofence;
    }

    public boolean isInside() {
        return inside;
    }

    public void setInside(boolean inside) {
        this.inside = inside;
    }

    public Double getLastDistanceMeters() {
        return lastDistanceMeters;
    }

    public void setLastDistanceMeters(
            Double lastDistanceMeters
    ) {
        this.lastDistanceMeters = lastDistanceMeters;
    }

    public Instant getLastEvaluatedAt() {
        return lastEvaluatedAt;
    }

    public void setLastEvaluatedAt(
            Instant lastEvaluatedAt
    ) {
        this.lastEvaluatedAt = lastEvaluatedAt;
    }

    public Instant getEnteredAt() {
        return enteredAt;
    }

    public void setEnteredAt(Instant enteredAt) {
        this.enteredAt = enteredAt;
    }

    public Instant getExitedAt() {
        return exitedAt;
    }

    public void setExitedAt(Instant exitedAt) {
        this.exitedAt = exitedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}