package com.guardian.cloud.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(
        prefix = "guardian.emergency-escalation"
)
public class EmergencyEscalationProperties {

    private Duration attemptTimeout =
            Duration.ofMinutes(2);

    public Duration getAttemptTimeout() {
        return attemptTimeout;
    }

    public void setAttemptTimeout(
            Duration attemptTimeout
    ) {
        if (
                attemptTimeout == null
                        || attemptTimeout.isZero()
                        || attemptTimeout.isNegative()
        ) {
            throw new IllegalArgumentException(
                    "Emergency escalation attempt timeout must be positive"
            );
        }

        this.attemptTimeout = attemptTimeout;
    }
}