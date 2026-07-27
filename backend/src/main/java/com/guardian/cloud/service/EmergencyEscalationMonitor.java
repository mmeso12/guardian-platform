package com.guardian.cloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EmergencyEscalationMonitor {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    EmergencyEscalationMonitor.class
            );

    private final EmergencyEscalationService
            escalationService;

    public EmergencyEscalationMonitor(
            EmergencyEscalationService escalationService
    ) {
        this.escalationService = escalationService;
    }

    @Scheduled(
            fixedDelayString =
                    "${guardian.emergency-escalation.check-interval:30000}",
            initialDelayString =
                    "${guardian.emergency-escalation.initial-delay:30000}"
    )
    public void processEscalations() {
        try {
            int processed =
                    escalationService
                            .processDueEscalations();

            if (processed > 0) {
                LOGGER.info(
                        "Processed {} due emergency escalation(s)",
                        processed
                );
            }
        } catch (Exception exception) {
            LOGGER.error(
                    "Emergency escalation processing failed",
                    exception
            );
        }
    }
}