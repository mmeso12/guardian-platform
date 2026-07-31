package com.guardian.cloud.service;

import com.guardian.cloud.dto.child.ChildResponse;
import com.guardian.cloud.dto.child.CreateChildRequest;
import com.guardian.cloud.dto.child.UpdateChildRequest;
import com.guardian.cloud.entity.ChildProfile;
import com.guardian.cloud.entity.GuardianUser;
import com.guardian.cloud.exception.ChildProfileNotFoundException;
import com.guardian.cloud.exception.GuardianUserNotFoundException;
import com.guardian.cloud.repository.ChildProfileRepository;
import com.guardian.cloud.repository.GuardianUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ChildProfileService {

    private static final int MAXIMUM_ACTIVE_CHILDREN = 20;

    private final ChildProfileRepository
            childProfileRepository;

    private final GuardianUserRepository
            guardianUserRepository;

    public ChildProfileService(
            ChildProfileRepository childProfileRepository,
            GuardianUserRepository guardianUserRepository
    ) {
        this.childProfileRepository =
                childProfileRepository;

        this.guardianUserRepository =
                guardianUserRepository;
    }

    /**
     * Creates a child profile owned by the
     * authenticated guardian.
     */
    @Transactional
    public ChildResponse createChild(
            Long guardianUserId,
            CreateChildRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Child profile request is required"
            );
        }

        GuardianUser guardianUser =
                requireGuardianUser(
                        guardianUserId
                );

        long activeChildCount =
                childProfileRepository
                        .countByGuardianUserIdAndActiveTrue(
                                guardianUserId
                        );

        if (
                activeChildCount
                        >= MAXIMUM_ACTIVE_CHILDREN
        ) {
            throw new IllegalStateException(
                    "A guardian cannot have more than "
                            + MAXIMUM_ACTIVE_CHILDREN
                            + " active child profiles"
            );
        }

        validateDateOfBirth(
                request.dateOfBirth()
        );

        ChildProfile childProfile =
                new ChildProfile();

        childProfile.setGuardianUser(
                guardianUser
        );

        childProfile.setFirstName(
                normalizeRequired(
                        request.firstName(),
                        "First name"
                )
        );

        childProfile.setLastName(
                normalizeOptional(
                        request.lastName()
                )
        );

        childProfile.setDateOfBirth(
                request.dateOfBirth()
        );

        childProfile.setGender(
                normalizeGender(
                        request.gender()
                )
        );

        childProfile.setProfileImageUrl(
                normalizeOptional(
                        request.profileImageUrl()
                )
        );

        childProfile.setActive(true);

        ChildProfile savedChild =
                childProfileRepository.save(
                        childProfile
                );

        return toResponse(savedChild);
    }

    /**
     * Returns all active children owned by the
     * authenticated guardian.
     */
    @Transactional(readOnly = true)
    public List<ChildResponse> getActiveChildren(
            Long guardianUserId
    ) {
        requireGuardianUser(
                guardianUserId
        );

        return childProfileRepository
                .findAllByGuardianUserIdAndActiveTrueOrderByCreatedAtDesc(
                        guardianUserId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Returns every child owned by the guardian,
     * including deactivated profiles.
     *
     * This may later be useful for an account
     * history or administrative screen.
     */
    @Transactional(readOnly = true)
    public List<ChildResponse> getAllChildren(
            Long guardianUserId
    ) {
        requireGuardianUser(
                guardianUserId
        );

        return childProfileRepository
                .findAllByGuardianUserIdOrderByCreatedAtDesc(
                        guardianUserId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Retrieves one active child while enforcing
     * guardian ownership.
     */
    @Transactional(readOnly = true)
    public ChildResponse getChild(
            Long guardianUserId,
            UUID childId
    ) {
        ChildProfile childProfile =
                requireActiveOwnedChild(
                        guardianUserId,
                        childId
                );

        return toResponse(childProfile);
    }

    /**
     * Updates an active child owned by the
     * authenticated guardian.
     */
    @Transactional
    public ChildResponse updateChild(
            Long guardianUserId,
            UUID childId,
            UpdateChildRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Child update request is required"
            );
        }

        ChildProfile childProfile =
                requireActiveOwnedChild(
                        guardianUserId,
                        childId
                );

        validateDateOfBirth(
                request.dateOfBirth()
        );

        childProfile.setFirstName(
                normalizeRequired(
                        request.firstName(),
                        "First name"
                )
        );

        childProfile.setLastName(
                normalizeOptional(
                        request.lastName()
                )
        );

        childProfile.setDateOfBirth(
                request.dateOfBirth()
        );

        childProfile.setGender(
                normalizeGender(
                        request.gender()
                )
        );

        childProfile.setProfileImageUrl(
                normalizeOptional(
                        request.profileImageUrl()
                )
        );

        ChildProfile savedChild =
                childProfileRepository.save(
                        childProfile
                );

        return toResponse(savedChild);
    }

    /**
     * Soft-deletes a child profile.
     *
     * The database row remains available for
     * future device, location and safety history.
     */
    @Transactional
    public void deactivateChild(
            Long guardianUserId,
            UUID childId
    ) {
        ChildProfile childProfile =
                requireActiveOwnedChild(
                        guardianUserId,
                        childId
                );

        childProfile.deactivate();

        childProfileRepository.save(
                childProfile
        );
    }

    /**
     * Restores a previously deactivated child
     * profile owned by the guardian.
     */
    @Transactional
    public ChildResponse reactivateChild(
            Long guardianUserId,
            UUID childId
    ) {
        ChildProfile childProfile =
                requireOwnedChild(
                        guardianUserId,
                        childId
                );

        if (childProfile.isActive()) {
            return toResponse(childProfile);
        }

        long activeChildCount =
                childProfileRepository
                        .countByGuardianUserIdAndActiveTrue(
                                guardianUserId
                        );

        if (
                activeChildCount
                        >= MAXIMUM_ACTIVE_CHILDREN
        ) {
            throw new IllegalStateException(
                    "A guardian cannot have more than "
                            + MAXIMUM_ACTIVE_CHILDREN
                            + " active child profiles"
            );
        }

        childProfile.activate();

        ChildProfile savedChild =
                childProfileRepository.save(
                        childProfile
                );

        return toResponse(savedChild);
    }

    /**
     * Internal helper for future device and
     * location services.
     *
     * It returns the managed entity while still
     * enforcing ownership and active status.
     */
    @Transactional(readOnly = true)
    public ChildProfile requireActiveChildEntity(
            Long guardianUserId,
            UUID childId
    ) {
        return requireActiveOwnedChild(
                guardianUserId,
                childId
        );
    }

    private GuardianUser requireGuardianUser(
            Long guardianUserId
    ) {
        if (guardianUserId == null) {
            throw new GuardianUserNotFoundException(
                    "null ID"
            );
        }

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

    private ChildProfile requireOwnedChild(
            Long guardianUserId,
            UUID childId
    ) {
        if (
                guardianUserId == null
                        || childId == null
        ) {
            throw new ChildProfileNotFoundException(
                    childId
            );
        }

        return childProfileRepository
                .findByChildIdAndGuardianUserId(
                        childId,
                        guardianUserId
                )
                .orElseThrow(
                        () ->
                                new ChildProfileNotFoundException(
                                        childId
                                )
                );
    }

    private ChildProfile requireActiveOwnedChild(
            Long guardianUserId,
            UUID childId
    ) {
        if (
                guardianUserId == null
                        || childId == null
        ) {
            throw new ChildProfileNotFoundException(
                    childId
            );
        }

        return childProfileRepository
                .findByChildIdAndGuardianUserIdAndActiveTrue(
                        childId,
                        guardianUserId
                )
                .orElseThrow(
                        () ->
                                new ChildProfileNotFoundException(
                                        childId
                                )
                );
    }

    private ChildResponse toResponse(
            ChildProfile childProfile
    ) {
        return new ChildResponse(
                childProfile.getChildId(),
                childProfile.getFirstName(),
                childProfile.getLastName(),
                childProfile.getDateOfBirth(),
                childProfile.getGender(),
                childProfile.getProfileImageUrl(),
                childProfile.isActive(),
                childProfile.getCreatedAt(),
                childProfile.getUpdatedAt()
        );
    }

    private void validateDateOfBirth(
            LocalDate dateOfBirth
    ) {
        if (
                dateOfBirth != null
                        && !dateOfBirth.isBefore(
                        LocalDate.now()
                )
        ) {
            throw new IllegalArgumentException(
                    "Date of birth must be in the past"
            );
        }
    }

    private String normalizeRequired(
            String value,
            String fieldName
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }

        return value.trim();
    }

    private String normalizeOptional(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    private String normalizeGender(
            String gender
    ) {
        String normalized =
                normalizeOptional(gender);

        if (normalized == null) {
            return null;
        }

        return normalized
                .toUpperCase(Locale.ROOT);
    }
}