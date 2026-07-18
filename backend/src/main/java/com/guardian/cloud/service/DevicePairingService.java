package com.guardian.cloud.service;

import com.guardian.cloud.dto.device.GuardianDeviceResponse;
import com.guardian.cloud.dto.device.PairDeviceRequest;
import com.guardian.cloud.entity.Device;
import com.guardian.cloud.entity.DeviceAccessRole;
import com.guardian.cloud.entity.DeviceStatus;
import com.guardian.cloud.entity.GuardianDeviceAccess;
import com.guardian.cloud.entity.GuardianUser;
import com.guardian.cloud.exception.DeviceAccessDeniedException;
import com.guardian.cloud.exception.DeviceAlreadyPairedException;
import com.guardian.cloud.exception.GuardianUserNotFoundException;
import com.guardian.cloud.exception.InvalidPairingCodeException;
import com.guardian.cloud.repository.DeviceRepository;
import com.guardian.cloud.repository.GuardianDeviceAccessRepository;
import com.guardian.cloud.repository.GuardianUserRepository;
import com.guardian.cloud.security.PairingCodeHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class DevicePairingService {

    private final DeviceRepository deviceRepository;
    private final GuardianUserRepository guardianUserRepository;
    private final GuardianDeviceAccessRepository accessRepository;
    private final PairingCodeHasher pairingCodeHasher;

    public DevicePairingService(
            DeviceRepository deviceRepository,
            GuardianUserRepository guardianUserRepository,
            GuardianDeviceAccessRepository accessRepository,
            PairingCodeHasher pairingCodeHasher
    ) {
        this.deviceRepository = deviceRepository;
        this.guardianUserRepository = guardianUserRepository;
        this.accessRepository = accessRepository;
        this.pairingCodeHasher = pairingCodeHasher;
    }

    @Transactional
    public GuardianDeviceResponse pairDevice(
            Long userId,
            PairDeviceRequest request
    ) {
        GuardianUser user = findUser(userId);

        String normalizedDeviceUid =
                request.deviceUid().trim().toUpperCase();

        Device device = deviceRepository
                .findByDeviceUidForUpdate(normalizedDeviceUid)
                .orElseThrow(InvalidPairingCodeException::new);

        if (device.getStatus() == DeviceStatus.DEACTIVATED) {
            throw new InvalidPairingCodeException();
        }

        if (device.isPaired()) {
            throw new DeviceAlreadyPairedException(
                    device.getDeviceUid()
            );
        }

        if (!pairingCodeHasher.matches(
                request.pairingCode(),
                device.getPairingCodeHash()
        )) {
            throw new InvalidPairingCodeException();
        }

        if (
                request.displayName() != null
                && !request.displayName().isBlank()
        ) {
            device.setDisplayName(
                    request.displayName().trim()
            );
        }

        device.setPaired(true);
        device.setPairedAt(Instant.now());

        deviceRepository.save(device);

        GuardianDeviceAccess access =
                new GuardianDeviceAccess();

        access.setUser(user);
        access.setDevice(device);
        access.setAccessRole(DeviceAccessRole.OWNER);
        access.setCanViewLocation(true);
        access.setCanManageAlerts(true);
        access.setCanManageDevice(true);

        GuardianDeviceAccess savedAccess =
                accessRepository.save(access);

        return toResponse(savedAccess);
    }

    @Transactional(readOnly = true)
    public List<GuardianDeviceResponse> getDevices(
            Long userId
    ) {
        findUser(userId);

        return accessRepository
                .findAllByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GuardianDeviceResponse getDevice(
            Long userId,
            Long deviceId
    ) {
        GuardianDeviceAccess access =
                accessRepository
                        .findByUserIdAndDeviceId(
                                userId,
                                deviceId
                        )
                        .orElseThrow(
                                () -> new DeviceAccessDeniedException(
                                        deviceId
                                )
                        );

        return toResponse(access);
    }

    @Transactional
    public void unpairDevice(
            Long userId,
            Long deviceId
    ) {
        GuardianDeviceAccess access =
                accessRepository
                        .findByUserIdAndDeviceId(
                                userId,
                                deviceId
                        )
                        .orElseThrow(
                                () -> new DeviceAccessDeniedException(
                                        deviceId
                                )
                        );

        if (
                access.getAccessRole() != DeviceAccessRole.OWNER
                || !access.isCanManageDevice()
        ) {
            throw new DeviceAccessDeniedException(deviceId);
        }

        Device device = access.getDevice();

        accessRepository.delete(access);
        accessRepository.flush();

        boolean otherAccessExists =
                !accessRepository
                        .findAllByDeviceId(deviceId)
                        .isEmpty();

        if (!otherAccessExists) {
            device.setPaired(false);
            device.setPairedAt(null);
            deviceRepository.save(device);
        }
    }

    private GuardianUser findUser(Long userId) {
        return guardianUserRepository
                .findById(userId)
                .orElseThrow(
                        () -> new GuardianUserNotFoundException(
                                "ID " + userId
                        )
                );
    }

    private GuardianDeviceResponse toResponse(
            GuardianDeviceAccess access
    ) {
        Device device = access.getDevice();

        return new GuardianDeviceResponse(
                device.getId(),
                device.getDeviceUid(),
                device.getDisplayName(),
                device.getStatus(),
                device.getBatteryLevel(),
                device.getMotionState(),
                device.getFirmwareVersion(),
                device.getLastSeenAt(),
                device.isPaired(),
                device.getPairedAt(),
                access.getAccessRole(),
                access.isCanViewLocation(),
                access.isCanManageAlerts(),
                access.isCanManageDevice()
        );
    }
}