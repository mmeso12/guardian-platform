package com.guardian.cloud.repository;

import com.guardian.cloud.entity.GeofenceState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface GeofenceStateRepository
        extends JpaRepository<GeofenceState, Long> {

    Optional<GeofenceState> findByGeofenceId(
            Long geofenceId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT state
            FROM GeofenceState state
            WHERE state.geofence.id = :geofenceId
            """)
    Optional<GeofenceState> findByGeofenceIdForUpdate(
            @Param("geofenceId") Long geofenceId
    );

    void deleteByGeofenceId(Long geofenceId);
}