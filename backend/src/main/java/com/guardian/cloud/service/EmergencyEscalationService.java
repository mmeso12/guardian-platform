package com.guardian.cloud.service;

import com.guardian.cloud.config.EmergencyEscalationProperties;
import com.guardian.cloud.dto.escalation.*;
import com.guardian.cloud.entity.*;
import com.guardian.cloud.exception.*;
import com.guardian.cloud.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class EmergencyEscalationService {

    private final EmergencyEscalationRepository
            escalationRepository;

    private final EmergencyContactAttemptRepository
            attemptRepository;

    private final EmergencyContactRepository
            contactRepository;

    private final GuardianDeviceAccessRepository
            accessRepository;

    private final GuardianAlertRepository
            alertRepository;

    private final GuardianUserRepository
            guardianUserRepository;

    private final EmergencyEscalationProperties
            properties;

    public EmergencyEscalationService(
            EmergencyEscalationRepository escalationRepository,
            EmergencyContactAttemptRepository attemptRepository,
            EmergencyContactRepository contactRepository,
            GuardianDeviceAccessRepository accessRepository,
            GuardianAlertRepository alertRepository,
            GuardianUserRepository guardianUserRepository,
            EmergencyEscalationProperties properties
    ) {
        this.escalationRepository =
                escalationRepository;
        this.attemptRepository = attemptRepository;
        this.contactRepository = contactRepository;
        this.accessRepository = accessRepository;
        this.alertRepository = alertRepository;
        this.guardianUserRepository =
                guardianUserRepository;
        this.properties = properties;
    }

    @Transactional
    public void createForSosAlert(
            GuardianAlert alert
    ) {
        if (
                alert == null
                        || alert.getId() == null
                        || alert.getEventType()
                        != EventType.SOS
        ) {
            return;
        }

        List<GuardianDeviceAccess> accessEntries =
                accessRepository.findAllByDeviceId(
                        alert.getDevice().getId()
                );

        for (GuardianDeviceAccess access : accessEntries) {
            GuardianUser guardian = access.getUser();

            if (
                    guardian == null
                            || guardian.getId() == null
                            || !guardian.isEnabled()
                            || !access.isCanManageAlerts()
            ) {
                continue;
            }

            createForGuardian(alert, guardian);
        }
    }

    private void createForGuardian(
            GuardianAlert alert,
            GuardianUser guardian
    ) {
        boolean exists =
                escalationRepository
                        .existsByGuardianAlertIdAndGuardianUserId(
                                alert.getId(),
                                guardian.getId()
                        );

        if (exists) {
            return;
        }

        Instant now = Instant.now();

        EmergencyEscalation escalation =
                new EmergencyEscalation();

        escalation.setGuardianAlert(alert);
        escalation.setGuardianUser(guardian);
        escalation.setStartedAt(now);
        escalation.setCurrentAttemptNumber(0);

        List<EmergencyContact> contacts =
                contactRepository
                        .findAllByGuardianUserIdAndEnabledTrueOrderByPriorityAsc(
                                guardian.getId()
                        );

        if (contacts.isEmpty()) {
            escalation.setStatus(
                    EmergencyEscalationStatus.EXHAUSTED
            );
            escalation.setNextActionAt(null);

            escalationRepository.save(escalation);
            return;
        }

        EmergencyContact firstContact =
                contacts.getFirst();

        escalation.setStatus(
                EmergencyEscalationStatus.IN_PROGRESS
        );
        escalation.setCurrentPriority(
                firstContact.getPriority()
        );
        escalation.setCurrentAttemptNumber(1);
        escalation.setNextActionAt(
                now.plus(properties.getAttemptTimeout())
        );

        escalation =
                escalationRepository.save(escalation);

        createAttempt(
                escalation,
                firstContact,
                1,
                now
        );
    }

    @Transactional(readOnly = true)
    public List<EmergencyEscalationResponse>
    getEscalations(Long guardianUserId) {
        return escalationRepository
                .findAllByGuardianUserIdOrderByCreatedAtDesc(
                        guardianUserId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmergencyEscalationResponse getEscalation(
            Long guardianUserId,
            Long escalationId
    ) {
        return toResponse(
                escalationRepository
                        .findByIdAndGuardianUserId(
                                escalationId,
                                guardianUserId
                        )
                        .orElseThrow(
                                () ->
                                        new EmergencyEscalationNotFoundException(
                                                escalationId
                                        )
                        )
        );
    }

    @Transactional
    public EmergencyEscalationResponse acknowledge(
            Long guardianUserId,
            Long escalationId,
            EmergencyEscalationActionRequest request
    ) {
        EmergencyEscalation escalation =
                findOwnedForUpdate(
                        guardianUserId,
                        escalationId
                );

        requireInProgress(escalation);

        Instant now = Instant.now();

        EmergencyContactAttempt attempt =
                currentAttempt(escalation);

        attempt.setStatus(
                EmergencyContactAttemptStatus.ACKNOWLEDGED
        );
        attempt.setAcknowledgedAt(now);
        attempt.setCompletedAt(now);
        attemptRepository.save(attempt);

        escalation.setStatus(
                EmergencyEscalationStatus.ACKNOWLEDGED
        );
        escalation.setAcknowledgedAt(now);
        escalation.setAcknowledgementNote(
                normalizeNote(request.note())
        );
        escalation.setNextActionAt(null);

        synchronizeAlertAcknowledgement(
                escalation,
                now,
                request.note()
        );

        return toResponse(
                escalationRepository.save(escalation)
        );
    }

    @Transactional
    public EmergencyEscalationResponse resolve(
            Long guardianUserId,
            Long escalationId,
            EmergencyEscalationActionRequest request
    ) {
        EmergencyEscalation escalation =
                findOwnedForUpdate(
                        guardianUserId,
                        escalationId
                );

        if (
                escalation.getStatus()
                        == EmergencyEscalationStatus.RESOLVED
        ) {
            throw new InvalidEmergencyEscalationStateException(
                    "Emergency escalation has already been resolved"
            );
        }

        Instant now = Instant.now();

        if (
                escalation.getStatus()
                        == EmergencyEscalationStatus.IN_PROGRESS
        ) {
            EmergencyContactAttempt attempt =
                    currentAttempt(escalation);

            attempt.setStatus(
                    EmergencyContactAttemptStatus.SKIPPED
            );
            attempt.setCompletedAt(now);
            attempt.setFailureReason(
                    "Escalation resolved by guardian"
            );

            attemptRepository.save(attempt);
        }

        escalation.setStatus(
                EmergencyEscalationStatus.RESOLVED
        );
        escalation.setResolvedAt(now);
        escalation.setResolutionNote(
                normalizeNote(request.note())
        );
        escalation.setNextActionAt(null);

        synchronizeAlertResolution(
                escalation,
                now,
                request.note()
        );

        return toResponse(
                escalationRepository.save(escalation)
        );
    }

    @Transactional
    public EmergencyEscalationResponse escalateNow(
            Long guardianUserId,
            Long escalationId
    ) {
        EmergencyEscalation escalation =
                findOwnedForUpdate(
                        guardianUserId,
                        escalationId
                );

        requireInProgress(escalation);

        advanceToNextContact(
                escalation,
                Instant.now(),
                EmergencyContactAttemptStatus.SKIPPED,
                "Manually escalated by guardian"
        );

        return toResponse(escalation);
    }

    @Transactional
    public int processDueEscalations() {
        Instant now = Instant.now();

        List<EmergencyEscalation> due =
                escalationRepository
                        .findDueEscalationsForUpdate(
                                EmergencyEscalationStatus.IN_PROGRESS,
                                now
                        );

        for (EmergencyEscalation escalation : due) {
            advanceToNextContact(
                    escalation,
                    now,
                    EmergencyContactAttemptStatus.TIMED_OUT,
                    "Emergency contact did not acknowledge in time"
            );
        }

        return due.size();
    }

    private void advanceToNextContact(
            EmergencyEscalation escalation,
            Instant now,
            EmergencyContactAttemptStatus previousStatus,
            String reason
    ) {
        EmergencyContactAttempt current =
                currentAttempt(escalation);

        current.setStatus(previousStatus);
        current.setCompletedAt(now);
        current.setFailureReason(reason);
        attemptRepository.save(current);

        List<EmergencyContact> contacts =
                contactRepository
                        .findAllByGuardianUserIdAndEnabledTrueOrderByPriorityAsc(
                                escalation
                                        .getGuardianUser()
                                        .getId()
                        );

        EmergencyContact nextContact =
                contacts.stream()
                        .filter(
                                contact ->
                                        contact.getPriority()
                                                > escalation
                                                .getCurrentPriority()
                        )
                        .findFirst()
                        .orElse(null);

        if (nextContact == null) {
            escalation.setStatus(
                    EmergencyEscalationStatus.EXHAUSTED
            );
            escalation.setNextActionAt(null);
            escalationRepository.save(escalation);
            return;
        }

        int nextAttemptNumber =
                escalation.getCurrentAttemptNumber() + 1;

        escalation.setCurrentPriority(
                nextContact.getPriority()
        );
        escalation.setCurrentAttemptNumber(
                nextAttemptNumber
        );
        escalation.setNextActionAt(
                now.plus(properties.getAttemptTimeout())
        );

        escalationRepository.save(escalation);

        createAttempt(
                escalation,
                nextContact,
                nextAttemptNumber,
                now
        );
    }

    private EmergencyContactAttempt createAttempt(
            EmergencyEscalation escalation,
            EmergencyContact contact,
            int attemptNumber,
            Instant attemptedAt
    ) {
        EmergencyContactAttempt attempt =
                new EmergencyContactAttempt();

        attempt.setEmergencyEscalation(escalation);
        attempt.setEmergencyContact(contact);
        attempt.setAttemptNumber(attemptNumber);
        attempt.setContactMethod(
                contact.getPreferredContactMethod()
        );
        attempt.setStatus(
                EmergencyContactAttemptStatus.PENDING
        );
        attempt.setContactName(contact.getFullName());
        attempt.setPhoneNumber(contact.getPhoneNumber());
        attempt.setEmail(contact.getEmail());
        attempt.setAttemptedAt(attemptedAt);

        return attemptRepository.save(attempt);
    }

    private EmergencyEscalation findOwnedForUpdate(
            Long guardianUserId,
            Long escalationId
    ) {
        return escalationRepository
                .findOwnedByIdForUpdate(
                        escalationId,
                        guardianUserId
                )
                .orElseThrow(
                        () ->
                                new EmergencyEscalationNotFoundException(
                                        escalationId
                                )
                );
    }

    private EmergencyContactAttempt currentAttempt(
            EmergencyEscalation escalation
    ) {
        return attemptRepository
                .findFirstByEmergencyEscalationIdOrderByAttemptNumberDesc(
                        escalation.getId()
                )
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Active escalation has no contact attempt"
                                )
                );
    }

    private void requireInProgress(
            EmergencyEscalation escalation
    ) {
        if (
                escalation.getStatus()
                        != EmergencyEscalationStatus.IN_PROGRESS
        ) {
            throw new InvalidEmergencyEscalationStateException(
                    "Only an in-progress escalation can perform this action"
            );
        }
    }

    private void synchronizeAlertAcknowledgement(
            EmergencyEscalation escalation,
            Instant now,
            String note
    ) {
        GuardianAlert alert =
                escalation.getGuardianAlert();

        if (alert.getStatus() == AlertStatus.OPEN) {
            alert.setStatus(AlertStatus.ACKNOWLEDGED);
            alert.setAcknowledgedBy(
                    escalation.getGuardianUser()
            );
            alert.setAcknowledgedAt(now);
            alert.setAcknowledgementNote(
                    normalizeNote(note)
            );

            alertRepository.save(alert);
        }
    }

    private void synchronizeAlertResolution(
            EmergencyEscalation escalation,
            Instant now,
            String note
    ) {
        GuardianAlert alert =
                escalation.getGuardianAlert();

        GuardianUser guardian =
                escalation.getGuardianUser();

        if (alert.getStatus() == AlertStatus.OPEN) {
            alert.setAcknowledgedBy(guardian);
            alert.setAcknowledgedAt(now);
            alert.setAcknowledgementNote(
                    "Automatically acknowledged during escalation resolution"
            );
        }

        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedBy(guardian);
        alert.setResolvedAt(now);
        alert.setResolutionNote(
                normalizeNote(note)
        );

        alertRepository.save(alert);
    }

    private EmergencyEscalationResponse toResponse(
            EmergencyEscalation escalation
    ) {
        GuardianAlert alert =
                escalation.getGuardianAlert();

        Device device = alert.getDevice();

        List<EmergencyContactAttemptResponse> attempts =
                attemptRepository
                        .findAllByEmergencyEscalationIdOrderByAttemptNumberAsc(
                                escalation.getId()
                        )
                        .stream()
                        .map(this::toAttemptResponse)
                        .toList();

        return new EmergencyEscalationResponse(
                escalation.getId(),
                alert.getId(),
                device.getId(),
                device.getDeviceUid(),
                device.getDisplayName(),
                escalation.getStatus(),
                escalation.getCurrentPriority(),
                escalation.getCurrentAttemptNumber(),
                escalation.getNextActionAt(),
                escalation.getStartedAt(),
                escalation.getAcknowledgedAt(),
                escalation.getResolvedAt(),
                escalation.getAcknowledgementNote(),
                escalation.getResolutionNote(),
                attempts,
                escalation.getCreatedAt(),
                escalation.getUpdatedAt()
        );
    }

    private EmergencyContactAttemptResponse
    toAttemptResponse(
            EmergencyContactAttempt attempt
    ) {
        return new EmergencyContactAttemptResponse(
                attempt.getId(),
                attempt.getEmergencyContact().getId(),
                attempt.getAttemptNumber(),
                attempt.getContactMethod(),
                attempt.getStatus(),
                attempt.getContactName(),
                attempt.getPhoneNumber(),
                attempt.getEmail(),
                attempt.getAttemptedAt(),
                attempt.getAcknowledgedAt(),
                attempt.getCompletedAt(),
                attempt.getFailureReason()
        );
    }

    private String normalizeNote(String note) {
        if (note == null || note.isBlank()) {
            return null;
        }

        return note.trim();
    }
}