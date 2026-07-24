package com.guardian.cloud.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(
        prefix = "guardian.device-availability"
)
public class DeviceAvailabilityProperties {

    private Duration offlineTimeout =
            Duration.ofMinutes(5);

    private Duration checkInterval =
            Duration.ofMinutes(1);

    private Duration initialDelay =
            Duration.ofSeconds(30);

    public Duration getOfflineTimeout() {
        return offlineTimeout;
    }

    public void setOfflineTimeout(
            Duration offlineTimeout
    ) {
        if (
                offlineTimeout == null
                        || offlineTimeout.isZero()
                        || offlineTimeout.isNegative()
        ) {
            throw new IllegalArgumentException(
                    "Offline timeout must be positive"
            );
        }

        this.offlineTimeout = offlineTimeout;
    }

    public Duration getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(
            Duration checkInterval
    ) {
        if (
                checkInterval == null
                        || checkInterval.isZero()
                        || checkInterval.isNegative()
        ) {
            throw new IllegalArgumentException(
                    "Check interval must be positive"
            );
        }

        this.checkInterval = checkInterval;
    }

    public Duration getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(
            Duration initialDelay
    ) {
        if (
                initialDelay == null
                        || initialDelay.isNegative()
        ) {
            throw new IllegalArgumentException(
                    "Initial delay must not be negative"
            );
        }

        this.initialDelay = initialDelay;
    }
}