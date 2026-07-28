package com.guardian.cloud.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guardian.cloud.config.DeviceCommandProperties;
import com.guardian.cloud.dto.command.*;
import com.guardian.cloud.entity.*;
import com.guardian.cloud.exception.DeviceAccessDeniedException;
import com.guardian.cloud.exception.DeviceCommandNotFoundException;
import com.guardian.cloud.exception.InvalidDeviceCommandPayloadException;
import com.guardian.cloud.exception.InvalidDeviceCommandStateException;
import com.guardian.cloud.repository.DeviceCommandRepository;
import com.guardian.cloud.repository.GuardianDeviceAccessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DeviceCommandService {

    private static final Set<DeviceCommandStatus>
            ACTIVE_STATUSES =
            EnumSet.of(
                    DeviceCommandStatus.PENDING,
                    DeviceCommandStatus.DELIVERED,
                    DeviceCommandStatus.RECEIVED,
                    DeviceCommandStatus.EXECUTING
            );

    private final DeviceCommandRepository
            commandRepository;

    private final GuardianDeviceAccessRepository
            accessRepository;

    private final DeviceCommandProperties properties;

    private final ObjectMapper objectMapper;

    public DeviceCommandService(
            DeviceCommandRepository commandRepository,
            GuardianDeviceAccessRepository accessRepository,
            DeviceCommandProperties properties,
            ObjectMapper objectMapper
    ) {
        this.commandRepository = commandRepository;
        this.accessRepository = accessRepository;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /*
     * GUARDIAN OPERATIONS
     */

    @Transactional
    public DeviceCommandResponse createCommand(
            Long guardianUserId,
            Long deviceId,
            CreateDeviceCommandRequest request
    ) {
        GuardianDeviceAccess access =
                requireManagementAccess(
                        guardianUserId,
                        deviceId
                );

        validatePayload(
                request.commandType(),
                request.payload()
        );

        preventConflictingCommand(
                deviceId,
                request.commandType()
        );

        DeviceCommand command =
                new DeviceCommand();

        command.setDevice(access.getDevice());
        command.setCreatedByGuardian(
                access.getUser()
        );
        command.setCommandType(
                request.commandType()
        );
        command.setStatus(
                DeviceCommandStatus.PENDING
        );
        command.setPayloadJson(
                writeJson(request.payload())
        );

        Duration expiration =
                request.expiresInSeconds() == null
                        ? properties.getDefaultExpiration()
                        : Duration.ofSeconds(
                                request.expiresInSeconds()
                        );

        command.setExpiresAt(
                Instant.now().plus(expiration)
        );

        return toResponse(
                commandRepository.save(command)
        );
    }

    @Transactional(readOnly = true)
    public List<DeviceCommandResponse>
    getGuardianCommands(
            Long guardianUserId,
            Long deviceId
    ) {
        requireDeviceAccess(
                guardianUserId,
                deviceId
        );

        return commandRepository
                .findAllByDeviceIdOrderByCreatedAtDesc(
                        deviceId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DeviceCommandResponse getGuardianCommand(
            Long guardianUserId,
            Long deviceId,
            Long commandId
    ) {
        requireDeviceAccess(
                guardianUserId,
                deviceId
        );

        DeviceCommand command =
                commandRepository
                        .findByIdAndDeviceId(
                                commandId,
                                deviceId
                        )
                        .orElseThrow(
                                () ->
                                        new DeviceCommandNotFoundException(
                                                commandId
                                        )
                        );

        return toResponse(command);
    }

    @Transactional
    public DeviceCommandResponse cancelCommand(
            Long guardianUserId,
            Long deviceId,
            Long commandId
    ) {
        requireManagementAccess(
                guardianUserId,
                deviceId
        );

        DeviceCommand command =
                findForDeviceAndLock(
                        commandId,
                        deviceId
                );

        if (
                command.getStatus()
                        != DeviceCommandStatus.PENDING
                        && command.getStatus()
                        != DeviceCommandStatus.DELIVERED
        ) {
            throw invalidState(
                    command,
                    "only PENDING or DELIVERED commands can be cancelled"
            );
        }

        command.setStatus(
                DeviceCommandStatus.CANCELLED
        );
        command.setCancelledAt(Instant.now());

        return toResponse(
                commandRepository.save(command)
        );
    }

    /*
     * DEVICE OPERATIONS
     */

    @Transactional
    public List<DeviceCommandResponse>
    fetchPendingCommands(Long deviceId) {
        Instant now = Instant.now();

        List<DeviceCommand> pendingCommands =
                commandRepository
                        .findAvailableForDevice(
                                deviceId,
                                DeviceCommandStatus.PENDING,
                                now
                        );

        int maximum =
                Math.min(
                        pendingCommands.size(),
                        properties.getMaximumPendingFetch()
                );

        List<DeviceCommand> commandsToDeliver =
                pendingCommands
                        .subList(0, maximum);

        for (DeviceCommand command :
                commandsToDeliver) {
            command.setStatus(
                    DeviceCommandStatus.DELIVERED
            );
            command.setDeliveredAt(now);
        }

        return commandRepository
                .saveAll(commandsToDeliver)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DeviceCommandResponse markReceived(
            Long deviceId,
            Long commandId
    ) {
        DeviceCommand command =
                findForDeviceAndLock(
                        commandId,
                        deviceId
                );

        expireIfNecessary(command);

        if (
                command.getStatus()
                        == DeviceCommandStatus.RECEIVED
        ) {
            return toResponse(command);
        }

        if (
                command.getStatus()
                        != DeviceCommandStatus.DELIVERED
        ) {
            throw invalidState(
                    command,
                    "command must be DELIVERED before it can be received"
            );
        }

        command.setStatus(
                DeviceCommandStatus.RECEIVED
        );
        command.setReceivedAt(Instant.now());

        return toResponse(
                commandRepository.save(command)
        );
    }

    @Transactional
    public DeviceCommandResponse markExecuting(
            Long deviceId,
            Long commandId
    ) {
        DeviceCommand command =
                findForDeviceAndLock(
                        commandId,
                        deviceId
                );

        expireIfNecessary(command);

        if (
                command.getStatus()
                        == DeviceCommandStatus.EXECUTING
        ) {
            return toResponse(command);
        }

        if (
                command.getStatus()
                        != DeviceCommandStatus.RECEIVED
        ) {
            throw invalidState(
                    command,
                    "command must be RECEIVED before execution starts"
            );
        }

        command.setStatus(
                DeviceCommandStatus.EXECUTING
        );

        command.setExecutionStartedAt(
                Instant.now()
        );

        return toResponse(
                commandRepository.save(command)
        );
    }

    @Transactional
    public DeviceCommandResponse completeCommand(
            Long deviceId,
            Long commandId,
            DeviceCommandResultRequest request
    ) {
        DeviceCommand command =
                findForDeviceAndLock(
                        commandId,
                        deviceId
                );

        expireIfNecessary(command);

        if (
                command.getStatus()
                        == DeviceCommandStatus.COMPLETED
        ) {
            return toResponse(command);
        }

        if (
                command.getStatus()
                        != DeviceCommandStatus.RECEIVED
                        && command.getStatus()
                        != DeviceCommandStatus.EXECUTING
        ) {
            throw invalidState(
                    command,
                    "command must be RECEIVED or EXECUTING before completion"
            );
        }

        command.setStatus(
                DeviceCommandStatus.COMPLETED
        );

        command.setResultJson(
                writeJson(request.result())
        );

        command.setFailureReason(null);
        command.setCompletedAt(Instant.now());

        return toResponse(
                commandRepository.save(command)
        );
    }

    @Transactional
    public DeviceCommandResponse failCommand(
            Long deviceId,
            Long commandId,
            DeviceCommandFailureRequest request
    ) {
        DeviceCommand command =
                findForDeviceAndLock(
                        commandId,
                        deviceId
                );

        expireIfNecessary(command);

        if (
                command.getStatus()
                        == DeviceCommandStatus.FAILED
        ) {
            return toResponse(command);
        }

        if (
                command.getStatus()
                        != DeviceCommandStatus.DELIVERED
                        && command.getStatus()
                        != DeviceCommandStatus.RECEIVED
                        && command.getStatus()
                        != DeviceCommandStatus.EXECUTING
        ) {
            throw invalidState(
                    command,
                    "command has not reached an executable state"
            );
        }

        command.setStatus(
                DeviceCommandStatus.FAILED
        );

        command.setResultJson(
                writeJson(request.result())
        );

        command.setFailureReason(
                request.failureReason().trim()
        );

        command.setFailedAt(Instant.now());

        return toResponse(
                commandRepository.save(command)
        );
    }

    /*
     * AUTOMATIC EXPIRATION
     */

    @Transactional
    public int expireDueCommands() {
        Instant now = Instant.now();

        List<DeviceCommand> expiredCommands =
                commandRepository
                        .findExpiredForUpdate(
                                ACTIVE_STATUSES,
                                now
                        );

        for (DeviceCommand command :
                expiredCommands) {
            command.setStatus(
                    DeviceCommandStatus.EXPIRED
            );

            command.setFailureReason(
                    "Command expired before successful completion"
            );
        }

        commandRepository.saveAll(
                expiredCommands
        );

        return expiredCommands.size();
    }

    private GuardianDeviceAccess requireDeviceAccess(
            Long guardianUserId,
            Long deviceId
    ) {
        return accessRepository
                .findByUserIdAndDeviceId(
                        guardianUserId,
                        deviceId
                )
                .orElseThrow(
                        () ->
                                new DeviceAccessDeniedException(
                                        deviceId
                                )
                );
    }

    private GuardianDeviceAccess
    requireManagementAccess(
            Long guardianUserId,
            Long deviceId
    ) {
        GuardianDeviceAccess access =
                requireDeviceAccess(
                        guardianUserId,
                        deviceId
                );

        if (!access.isCanManageDevice()) {
            throw new DeviceAccessDeniedException(
                    deviceId
            );
        }

        return access;
    }

    private DeviceCommand findForDeviceAndLock(
            Long commandId,
            Long deviceId
    ) {
        DeviceCommand command =
                commandRepository
                        .findByIdForUpdate(commandId)
                        .orElseThrow(
                                () ->
                                        new DeviceCommandNotFoundException(
                                                commandId
                                        )
                        );

        if (
                !command.getDevice()
                        .getId()
                        .equals(deviceId)
        ) {
            throw new DeviceCommandNotFoundException(
                    commandId
            );
        }

        return command;
    }

    private void expireIfNecessary(
            DeviceCommand command
    ) {
        if (
                command.getExpiresAt() != null
                        && !command.getStatus()
                        .isTerminal()
                        && !command.getExpiresAt()
                        .isAfter(Instant.now())
        ) {
            command.setStatus(
                    DeviceCommandStatus.EXPIRED
            );

            command.setFailureReason(
                    "Command expired before successful completion"
            );

            commandRepository.save(command);

            throw invalidState(
                    command,
                    "command has expired"
            );
        }
    }

    private void preventConflictingCommand(
            Long deviceId,
            DeviceCommandType commandType
    ) {
        boolean conflictRequired =
                commandType
                        == DeviceCommandType.ACTIVATE_BUZZER
                        || commandType
                        == DeviceCommandType.RESTART_DEVICE;

        if (!conflictRequired) {
            return;
        }

        boolean activeCommandExists =
                commandRepository
                        .existsByDeviceIdAndCommandTypeAndStatusIn(
                                deviceId,
                                commandType,
                                ACTIVE_STATUSES
                        );

        if (activeCommandExists) {
            throw new InvalidDeviceCommandStateException(
                    deviceId,
                    "an active "
                            + commandType
                            + " command already exists"
            );
        }
    }

    private void validatePayload(
            DeviceCommandType commandType,
            Map<String, Object> payload
    ) {
        if (
                commandType
                        == DeviceCommandType.SET_TRACKING_INTERVAL
        ) {
            if (
                    payload == null
                            || !payload.containsKey(
                            "intervalSeconds"
                    )
            ) {
                throw new InvalidDeviceCommandPayloadException(
                        "SET_TRACKING_INTERVAL requires intervalSeconds"
                );
            }

            Object value =
                    payload.get("intervalSeconds");

            if (!(value instanceof Number number)) {
                throw new InvalidDeviceCommandPayloadException(
                        "intervalSeconds must be numeric"
                );
            }

            long intervalSeconds =
                    number.longValue();

            if (
                    intervalSeconds < 10
                            || intervalSeconds > 3600
            ) {
                throw new InvalidDeviceCommandPayloadException(
                        "intervalSeconds must be between 10 and 3600"
                );
            }
        }
    }

    private String writeJson(
            Map<String, Object> value
    ) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        try {
            return objectMapper
                    .writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new InvalidDeviceCommandPayloadException(
                    "Command data could not be converted to JSON"
            );
        }
    }

    private Map<String, Object> readJson(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return Map.of();
        }

        try {
            return objectMapper.readValue(
                    value,
                    new TypeReference<>() {
                    }
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Stored command JSON is invalid",
                    exception
            );
        }
    }

    private InvalidDeviceCommandStateException
    invalidState(
            DeviceCommand command,
            String message
    ) {
        return new InvalidDeviceCommandStateException(
                command.getId(),
                message
                        + "; current status is "
                        + command.getStatus()
        );
    }

    private DeviceCommandResponse toResponse(
            DeviceCommand command
    ) {
        Device device = command.getDevice();

        return new DeviceCommandResponse(
                command.getId(),

                device.getId(),
                device.getDeviceUid(),
                device.getDisplayName(),

                command
                        .getCreatedByGuardian()
                        .getId(),

                command.getCommandType(),
                command.getStatus(),

                readJson(command.getPayloadJson()),
                readJson(command.getResultJson()),

                command.getFailureReason(),

                command.getDeliveredAt(),
                command.getReceivedAt(),
                command.getExecutionStartedAt(),
                command.getCompletedAt(),
                command.getFailedAt(),
                command.getCancelledAt(),
                command.getExpiresAt(),

                command.getCreatedAt(),
                command.getUpdatedAt()
        );
    }
}