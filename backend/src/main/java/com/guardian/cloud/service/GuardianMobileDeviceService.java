package com.guardian.cloud.service;

import com.guardian.cloud.dto.push.GuardianMobileDeviceResponse;
import com.guardian.cloud.dto.push.RegisterMobileDeviceRequest;
import com.guardian.cloud.entity.GuardianMobileDevice;
import com.guardian.cloud.entity.GuardianUser;
import com.guardian.cloud.exception.GuardianMobileDeviceNotFoundException;
import com.guardian.cloud.exception.GuardianUserNotFoundException;
import com.guardian.cloud.exception.PushTokenAlreadyRegisteredException;
import com.guardian.cloud.repository.GuardianMobileDeviceRepository;
import com.guardian.cloud.repository.GuardianUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class GuardianMobileDeviceService {

    private final GuardianMobileDeviceRepository
            mobileDeviceRepository;

    private final GuardianUserRepository
            guardianUserRepository;

    public GuardianMobileDeviceService(
            GuardianMobileDeviceRepository
                    mobileDeviceRepository,
            GuardianUserRepository
                    guardianUserRepository
    ) {
        this.mobileDeviceRepository =
                mobileDeviceRepository;

        this.guardianUserRepository =
                guardianUserRepository;
    }

    @Transactional
    public GuardianMobileDeviceResponse register(
            Long guardianUserId,
            RegisterMobileDeviceRequest request
    ) {
        GuardianUser guardian =
                guardianUserRepository
                        .findById(guardianUserId)
                        .orElseThrow(
                                () ->
                                        new GuardianUserNotFoundException(
                                                "ID "
                                                        + guardianUserId
                                        )
                        );

        String normalizedToken =
                request.pushToken().trim();

        Optional<GuardianMobileDevice> existingToken =
                mobileDeviceRepository
                        .findByPushToken(
                                normalizedToken
                        );

        if (existingToken.isPresent()) {
            GuardianMobileDevice existing =
                    existingToken.get();

            if (
                    !existing.getGuardianUser()
                            .getId()
                            .equals(guardianUserId)
            ) {
                throw new PushTokenAlreadyRegisteredException();
            }

            existing.setPlatform(request.platform());
            existing.setDeviceName(
                    request.deviceName()
            );
            existing.setAppVersion(
                    request.appVersion()
            );
            existing.setEnabled(true);
            existing.setLastSeenAt(Instant.now());

            return toResponse(
                    mobileDeviceRepository.save(existing)
            );
        }

        GuardianMobileDevice mobileDevice =
                new GuardianMobileDevice();

        mobileDevice.setGuardianUser(guardian);
        mobileDevice.setPushToken(normalizedToken);
        mobileDevice.setPlatform(
                request.platform()
        );
        mobileDevice.setDeviceName(
                request.deviceName()
        );
        mobileDevice.setAppVersion(
                request.appVersion()
        );
        mobileDevice.setEnabled(true);
        mobileDevice.setLastSeenAt(Instant.now());

        return toResponse(
                mobileDeviceRepository.save(
                        mobileDevice
                )
        );
    }

    @Transactional(readOnly = true)
    public List<GuardianMobileDeviceResponse>
    getDevices(Long guardianUserId) {
        return mobileDeviceRepository
                .findAllByGuardianUserIdOrderByCreatedAtDesc(
                        guardianUserId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public GuardianMobileDeviceResponse enable(
            Long guardianUserId,
            Long mobileDeviceId
    ) {
        GuardianMobileDevice mobileDevice =
                findOwnedDevice(
                        guardianUserId,
                        mobileDeviceId
                );

        mobileDevice.setEnabled(true);
        mobileDevice.setLastSeenAt(Instant.now());

        return toResponse(
                mobileDeviceRepository.save(
                        mobileDevice
                )
        );
    }

    @Transactional
    public GuardianMobileDeviceResponse disable(
            Long guardianUserId,
            Long mobileDeviceId
    ) {
        GuardianMobileDevice mobileDevice =
                findOwnedDevice(
                        guardianUserId,
                        mobileDeviceId
                );

        mobileDevice.setEnabled(false);

        return toResponse(
                mobileDeviceRepository.save(
                        mobileDevice
                )
        );
    }

    /**
     * Soft deletion preserves historical push-delivery
     * records linked to this mobile device.
     */
    @Transactional
    public void delete(
            Long guardianUserId,
            Long mobileDeviceId
    ) {
        GuardianMobileDevice mobileDevice =
                findOwnedDevice(
                        guardianUserId,
                        mobileDeviceId
                );

        mobileDevice.setEnabled(false);

        mobileDeviceRepository.save(mobileDevice);
    }

    private GuardianMobileDevice findOwnedDevice(
            Long guardianUserId,
            Long mobileDeviceId
    ) {
        return mobileDeviceRepository
                .findByIdAndGuardianUserId(
                        mobileDeviceId,
                        guardianUserId
                )
                .orElseThrow(
                        () ->
                                new GuardianMobileDeviceNotFoundException(
                                        mobileDeviceId
                                )
                );
    }

    private GuardianMobileDeviceResponse toResponse(
            GuardianMobileDevice mobileDevice
    ) {
        return new GuardianMobileDeviceResponse(
                mobileDevice.getId(),
                mobileDevice.getPlatform(),
                mobileDevice.getDeviceName(),
                mobileDevice.getAppVersion(),
                mobileDevice.isEnabled(),
                mobileDevice.getLastSeenAt(),
                mobileDevice.getCreatedAt(),
                mobileDevice.getUpdatedAt()
        );
    }
}