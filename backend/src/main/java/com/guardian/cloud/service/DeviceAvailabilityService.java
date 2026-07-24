package com.guardian.cloud.service;

import com.guardian.cloud.config.DeviceAvailabilityProperties;
import com.guardian.cloud.entity.Device;
import com.guardian.cloud.entity.DeviceStatus;
import com.guardian.cloud.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class DeviceAvailabilityService {

    private final DeviceRepository deviceRepository;
    private final DeviceEventService deviceEventService;
    private final DeviceAvailabilityProperties properties;
    private final Clock clock;

    public DeviceAvailabilityService(
            DeviceRepository deviceRepository,
            DeviceEventService deviceEventService,
            DeviceAvailabilityProperties properties
    ) {
        this.deviceRepository = deviceRepository;
        this.deviceEventService = deviceEventService;
        this.properties = properties;
        this.clock = Clock.systemUTC();
    }

    @Transactional
    public int markStaleDevicesOffline() {
        Instant detectedAt = Instant.now(clock);

        Instant cutoff = detectedAt.minus(
                properties.getOfflineTimeout()
        );

        List<Device> staleDevices =
                deviceRepository.findStaleDevicesForUpdate(
                        DeviceStatus.ONLINE,
                        cutoff
                );

        int offlineCount = 0;

        for (Device device : staleDevices) {
            if (markDeviceOffline(device, detectedAt)) {
                offlineCount++;
            }
        }

        return offlineCount;
    }

    private boolean markDeviceOffline(
            Device device,
            Instant detectedAt
    ) {
        if (device.getStatus() != DeviceStatus.ONLINE) {
            return false;
        }

        Instant lastSeenAt = device.getLastSeenAt();

        device.setStatus(DeviceStatus.OFFLINE);
        deviceRepository.save(device);

        deviceEventService.createDeviceOfflineEvent(
                device,
                detectedAt,
                lastSeenAt
        );

        return true;
    }
}