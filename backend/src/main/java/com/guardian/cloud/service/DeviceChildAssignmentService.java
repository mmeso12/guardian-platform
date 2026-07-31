package com.guardian.cloud.service;

import com.guardian.cloud.dto.device.AssignDeviceToChildRequest;
import com.guardian.cloud.dto.device.DeviceChildAssignmentResponse;
import com.guardian.cloud.entity.*;
import com.guardian.cloud.exception.ChildProfileNotFoundException;
import com.guardian.cloud.exception.DeviceAccessDeniedException;
import com.guardian.cloud.exception.DeviceAlreadyAssignedException;
import com.guardian.cloud.exception.DeviceChildAssignmentNotFoundException;
import com.guardian.cloud.repository.ChildProfileRepository;
import com.guardian.cloud.repository.DeviceChildAssignmentRepository;
import com.guardian.cloud.repository.GuardianDeviceAccessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DeviceChildAssignmentService {

    private final DeviceChildAssignmentRepository
            assignmentRepository;

    private final GuardianDeviceAccessRepository
            accessRepository;

    private final ChildProfileRepository
            childProfileRepository;

    public DeviceChildAssignmentService(
            DeviceChildAssignmentRepository assignmentRepository,
            GuardianDeviceAccessRepository accessRepository,
            ChildProfileRepository childProfileRepository
    ) {
        this.assignmentRepository = assignmentRepository;
        this.accessRepository = accessRepository;
        this.childProfileRepository = childProfileRepository;
    }

    @Transactional
    public DeviceChildAssignmentResponse assignDevice(
            Long guardianUserId,
            Long deviceId,
            AssignDeviceToChildRequest request
    ) {
        GuardianDeviceAccess access =
                requireManageableDevice(
                        guardianUserId,
                        deviceId
                );

        ChildProfile child =
                requireActiveOwnedChild(
                        guardianUserId,
                        request.childId()
                );

        assignmentRepository
                .findActiveByDeviceIdForUpdate(deviceId)
                .ifPresent(existing -> {
                    throw new DeviceAlreadyAssignedException(
                            deviceId
                    );
                });

        DeviceChildAssignment assignment =
                new DeviceChildAssignment();

        assignment.setDevice(access.getDevice());
        assignment.setChildProfile(child);
        assignment.setActive(true);

        return toResponse(
                assignmentRepository.save(assignment)
        );
    }

    @Transactional(readOnly = true)
    public DeviceChildAssignmentResponse getDeviceAssignment(
            Long guardianUserId,
            Long deviceId
    ) {
        requireDeviceAccess(
                guardianUserId,
                deviceId
        );

        DeviceChildAssignment assignment =
                assignmentRepository
                        .findByDeviceIdAndActiveTrue(deviceId)
                        .orElseThrow(
                                () ->
                                        new DeviceChildAssignmentNotFoundException(
                                                deviceId
                                        )
                        );

        verifyChildOwnership(
                assignment,
                guardianUserId
        );

        return toResponse(assignment);
    }

    @Transactional
    public void unassignDevice(
            Long guardianUserId,
            Long deviceId
    ) {
        requireManageableDevice(
                guardianUserId,
                deviceId
        );

        DeviceChildAssignment assignment =
                assignmentRepository
                        .findActiveByDeviceIdForUpdate(deviceId)
                        .orElseThrow(
                                () ->
                                        new DeviceChildAssignmentNotFoundException(
                                                deviceId
                                        )
                        );

        verifyChildOwnership(
                assignment,
                guardianUserId
        );

        assignment.unassign();
        assignmentRepository.save(assignment);
    }

    @Transactional
    public DeviceChildAssignmentResponse reassignDevice(
            Long guardianUserId,
            Long deviceId,
            AssignDeviceToChildRequest request
    ) {
        GuardianDeviceAccess access =
                requireManageableDevice(
                        guardianUserId,
                        deviceId
                );

        ChildProfile newChild =
                requireActiveOwnedChild(
                        guardianUserId,
                        request.childId()
                );

        assignmentRepository
                .findActiveByDeviceIdForUpdate(deviceId)
                .ifPresent(existing -> {
                    verifyChildOwnership(
                            existing,
                            guardianUserId
                    );

                    if (
                            existing.getChildProfile()
                                    .getChildId()
                                    .equals(request.childId())
                    ) {
                        throw new DeviceAlreadyAssignedException(
                                deviceId
                        );
                    }

                    existing.unassign();
                    assignmentRepository.save(existing);
                    assignmentRepository.flush();
                });

        DeviceChildAssignment replacement =
                new DeviceChildAssignment();

        replacement.setDevice(access.getDevice());
        replacement.setChildProfile(newChild);
        replacement.setActive(true);

        return toResponse(
                assignmentRepository.save(replacement)
        );
    }

    @Transactional(readOnly = true)
    public List<DeviceChildAssignmentResponse>
    getChildDevices(
            Long guardianUserId,
            UUID childId
    ) {
        ChildProfile child =
                requireActiveOwnedChild(
                        guardianUserId,
                        childId
                );

        return assignmentRepository
                .findAllByChildProfileIdAndActiveTrueOrderByAssignedAtDesc(
                        child.getId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DeviceChildAssignmentResponse>
    getDeviceAssignmentHistory(
            Long guardianUserId,
            Long deviceId
    ) {
        requireManageableDevice(
                guardianUserId,
                deviceId
        );

        return assignmentRepository
                .findAllByDeviceIdOrderByAssignedAtDesc(
                        deviceId
                )
                .stream()
                .filter(
                        assignment ->
                                assignment
                                        .getChildProfile()
                                        .getGuardianUser()
                                        .getId()
                                        .equals(guardianUserId)
                )
                .map(this::toResponse)
                .toList();
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

    private GuardianDeviceAccess requireManageableDevice(
            Long guardianUserId,
            Long deviceId
    ) {
        GuardianDeviceAccess access =
                requireDeviceAccess(
                        guardianUserId,
                        deviceId
                );

        if (
                access.getAccessRole()
                        != DeviceAccessRole.OWNER
                        || !access.isCanManageDevice()
        ) {
            throw new DeviceAccessDeniedException(
                    deviceId
            );
        }

        return access;
    }

    private ChildProfile requireActiveOwnedChild(
            Long guardianUserId,
            UUID childId
    ) {
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

    private void verifyChildOwnership(
            DeviceChildAssignment assignment,
            Long guardianUserId
    ) {
        Long ownerId =
                assignment
                        .getChildProfile()
                        .getGuardianUser()
                        .getId();

        if (!ownerId.equals(guardianUserId)) {
            throw new DeviceAccessDeniedException(
                    assignment.getDevice().getId()
            );
        }
    }

    private DeviceChildAssignmentResponse toResponse(
            DeviceChildAssignment assignment
    ) {
        Device device = assignment.getDevice();
        ChildProfile child =
                assignment.getChildProfile();

        return new DeviceChildAssignmentResponse(
                assignment.getAssignmentId(),

                device.getId(),
                device.getDeviceUid(),
                device.getDisplayName(),

                child.getChildId(),
                child.getFirstName(),
                child.getLastName(),
                child.getDateOfBirth(),

                assignment.getAssignedAt(),
                assignment.getUnassignedAt(),
                assignment.isActive()
        );
    }
}