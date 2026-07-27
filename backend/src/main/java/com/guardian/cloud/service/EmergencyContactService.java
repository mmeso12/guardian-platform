package com.guardian.cloud.service;

import com.guardian.cloud.dto.emergencycontact.*;
import com.guardian.cloud.entity.EmergencyContact;
import com.guardian.cloud.entity.GuardianUser;
import com.guardian.cloud.entity.PreferredContactMethod;
import com.guardian.cloud.exception.*;
import com.guardian.cloud.repository.EmergencyContactRepository;
import com.guardian.cloud.repository.GuardianUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmergencyContactService {

    private final EmergencyContactRepository
            emergencyContactRepository;

    private final GuardianUserRepository
            guardianUserRepository;

    public EmergencyContactService(
            EmergencyContactRepository emergencyContactRepository,
            GuardianUserRepository guardianUserRepository
    ) {
        this.emergencyContactRepository =
                emergencyContactRepository;

        this.guardianUserRepository =
                guardianUserRepository;
    }

    @Transactional
    public EmergencyContactResponse createContact(
            Long guardianUserId,
            CreateEmergencyContactRequest request
    ) {
        GuardianUser guardian =
                findGuardian(guardianUserId);

        validateContactDetails(
                request.phoneNumber(),
                request.email(),
                request.preferredContactMethod()
        );

        validatePriorityForCreate(
                guardianUserId,
                request.priority()
        );

        EmergencyContact contact =
                new EmergencyContact();

        contact.setGuardianUser(guardian);
        contact.setFullName(request.fullName());
        contact.setPhoneNumber(request.phoneNumber());
        contact.setEmail(request.email());
        contact.setRelationship(
                request.relationship()
        );
        contact.setPriority(request.priority());
        contact.setPreferredContactMethod(
                request.preferredContactMethod()
        );
        contact.setEnabled(true);

        return toResponse(
                emergencyContactRepository.save(contact)
        );
    }

    @Transactional(readOnly = true)
    public List<EmergencyContactResponse> getContacts(
            Long guardianUserId
    ) {
        return emergencyContactRepository
                .findAllByGuardianUserIdOrderByPriorityAsc(
                        guardianUserId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EmergencyContactResponse>
    getEnabledContacts(
            Long guardianUserId
    ) {
        return emergencyContactRepository
                .findAllByGuardianUserIdAndEnabledTrueOrderByPriorityAsc(
                        guardianUserId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmergencyContactResponse getContact(
            Long guardianUserId,
            Long contactId
    ) {
        return toResponse(
                findOwnedContact(
                        guardianUserId,
                        contactId
                )
        );
    }

    @Transactional
    public EmergencyContactResponse updateContact(
            Long guardianUserId,
            Long contactId,
            UpdateEmergencyContactRequest request
    ) {
        EmergencyContact contact =
                findOwnedContact(
                        guardianUserId,
                        contactId
                );

        validateContactDetails(
                request.phoneNumber(),
                request.email(),
                request.preferredContactMethod()
        );

        validatePriorityForUpdate(
                guardianUserId,
                request.priority(),
                contactId
        );

        contact.setFullName(request.fullName());
        contact.setPhoneNumber(request.phoneNumber());
        contact.setEmail(request.email());
        contact.setRelationship(
                request.relationship()
        );
        contact.setPriority(request.priority());
        contact.setPreferredContactMethod(
                request.preferredContactMethod()
        );

        return toResponse(
                emergencyContactRepository.save(contact)
        );
    }

    @Transactional
    public EmergencyContactResponse setContactStatus(
            Long guardianUserId,
            Long contactId,
            EmergencyContactStatusRequest request
    ) {
        EmergencyContact contact =
                findOwnedContact(
                        guardianUserId,
                        contactId
                );

        contact.setEnabled(request.enabled());

        return toResponse(
                emergencyContactRepository.save(contact)
        );
    }

    /**
     * Soft-delete behavior:
     * disabling preserves escalation history and allows
     * the contact to be re-enabled later.
     */
    @Transactional
    public void deleteContact(
            Long guardianUserId,
            Long contactId
    ) {
        EmergencyContact contact =
                findOwnedContact(
                        guardianUserId,
                        contactId
                );

        contact.setEnabled(false);

        emergencyContactRepository.save(contact);
    }

    private GuardianUser findGuardian(
            Long guardianUserId
    ) {
        return guardianUserRepository
                .findById(guardianUserId)
                .orElseThrow(
                        () ->
                                new GuardianUserNotFoundException(
                                        "ID "
                                                + guardianUserId
                                )
                );
    }

    private EmergencyContact findOwnedContact(
            Long guardianUserId,
            Long contactId
    ) {
        return emergencyContactRepository
                .findByIdAndGuardianUserId(
                        contactId,
                        guardianUserId
                )
                .orElseThrow(
                        () ->
                                new EmergencyContactNotFoundException(
                                        contactId
                                )
                );
    }

    private void validatePriorityForCreate(
            Long guardianUserId,
            Integer priority
    ) {
        boolean exists =
                emergencyContactRepository
                        .existsByGuardianUserIdAndPriority(
                                guardianUserId,
                                priority
                        );

        if (exists) {
            throw new DuplicateEmergencyContactPriorityException(
                    priority
            );
        }
    }

    private void validatePriorityForUpdate(
            Long guardianUserId,
            Integer priority,
            Long contactId
    ) {
        boolean exists =
                emergencyContactRepository
                        .existsByGuardianUserIdAndPriorityAndIdNot(
                                guardianUserId,
                                priority,
                                contactId
                        );

        if (exists) {
            throw new DuplicateEmergencyContactPriorityException(
                    priority
            );
        }
    }

    private void validateContactDetails(
            String phoneNumber,
            String email,
            PreferredContactMethod method
    ) {
        boolean hasPhone =
                phoneNumber != null
                        && !phoneNumber.isBlank();

        boolean hasEmail =
                email != null
                        && !email.isBlank();

        if (!hasPhone && !hasEmail) {
            throw new InvalidEmergencyContactException(
                    "An emergency contact must have a phone number or email"
            );
        }

        if (
                method == PreferredContactMethod.EMAIL
                        && !hasEmail
        ) {
            throw new InvalidEmergencyContactException(
                    "Email is required when the preferred contact method is EMAIL"
            );
        }

        if (
                (
                        method
                                == PreferredContactMethod.PHONE_CALL
                                || method
                                == PreferredContactMethod.SMS
                                || method
                                == PreferredContactMethod.WHATSAPP
                )
                        && !hasPhone
        ) {
            throw new InvalidEmergencyContactException(
                    "A phone number is required for the selected contact method"
            );
        }

        if (
                method == PreferredContactMethod.ALL
                        && (!hasPhone || !hasEmail)
        ) {
            throw new InvalidEmergencyContactException(
                    "Phone number and email are required when the preferred contact method is ALL"
            );
        }
    }

    private EmergencyContactResponse toResponse(
            EmergencyContact contact
    ) {
        return new EmergencyContactResponse(
                contact.getId(),
                contact.getFullName(),
                contact.getPhoneNumber(),
                contact.getEmail(),
                contact.getRelationship(),
                contact.getPriority(),
                contact.getPreferredContactMethod(),
                contact.isEnabled(),
                contact.getCreatedAt(),
                contact.getUpdatedAt()
        );
    }
}