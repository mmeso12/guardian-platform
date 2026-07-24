package com.guardian.cloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DeviceAvailabilityMonitor {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    DeviceAvailabilityMonitor.class
            );

    private final DeviceAvailabilityService
            availabilityService;

    public DeviceAvailabilityMonitor(
            DeviceAvailabilityService
                    availabilityService
    ) {
        this.availabilityService =
                availabilityService;
    }

    @Scheduled(
          fixedDelayString =
                  "${guardian.device-availability.check-interval:60000}",
          initialDelayString =
                  "${guardian.device-availability.initial-delay:30000}"
  )
  public void checkDeviceAvailability() {
      try {
          int offlineCount =
                  availabilityService.markStaleDevicesOffline();

          if (offlineCount > 0) {
              LOGGER.info(
                      "Marked {} device(s) offline",
                      offlineCount
              );
          }
      } catch (Exception exception) {
          LOGGER.error(
                  "Device availability check failed",
                  exception
          );
      }
  }
}