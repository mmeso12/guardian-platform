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
    findAllByGuardianUserIdAndVisibleInAppTrueOrderByCreatedAtDesc(
            Long guardianUserId
    );

    long countByGuardianUserIdAndVisibleInAppTrueAndReadAtIsNull(
            Long guardianUserId
    );

    Optional<GuardianNotification>
    findByIdAndGuardianUserIdAndVisibleInAppTrue(
            Long notificationId,
            Long guardianUserId
    );

    boolean existsByGuardianUserIdAndGuardianAlertId(
            Long guardianUserId,
            Long guardianAlertId
    );

    @Modifying
    @Query("""
            UPDATE GuardianNotification notification
               SET notification.readAt = :readAt
             WHERE notification.guardianUser.id = :guardianUserId
               AND notification.visibleInApp = true
               AND notification.readAt IS NULL
            """)
    int markAllVisibleUnreadAsRead(
            @Param("guardianUserId")
            Long guardianUserId,

            @Param("readAt")
            Instant readAt
    );
}