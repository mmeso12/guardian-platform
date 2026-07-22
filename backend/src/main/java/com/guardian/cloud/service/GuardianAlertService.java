package com.guardian.cloud.service;

import com.guardian.cloud.dto.alert.AlertActionRequest;
import com.guardian.cloud.dto.alert.AlertUserResponse;
import com.guardian.cloud.dto.alert.GuardianAlertResponse;
import com.guardian.cloud.entity.AlertStatus;
import com.guardian.cloud.entity.Device;
import com.guardian.cloud.entity.GuardianAlert;
import com.guardian.cloud.entity.GuardianDeviceAccess;
import com.guardian.cloud.entity.GuardianUser;
import com.guardian.cloud.exception.AlertAccessDeniedException;
import com.guardian.cloud.exception.GuardianAlertNotFoundException;
import com.guardian.cloud.exception.InvalidAlertStateException;
import com.guardian.cloud.repository.GuardianAlertRepository;
import com.guardian.cloud.repository.GuardianDeviceAccessRepository;
import com.guardian.cloud.repository.GuardianUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class GuardianAlertService {

    private final GuardianAlertRepository guardianAlertRepository;
    private final GuardianDeviceAccessRepository accessRepository;
    private final GuardianUserRepository guardianUserRepository;

    public GuardianAlertService(
            GuardianAlertRepository guardianAlertRepository,
            GuardianDeviceAccessRepository accessRepository,
            GuardianUserRepository guardianUserRepository
    ) {
        this.guardianAlertRepository = guardianAlertRepository;
        this.accessRepository = accessRepository;
        this.guardianUserRepository = guardianUserRepository;
    }

    @Transactional(readOnly = true)
    public List<GuardianAlertResponse> getAllAlerts(
            Long guardianUserId
    ) {
        return accessRepository
                .findAllByUserId(guardianUserId)
                .stream()
                .flatMap(access ->
                        guardianAlertRepository
                                .findAllByDeviceIdOrderByCreatedAtDesc(
                                        access.getDevice().getId()
                                )
                                .stream()
                )
                .sorted(
                        Comparator.comparing(
                                GuardianAlert::getCreatedAt
                        ).reversed()
                )
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GuardianAlertResponse getAlert(
            Long guardianUserId,
            Long alertId
    ) {
        GuardianAlert alert = guardianAlertRepository
                .findById(alertId)
                .orElseThrow(
                        () -> new GuardianAlertNotFoundException(
                                alertId
                        )
                );

        requireDeviceAccess(
                guardianUserId,
                alert.getDevice().getId()
        );

        return toResponse(alert);
    }

    @Transactional(readOnly = true)
    public List<GuardianAlertResponse> getDeviceAlerts(
            Long guardianUserId,
            Long deviceId,
            AlertStatus status
    ) {
        requireDeviceAccess(
                guardianUserId,
                deviceId
        );

        List<GuardianAlert> alerts;

        if (status == null) {
            alerts =
                    guardianAlertRepository
                            .findAllByDeviceIdOrderByCreatedAtDesc(
                                    deviceId
                            );
        } else {
            alerts =
                    guardianAlertRepository
                            .findAllByDeviceIdAndStatusOrderByCreatedAtDesc(
                                    deviceId,
                                    status
                            );
        }

        return alerts.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public GuardianAlertResponse acknowledgeAlert(
            Long guardianUserId,
            Long alertId,
            AlertActionRequest request
    ) {
        GuardianAlert alert = guardianAlertRepository
                .findByIdForUpdate(alertId)
                .orElseThrow(
                        () -> new GuardianAlertNotFoundException(
                                alertId
                        )
                );

        requireDeviceAccess(
                guardianUserId,
                alert.getDevice().getId()
        );

        if (alert.getStatus() == AlertStatus.RESOLVED) {
            throw new InvalidAlertStateException(
                    "A resolved alert cannot be acknowledged"
            );
        }

        if (alert.getStatus() == AlertStatus.ACKNOWLEDGED) {
            throw new InvalidAlertStateException(
                    "Alert has already been acknowledged"
            );
        }

        GuardianUser guardian = findGuardian(
                guardianUserId
        );

        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedBy(guardian);
        alert.setAcknowledgedAt(Instant.now());
        alert.setAcknowledgementNote(
                normalizeNote(request.note())
        );

        return toResponse(
                guardianAlertRepository.save(alert)
        );
    }

    @Transactional
    public GuardianAlertResponse resolveAlert(
            Long guardianUserId,
            Long alertId,
            AlertActionRequest request
    ) {
        GuardianAlert alert = guardianAlertRepository
                .findByIdForUpdate(alertId)
                .orElseThrow(
                        () -> new GuardianAlertNotFoundException(
                                alertId
                        )
                );

        requireDeviceAccess(
                guardianUserId,
                alert.getDevice().getId()
        );

        if (alert.getStatus() == AlertStatus.RESOLVED) {
            throw new InvalidAlertStateException(
                    "Alert has already been resolved"
            );
        }

        GuardianUser guardian = findGuardian(
                guardianUserId
        );

        Instant now = Instant.now();

        if (alert.getStatus() == AlertStatus.OPEN) {
            alert.setAcknowledgedBy(guardian);
            alert.setAcknowledgedAt(now);
            alert.setAcknowledgementNote(
                    "Automatically acknowledged during resolution"
            );
        }

        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedBy(guardian);
        alert.setResolvedAt(now);
        alert.setResolutionNote(
                normalizeNote(request.note())
        );

        return toResponse(
                guardianAlertRepository.save(alert)
        );
    }

    private void requireDeviceAccess(
            Long guardianUserId,
            Long deviceId
    ) {
        boolean hasAccess =
                accessRepository
                        .findByUserIdAndDeviceId(
                                guardianUserId,
                                deviceId
                        )
                        .isPresent();

        if (!hasAccess) {
            throw new AlertAccessDeniedException();
        }
    }

    private GuardianUser findGuardian(
            Long guardianUserId
    ) {
        return guardianUserRepository
                .findById(guardianUserId)
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Authenticated guardian no longer exists"
                        )
                );
    }

    private String normalizeNote(String note) {
        if (note == null || note.isBlank()) {
            return null;
        }

        return note.trim();
    }

    private GuardianAlertResponse toResponse(
            GuardianAlert alert
    ) {
        Device device = alert.getDevice();

        return new GuardianAlertResponse(
                alert.getId(),
                device.getId(),
                device.getDeviceUid(),
                device.getDisplayName(),
                alert.getDeviceEvent().getId(),
                alert.getEventType(),
                alert.getSeverity(),
                alert.getStatus(),
                alert.getTitle(),
                alert.getMessage(),
                alert.getLatitude(),
                alert.getLongitude(),
                alert.getOpenedAt(),
                toUserResponse(alert.getAcknowledgedBy()),
                alert.getAcknowledgedAt(),
                alert.getAcknowledgementNote(),
                toUserResponse(alert.getResolvedBy()),
                alert.getResolvedAt(),
                alert.getResolutionNote(),
                alert.getCreatedAt(),
                alert.getUpdatedAt()
        );
    }

    private AlertUserResponse toUserResponse(
            GuardianUser user
    ) {
        if (user == null) {
            return null;
        }

        return new AlertUserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail()
        );
    }
}