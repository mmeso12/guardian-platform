package com.guardian.cloud.repository;

import com.guardian.cloud.entity.PushDeliveryStatus;
import com.guardian.cloud.entity.PushNotificationDelivery;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface PushNotificationDeliveryRepository
        extends JpaRepository<
                PushNotificationDelivery,
                Long
                > {

    boolean existsByGuardianNotificationIdAndGuardianMobileDeviceId(
            Long guardianNotificationId,
            Long guardianMobileDeviceId
    );

    List<PushNotificationDelivery>
    findAllByGuardianMobileDeviceGuardianUserIdOrderByCreatedAtDesc(
            Long guardianUserId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT delivery
            FROM PushNotificationDelivery delivery
            WHERE delivery.status = :status
              AND delivery.nextRetryAt IS NOT NULL
              AND delivery.nextRetryAt <= :now
            ORDER BY delivery.nextRetryAt ASC
            """)
    List<PushNotificationDelivery>
    findDueDeliveriesForUpdate(
            @Param("status")
            PushDeliveryStatus status,

            @Param("now")
            Instant now
    );
}