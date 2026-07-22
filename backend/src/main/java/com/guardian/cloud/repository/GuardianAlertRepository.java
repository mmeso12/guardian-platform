package com.guardian.cloud.repository;

import com.guardian.cloud.entity.AlertStatus;
import com.guardian.cloud.entity.GuardianAlert;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GuardianAlertRepository
        extends JpaRepository<GuardianAlert, Long> {

    boolean existsByDeviceEventId(Long deviceEventId);

    List<GuardianAlert> findAllByDeviceIdOrderByCreatedAtDesc(
            Long deviceId
    );

    List<GuardianAlert>
    findAllByDeviceIdAndStatusOrderByCreatedAtDesc(
            Long deviceId,
            AlertStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT alert
            FROM GuardianAlert alert
            WHERE alert.id = :alertId
            """)
    Optional<GuardianAlert> findByIdForUpdate(
            @Param("alertId") Long alertId
    );
}