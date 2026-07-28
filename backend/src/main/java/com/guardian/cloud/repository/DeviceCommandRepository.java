package com.guardian.cloud.repository;

import com.guardian.cloud.entity.DeviceCommand;
import com.guardian.cloud.entity.DeviceCommandStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DeviceCommandRepository
        extends JpaRepository<DeviceCommand, Long> {

    List<DeviceCommand>
    findAllByDeviceIdOrderByCreatedAtDesc(
            Long deviceId
    );

    Optional<DeviceCommand>
    findByIdAndDeviceId(
            Long commandId,
            Long deviceId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT command
            FROM DeviceCommand command
            WHERE command.id = :commandId
            """)
    Optional<DeviceCommand> findByIdForUpdate(
            @Param("commandId")
            Long commandId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT command
            FROM DeviceCommand command
            WHERE command.device.id = :deviceId
              AND command.status = :status
              AND (
                    command.expiresAt IS NULL
                    OR command.expiresAt > :now
              )
            ORDER BY command.createdAt ASC
            """)
    List<DeviceCommand> findAvailableForDevice(
            @Param("deviceId")
            Long deviceId,

            @Param("status")
            DeviceCommandStatus status,

            @Param("now")
            Instant now
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT command
            FROM DeviceCommand command
            WHERE command.status IN :statuses
              AND command.expiresAt IS NOT NULL
              AND command.expiresAt <= :now
            ORDER BY command.expiresAt ASC
            """)
    List<DeviceCommand> findExpiredForUpdate(
            @Param("statuses")
            Collection<DeviceCommandStatus> statuses,

            @Param("now")
            Instant now
    );

    boolean existsByDeviceIdAndCommandTypeAndStatusIn(
            Long deviceId,
            com.guardian.cloud.entity.DeviceCommandType commandType,
            Collection<DeviceCommandStatus> statuses
    );
}