package com.guardian.cloud.repository;

import com.guardian.cloud.entity.GuardianNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface GuardianNotificationRepository
        extends JpaRepository<GuardianNotification, Long> {

    List<GuardianNotification>
    findAllByGuardianUserIdOrderByCreatedAtDesc(
            Long guardianUserId
    );

    Optional<GuardianNotification>
    findByIdAndGuardianUserId(
            Long notificationId,
            Long guardianUserId
    );

    boolean existsByGuardianUserIdAndGuardianAlertId(
            Long guardianUserId,
            Long guardianAlertId
    );

    long countByGuardianUserIdAndReadAtIsNull(
            Long guardianUserId
    );

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE GuardianNotification notification
            SET notification.readAt = :readAt,
                notification.updatedAt = :readAt
            WHERE notification.guardianUser.id = :guardianUserId
              AND notification.readAt IS NULL
            """)
    int markAllUnreadAsRead(
            @Param("guardianUserId")
            Long guardianUserId,

            @Param("readAt")
            Instant readAt
    );
}