package com.guardian.cloud.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(
        prefix = "guardian.push-notification"
)
public class PushNotificationProperties {

    private int maxAttempts = 3;

    private Duration retryDelay =
            Duration.ofMinutes(1);

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException(
                    "Push max attempts must be at least 1"
            );
        }

        this.maxAttempts = maxAttempts;
    }

    public Duration getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(
            Duration retryDelay
    ) {
        if (
                retryDelay == null
                        || retryDelay.isZero()
                        || retryDelay.isNegative()
        ) {
            throw new IllegalArgumentException(
                    "Push retry delay must be positive"
            );
        }

        this.retryDelay = retryDelay;
    }
}