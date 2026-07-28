package com.guardian.cloud.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(
        prefix = "guardian.device-command"
)
public class DeviceCommandProperties {

    private Duration defaultExpiration =
            Duration.ofMinutes(10);

    private int maximumPendingFetch = 20;

    public Duration getDefaultExpiration() {
        return defaultExpiration;
    }

    public void setDefaultExpiration(
            Duration defaultExpiration
    ) {
        if (
                defaultExpiration == null
                        || defaultExpiration.isZero()
                        || defaultExpiration.isNegative()
        ) {
            throw new IllegalArgumentException(
                    "Default command expiration must be positive"
            );
        }

        this.defaultExpiration =
                defaultExpiration;
    }

    public int getMaximumPendingFetch() {
        return maximumPendingFetch;
    }

    public void setMaximumPendingFetch(
            int maximumPendingFetch
    ) {
        if (maximumPendingFetch < 1) {
            throw new IllegalArgumentException(
                    "Maximum pending fetch must be at least 1"
            );
        }

        this.maximumPendingFetch =
                maximumPendingFetch;
    }
}