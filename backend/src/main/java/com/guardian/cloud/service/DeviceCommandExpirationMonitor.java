package com.guardian.cloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DeviceCommandExpirationMonitor {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    DeviceCommandExpirationMonitor.class
            );

    private final DeviceCommandService
            deviceCommandService;

    public DeviceCommandExpirationMonitor(
            DeviceCommandService
                    deviceCommandService
    ) {
        this.deviceCommandService =
                deviceCommandService;
    }

    @Scheduled(
            fixedDelayString =
                    "${guardian.device-command.expiration-check-interval:30000}",
            initialDelayString =
                    "${guardian.device-command.expiration-initial-delay:30000}"
    )
    public void expireCommands() {
        try {
            int expiredCount =
                    deviceCommandService
                            .expireDueCommands();

            if (expiredCount > 0) {
                LOGGER.info(
                        "Expired {} device command(s)",
                        expiredCount
                );
            }
        } catch (Exception exception) {
            LOGGER.error(
                    "Device command expiration check failed",
                    exception
            );
        }
    }
}